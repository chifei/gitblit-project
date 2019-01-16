package com.gitblit.console.guice;

import com.gitblit.console.servlet.ConsoleServlet;
import com.gitblit.console.servlet.RepositoriesServlet;
import com.gitblit.console.servlet.RepositoryFileServlet;
import com.gitblit.console.servlet.RepositoryTreeServlet;
import com.gitblit.console.servlet.ResourceServlet;
import com.google.inject.servlet.ServletModule;

/**
 * @author miller
 */
public class CustomWebModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve("/console").with(ConsoleServlet.class);
        serve("/api/repository/list").with(RepositoriesServlet.class);
        serve("/api/repository/tree/*").with(RepositoryTreeServlet.class);
        serve("/api/repository/file/*").with(RepositoryFileServlet.class);
        serve("/static/*").with(ResourceServlet.class);
    }
}
