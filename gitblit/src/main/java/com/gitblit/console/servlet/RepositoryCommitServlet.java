package com.gitblit.console.servlet;

import com.gitblit.console.service.Workspace;
import com.gitblit.console.service.WorkspaceService;
import com.gitblit.servlet.JsonServlet;
import com.google.inject.Singleton;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * @author miller
 */
@Singleton
public class RepositoryCommitServlet extends JsonServlet {
    @Inject
    WorkspaceService workspaceService;

    @Override
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String pathInfo = httpServletRequest.getPathInfo();
        String repository = pathInfo.substring(1);
        Optional<Workspace> workspace = workspaceService.workspace(repository);
        if (!workspace.isPresent()) {
            return;
        }
        Git git = workspace.get().git();
        try {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("commit-" + UUID.randomUUID().toString()).call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider("admin", "admin")).call();
            workspace.get().delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
