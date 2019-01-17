package com.gitblit.console.servlet;

import com.gitblit.console.ConsoleContext;
import com.gitblit.servlet.JsonServlet;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
        try {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("commit-" + UUID.randomUUID().toString()).call();
            git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider("admin", "admin")).call();
            String workSpace = ConsoleContext.WORK_SPACE_DIR + repository + File.separator;
            git.close();
            FileUtils.deleteDirectory(new File(workSpace));
            ConsoleContext.WORK_SPACE.remove(repository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
