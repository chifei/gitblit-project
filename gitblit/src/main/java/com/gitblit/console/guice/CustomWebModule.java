package com.gitblit.console.guice;

import com.gitblit.console.servlet.RepositoriesServlet;
import com.gitblit.console.servlet.RepositoryTreeServlet;
import com.google.inject.servlet.ServletModule;

/**
 * @author miller
 */
public class CustomWebModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve("/api/repository/list").with(RepositoriesServlet.class);
        serve("/api/repository/tree/*").with(RepositoryTreeServlet.class);
    }
}
