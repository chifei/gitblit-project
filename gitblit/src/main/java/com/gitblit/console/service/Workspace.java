package com.gitblit.console.service;

import com.gitblit.console.ConsoleContext;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;

/**
 * @author chi
 */
public class Workspace {
    private final String repo;
    private final File dir;

    public Workspace(String repo, File dir) {
        this.repo = repo;
        this.dir = dir;
    }

    public File file(String file) {
        return new File(dir, file);
    }

    public Git git() {
        try {
            return Git.cloneRepository().setURI(ConsoleContext.BASE_GIT_URI + repo)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("admin", "admin"))
                .setDirectory(dir)
                .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete() {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
