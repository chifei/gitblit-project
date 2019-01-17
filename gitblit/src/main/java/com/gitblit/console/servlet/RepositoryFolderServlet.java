package com.gitblit.console.servlet;

import com.gitblit.console.service.Workspace;
import com.gitblit.console.service.WorkspaceService;
import com.gitblit.console.servlet.model.RepositoryTreePath;
import com.gitblit.manager.IRepositoryManager;
import com.gitblit.servlet.JsonServlet;
import com.gitblit.utils.JGitUtils;
import com.google.common.io.Files;
import com.google.inject.Singleton;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;


/**
 * @author chi
 */
@Singleton
public class RepositoryFolderServlet extends JsonServlet {
    private IRepositoryManager repositoryManager;
    private WorkspaceService workspaceService;

    @Inject
    public RepositoryFolderServlet(IRepositoryManager repositoryManager, WorkspaceService workspaceService) {
        this.repositoryManager = repositoryManager;
        this.workspaceService = workspaceService;
    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpStatus.SC_NOT_FOUND);
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
        Optional<Workspace> workspaceOptional = workspaceService.workspace(repo);
        Workspace workspace;
        if (!workspaceOptional.isPresent()) {
            workspace = workspaceService.createWorkspace(repo);
        } else {
            workspace = workspaceOptional.get();
        }
        File dir = workspace.file(params.get("f"));
        if (dir.exists()) {
            throw new RuntimeException("folder exists");
        }
        Files.createParentDirs(dir);
        dir.mkdir();
    }

    protected RevCommit getCommit(Repository r, Map<String, String> params) {
        RevCommit commit = JGitUtils.getCommit(r, params.get("h"));
        if (commit == null) {
            return null;
        }
        return commit;
    }
}
