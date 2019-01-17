package com.gitblit.console.servlet;

import com.gitblit.console.ConsoleContext;
import com.gitblit.servlet.JsonServlet;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;


/**
 * @author miller
 */
@Singleton
public class RepositoryStatusServlet extends JsonServlet {
    @Override
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String pathInfo = httpServletRequest.getPathInfo();
        String repository = pathInfo.substring(1);
        Git git = ConsoleContext.WORK_SPACE.get(repository);
        Map<String, Boolean> map = Maps.newHashMap();
        if (git == null) {
            map.put("changed", false);
        } else {
            try {
                Status status = git.status().call();
                Set<String> changed = status.getUncommittedChanges();
                map.put("changed", !changed.isEmpty());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        this.serialize(httpServletResponse, map);
    }
}