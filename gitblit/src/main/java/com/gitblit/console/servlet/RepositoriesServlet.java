package com.gitblit.console.servlet;

import com.gitblit.IStoredSettings;
import com.gitblit.manager.IGitblit;
import com.gitblit.models.RepositoryModel;
import com.gitblit.servlet.JsonServlet;
import com.google.inject.Singleton;
import org.apache.http.HttpStatus;

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
    private IStoredSettings settings;
    private IGitblit gitblit;

    @Inject
    public RepositoriesServlet(IStoredSettings settings, IGitblit gitblit) {
        this.settings = settings;
        this.gitblit = gitblit;
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
}
