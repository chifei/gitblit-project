package com.gitblit.console.servlet;

import com.gitblit.servlet.JsonServlet;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.gitblit.console.servlet.RepositoriesServlet.LOCAL_REPOSITORIES;

/**
 * @author miller
 */
@Singleton
public class RepositoryStatusServlet extends JsonServlet {
    @Override
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String pathInfo = httpServletRequest.getPathInfo();
        String repository = pathInfo.substring(1);
        Git git = LOCAL_REPOSITORIES.get(repository);
        if (git == null) {
            httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
            return;
        }
        try {
            Status status = git.status().call();
            Set<String> changed = status.getUncommittedChanges();
            Map<String, Boolean> map = Maps.newHashMap();
            map.put("changed", !changed.isEmpty());
            this.serialize(httpServletResponse, map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
