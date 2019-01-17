package com.gitblit.console.servlet;

import com.gitblit.console.service.Workspace;
import com.gitblit.console.service.WorkspaceService;
import com.gitblit.servlet.JsonServlet;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import org.eclipse.jgit.api.Status;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * @author miller
 */
@Singleton
public class RepositoryStatusServlet extends JsonServlet {
    @Inject
    WorkspaceService workspaceService;

    @Override
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String pathInfo = httpServletRequest.getPathInfo();
        String repository = pathInfo.substring(1);

        Optional<Workspace> workspace = workspaceService.workspace(repository);
        Map<String, Boolean> response = Maps.newHashMap();
        if (!workspace.isPresent()) {
            response.put("changed", false);
        } else {
            try {
                Status status = workspace.get().git().status().call();
                Set<String> changed = status.getUncommittedChanges();
                response.put("changed", !changed.isEmpty());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        this.serialize(httpServletResponse, response);
    }
}
