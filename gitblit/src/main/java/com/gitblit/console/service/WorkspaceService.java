package com.gitblit.console.service;

import com.gitblit.console.ConsoleContext;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.util.Optional;

/**
 * @author chi
 */
public class WorkspaceService {
    private final File dir;

    public WorkspaceService(File dir) {
        this.dir = dir;
    }

    public Optional<Workspace> workspace(String repo) {
        File repoDir = new File(dir, repo);
        if (repoDir.exists()) {
            return Optional.of(new Workspace(repo, repoDir));
        }
        return Optional.empty();
    }

    public Workspace createWorkspace(String repo) {
        File repoDir = new File(dir, repo);
        try {
            Git git = Git.cloneRepository().setURI(ConsoleContext.BASE_GIT_URI + repo)
                .setCredentialsProvider(new SystemCredentialsProvider())
                .setDirectory(repoDir)
                .call();
            git.close();
            return new Workspace(repo, repoDir);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
