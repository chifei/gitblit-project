package com.gitblit.console.servlet;

import com.gitblit.IStoredSettings;
import com.gitblit.manager.IGitblit;
import com.gitblit.models.RepositoryModel;
import com.gitblit.servlet.JsonServlet;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author miller
 */
@Singleton
public class RepositoriesServlet extends JsonServlet {
    public static final String BASE_GIT_URI = "ssh://admin@localhost:29418/";
    public static final String BASE_CLONE_DIR;
    public static final Map<String, Git> LOCAL_REPOSITORIES = Maps.newConcurrentMap();

    static {
        File tempDir = Files.createTempDir();
        BASE_CLONE_DIR = tempDir.getAbsolutePath() + File.separator;
    }

    private final Logger logger = LoggerFactory.getLogger(RepositoriesServlet.class);
    //private static final String BASE_GIT_URI = "https://admin@localhost:8443/r/";
    private volatile boolean loaded = false;
    private volatile boolean free = true;

    private IStoredSettings settings;
    private IGitblit gitblit;

    @Inject
    public RepositoriesServlet(IStoredSettings settings, IGitblit gitblit) {
        this.settings = settings;
        this.gitblit = gitblit;
        logger.info("base clone dir={}", BASE_CLONE_DIR);
    }

    @Override
    protected void processRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        httpServletResponse.setStatus(HttpStatus.SC_NOT_FOUND);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<RepositoryModel> repositories = gitblit.getRepositoryModels();
        cloneRepositories(repositories);
        this.serialize(response, repositories);
    }

    private void cloneRepositories(List<RepositoryModel> repositories) {
        if (free && !loaded) {
            try {
                free = false;
                for (RepositoryModel r : repositories) {
                    String master = r.HEAD.substring(r.HEAD.lastIndexOf("/") + 1);
                    File repoDir = new File(BASE_CLONE_DIR + r.name + "/" + master);
                    FileUtils.deleteDirectory(repoDir);
                    Git git = Git.cloneRepository().setURI(BASE_GIT_URI + r.name)
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
    }
}
