package com.gitblit.console.servlet;

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
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

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

import static com.gitblit.console.servlet.RepositoriesServlet.BASE_CLONE_DIR;

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
        /*Repository r = repositoryManager.getRepository(params.get("r"));
        final String blobPath = params.get("f");
        RevCommit commit = getCommit(r, params);

        String source = JGitUtils.getStringContent(r, commit.getTree(), blobPath, Charsets.UTF_8.name());
        if (source == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            response.getWriter().print("missing file");
            return;
        }*/
        String path = BASE_CLONE_DIR + params.get("r") + "/" + params.get("h") + "/" + params.get("f");
        StringWriter stringWriter = new StringWriter();
        try (InputStream in = new FileInputStream(new File(path))) {
            IOUtils.copy(in, stringWriter, Charsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<String, String> map = Maps.newHashMap();
        map.put("content", stringWriter.toString());
        this.serialize(response, map);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(request.getInputStream(), stringWriter, Charsets.UTF_8.name());
        UpdateFileRequest updateFileRequest = JsonUtils.fromJsonString(stringWriter.toString(), UpdateFileRequest.class);
        String path = BASE_CLONE_DIR + params.get("r") + "/" + params.get("h") + "/" + params.get("f");
        try (OutputStream out = new FileOutputStream(new File(path))) {
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
