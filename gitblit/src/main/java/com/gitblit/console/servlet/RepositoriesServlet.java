package com.gitblit.console.servlet;

import com.gitblit.IStoredSettings;
import com.gitblit.console.ConsoleContext;
import com.gitblit.manager.IGitblit;
import com.gitblit.models.RepositoryModel;
import com.gitblit.servlet.JsonServlet;
import com.google.inject.Singleton;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author miller
 */
@Singleton
public class RepositoriesServlet extends JsonServlet {


    private final Logger logger = LoggerFactory.getLogger(RepositoriesServlet.class);
    //private static final String BASE_GIT_URI = "https://admin@localhost:8443/r/";
//    private volatile boolean loaded = false;
//    private volatile boolean free = true;

    private IStoredSettings settings;
    private IGitblit gitblit;

    @Inject
    public RepositoriesServlet(IStoredSettings settings, IGitblit gitblit) {
        this.settings = settings;
        this.gitblit = gitblit;
        logger.info("base clone dir={}", ConsoleContext.BASE_CLONE_DIR);
    }

    @Override
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<RepositoryModel> repositories = gitblit.getRepositoryModels();
        this.serialize(response, repositories);
    }

    /*private void cloneRepositories(List<RepositoryModel> repositories) {
        if (free && !loaded) {
            try {
                free = false;
                for (RepositoryModel r : repositories) {
                    String master = r.HEAD.substring(r.HEAD.lastIndexOf("/") + 1);
                    File repoDir = new File(ConsoleContext.BASE_CLONE_DIR + r.name + "/" + master);
                    FileUtils.deleteDirectory(repoDir);
                    Git git = Git.cloneRepository().setURI(ConsoleContext.BASE_GIT_URI + r.name)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider("admin", "admin"))
                        .setDirectory(repoDir)
                        .call();
                    LOCAL_REPOSITORIES.put(r.name, git);
                }
                loaded = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                free = true;
            }
        }
    }*/
}
