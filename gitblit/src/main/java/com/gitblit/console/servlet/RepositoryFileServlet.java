package com.gitblit.console.servlet;

import com.gitblit.console.ConsoleContext;
import com.gitblit.console.servlet.model.RepositoryTreePath;
import com.gitblit.console.servlet.model.UpdateFileRequest;
import com.gitblit.manager.IRepositoryManager;
import com.gitblit.servlet.JsonServlet;
import com.gitblit.utils.JGitUtils;
import com.gitblit.utils.JsonUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.wicket.util.file.Files;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;


/**
 * @author chi
 */
@Singleton
public class RepositoryFileServlet extends JsonServlet {
    private IRepositoryManager repositoryManager;

    @Inject
    public RepositoryFileServlet(IRepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpStatus.SC_NOT_FOUND);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        Map<String, String> params = RepositoryTreePath.parse(pathInfo);
        if (params.get("r") == null) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            response.getWriter().print("invalid repository");
            return;
        }
        if (params.get("h") == null) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            response.getWriter().print("invalid branch");
            return;
        }
        Git workSpace = ConsoleContext.WORK_SPACE.get(params.get("r"));
        Map<String, String> map = Maps.newHashMap();
        if (workSpace != null) {
            String path = ConsoleContext.WORK_SPACE_DIR + params.get("r") + "/" + params.get("f");
            File workSpaceFile = new File(path);
            StringWriter stringWriter = new StringWriter();
            try (InputStream in = new FileInputStream(workSpaceFile)) {
                IOUtils.copy(in, stringWriter, Charsets.UTF_8.name());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            map.put("content", stringWriter.toString());
        } else {
            Repository r = repositoryManager.getRepository(params.get("r"));
            final String blobPath = params.get("f");
            RevCommit commit = getCommit(r, params);
            String source = JGitUtils.getStringContent(r, commit.getTree(), blobPath, Charsets.UTF_8.name());
            if (source == null) {
                response.setStatus(HttpStatus.SC_NOT_FOUND);
                response.getWriter().print("missing file");
                return;
            }
            map.put("content", source);
        }
        this.serialize(response, map);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        Map<String, String> params = RepositoryTreePath.parse(pathInfo);
        String repo = params.get("r");
        if (repo == null) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            response.getWriter().print("invalid repository");
            return;
        }
        if (params.get("h") == null) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            response.getWriter().print("invalid branch");
            return;
        }
        Git workSpace = ConsoleContext.WORK_SPACE.get(repo);
        if (workSpace == null) {
            File repoDir = new File(ConsoleContext.WORK_SPACE_DIR + repo);
            try {
                Files.remove(repoDir);
                workSpace = Git.cloneRepository().setURI(ConsoleContext.BASE_GIT_URI + repo)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("admin", "admin"))
                    .setDirectory(repoDir)
                    .call();
                ConsoleContext.WORK_SPACE.put(repo, workSpace);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        String path = ConsoleContext.WORK_SPACE_DIR + repo + "/" + params.get("f");
        File workSpaceFile = new File(path);
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(request.getInputStream(), stringWriter, Charsets.UTF_8.name());
        UpdateFileRequest updateFileRequest = JsonUtils.fromJsonString(stringWriter.toString(), UpdateFileRequest.class);
        try (OutputStream out = new FileOutputStream(workSpaceFile)) {
            ByteStreams.copy(new ByteArrayInputStream(updateFileRequest.content.getBytes(Charsets.UTF_8.name())), out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected RevCommit getCommit(Repository r, Map<String, String> params) {
        RevCommit commit = JGitUtils.getCommit(r, params.get("h"));
        if (commit == null) {
            return null;
        }
        return commit;
    }
}
