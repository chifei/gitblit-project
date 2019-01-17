package com.gitblit.console.servlet;

import com.gitblit.console.ConsoleContext;
import com.gitblit.servlet.JsonServlet;
import com.google.inject.Singleton;
import org.eclipse.jgit.api.Git;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author miller
 */
@Singleton
public class RepositoryCommitServlet extends JsonServlet {
    @Override
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String pathInfo = httpServletRequest.getPathInfo();
        String repository = pathInfo.substring(1);
        Git git = ConsoleContext.WORK_SPACE.get(repository);
        if (git == null) {
            return;
        }
        /*Git git = LOCAL_REPOSITORIES.get(repository);
        if (git == null) {
            httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
            return;
        }*/

    }
}
