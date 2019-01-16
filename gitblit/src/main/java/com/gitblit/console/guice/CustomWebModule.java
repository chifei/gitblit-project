package com.gitblit.console.guice;

import com.gitblit.console.servlet.RepositoriesServlet;
import com.google.inject.servlet.ServletModule;

/**
 * @author miller
 */
public class CustomWebModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve("/api/repositories").with(RepositoriesServlet.class);
    }
}
