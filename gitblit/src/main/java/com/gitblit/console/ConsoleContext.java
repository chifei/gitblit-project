package com.gitblit.console;

import com.gitblit.IStoredSettings;
import com.gitblit.console.guice.CustomWebModule;
import com.gitblit.guice.CoreModule;
import com.gitblit.guice.WebModule;
import com.gitblit.servlet.GitblitContext;
import com.google.inject.AbstractModule;

import java.io.File;

/**
 * @author miller
 */
public class ConsoleContext extends GitblitContext {
    public static final String BASE_GIT_URI = "ssh://admin@localhost:29418/";
    private final File baseFolder;

    public ConsoleContext(IStoredSettings settings, File baseFolder) {
        super(settings, baseFolder);
        this.baseFolder = baseFolder;
    }

    protected AbstractModule[] getModules() {
        return new AbstractModule[]{new CoreModule(), new WebModule(), new CustomWebModule(baseFolder)};
    }
}
