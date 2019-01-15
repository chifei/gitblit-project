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
    public ConsoleContext(IStoredSettings settings, File baseFolder) {
        super(settings, baseFolder);
    }

    protected AbstractModule[] getModules() {
        return new AbstractModule[]{new CoreModule(), new WebModule(), new CustomWebModule()};
    }
}
