package com.gitblit.console.servlet;

import com.gitblit.console.ConsoleContext;
import com.gitblit.console.servlet.model.RepositoryTreePath;
import com.gitblit.console.servlet.model.TreePathModel;
import com.gitblit.manager.IRepositoryManager;
import com.gitblit.models.PathModel;
import com.gitblit.servlet.JsonServlet;
import com.gitblit.utils.JGitUtils;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author miller
 */
@Singleton
public class RepositoryTreeServlet extends JsonServlet {
    private IRepositoryManager repositoryManager;

    @Inject
    public RepositoryTreeServlet(IRepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @Override
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String pathInfo = httpServletRequest.getPathInfo();
        Map<String, String> params = RepositoryTreePath.parse(pathInfo);
        String repo = params.get("r");
        if (repo == null) {
            httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
            httpServletResponse.getWriter().print("invalid repository");
            return;
        }
        if (params.get("h") == null) {
            httpServletResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
            httpServletResponse.getWriter().print("invalid branch");
            return;
        }
        String path = params.get("f");
        if (path == null) {
            path = "";
        }
        Git git = ConsoleContext.WORK_SPACE.get(repo);
        if (git == null) {
            Repository r = repositoryManager.getRepository(repo);
            RevCommit commit = getCommit(r, params);
            List<TreePathModel> paths = JGitUtils.getFilesInPath2(r, path, commit)
                .stream().map(this::model).collect(Collectors.toList());
            this.serialize(httpServletResponse, paths);
        } else {
            String workSpacePath = ConsoleContext.WORK_SPACE_DIR + repo + File.separator + path + File.separator;
            File file = new File(workSpacePath);
            if (!file.exists()) {
                httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
                return;
            }
            File[] files = file.listFiles();
            List<TreePathModel> paths = Lists.newArrayList();
            if (files != null) {
                for (File f : files) {
                    if (".git".equals(f.getName())) {
                        continue;
                    }
                    String p;
                    if ("".equals(path)) {
                        p = f.getName();
                    } else {
                        p = path + File.separator + f.getName();
                    }
                    paths.add(model(f, p));
                }
            }
            this.serialize(httpServletResponse, paths);
        }

    }

    protected RevCommit getCommit(Repository r, Map<String, String> params) {
        RevCommit commit = JGitUtils.getCommit(r, params.get("h"));
        if (commit == null) {
            return null;
        }
        return commit;
    }

    private TreePathModel model(PathModel pathModel) {
        return new TreePathModel(pathModel.name, pathModel.path, pathModel.getFilestoreOid(),
            pathModel.size, pathModel.mode, pathModel.objectId, pathModel.commitId);
    }

    private TreePathModel model(File file, String path) {
        return new TreePathModel(file.getName(), path, file.length(), file.isDirectory(), file.isFile());
    }
}
