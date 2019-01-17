package com.gitblit.console;

import com.gitblit.IStoredSettings;
import com.gitblit.console.guice.CustomWebModule;
import com.gitblit.guice.CoreModule;
import com.gitblit.guice.WebModule;
import com.gitblit.servlet.GitblitContext;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.util.Map;

/**
 * @author miller
 */
public class ConsoleContext extends GitblitContext {
    public static final Map<String, Git> WORK_SPACE = Maps.newConcurrentMap();
    public static final String BASE_GIT_URI = "ssh://admin@localhost:29418/";
    public static final String WORK_SPACE_DIR;

    static {
        File tempDir = Files.createTempDir();
        WORK_SPACE_DIR = tempDir.getAbsolutePath() + File.separator;
    }

    public ConsoleContext(IStoredSettings settings, File baseFolder) {
        super(settings, baseFolder);
    }

    protected AbstractModule[] getModules() {
        return new AbstractModule[]{new CoreModule(), new WebModule(), new CustomWebModule()};
    }
}
