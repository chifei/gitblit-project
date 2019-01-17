package com.gitblit.console.servlet;

import com.gitblit.console.ConsoleContext;
import com.gitblit.servlet.JsonServlet;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author miller
 */
@Singleton
public class RepositoryRevertServlet extends JsonServlet {
    @Override
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String pathInfo = httpServletRequest.getPathInfo();
        String repository = pathInfo.substring(1);
        Git git = ConsoleContext.WORK_SPACE.get(repository);
        if (git == null) {
            return;
        }
        try {
            git.close();
            String workSpace = ConsoleContext.WORK_SPACE_DIR + repository + File.separator;
            FileUtils.forceDelete(new File(workSpace));
            ConsoleContext.WORK_SPACE.remove(repository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
