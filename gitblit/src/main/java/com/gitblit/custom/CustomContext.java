package com.gitblit.custom;

import com.gitblit.IStoredSettings;
import com.gitblit.custom.guice.CustomWebModule;
import com.gitblit.manager.IManager;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * @author miller
 */
public class CustomContext extends GuiceServletContextListener {
    private static CustomContext custom;
    protected final Logger logger = LoggerFactory.getLogger(CustomContext.class);
    private final List<IManager> managers;
    private final IStoredSettings goSettings;
    private final File goBaseFolder;

    public CustomContext() {
        this((IStoredSettings) null, (File) null);
    }

    public CustomContext(IStoredSettings settings, File baseFolder) {
        this.managers = Lists.newArrayList();
        this.goSettings = settings;
        this.goBaseFolder = baseFolder;
        custom = this;
    }

    @Override
    protected Injector getInjector() {
        return Guice.createInjector(this.getModules());
    }

    protected AbstractModule[] getModules() {
        return new AbstractModule[]{new CustomWebModule()};
    }
}
