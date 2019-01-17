package com.gitblit.console.service;

import com.gitblit.console.ConsoleContext;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

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
            Git.cloneRepository().setURI(ConsoleContext.BASE_GIT_URI + repo)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("admin", "admin") {
                    @Override
                    public boolean get(URIish uri, CredentialItem... items)
                        throws UnsupportedCredentialItem {
                        for (CredentialItem i : items) {
                            if (i instanceof CredentialItem.Username) {
                                ((CredentialItem.Username) i).setValue("admin");
                                continue;
                            }
                            if (i instanceof CredentialItem.Password) {
                                ((CredentialItem.Password) i).setValue("admin".toCharArray());
                                continue;
                            }
                            if (i instanceof CredentialItem.StringType) {
                                if (i.getPromptText().equals("Password: ")) { //$NON-NLS-1$
                                    ((CredentialItem.StringType) i).setValue("admin");
                                    continue;
                                }
                            }
                            if (i instanceof CredentialItem.YesNoType) {
                                ((CredentialItem.YesNoType) i).setValue(true);
                                continue;
                            }
                            throw new UnsupportedCredentialItem(uri, i.getClass().getName()
                                + ":" + i.getPromptText()); //$NON-NLS-1$
                        }
                        return true;
                    }
                })
                .setDirectory(repoDir)
                .call();
            return new Workspace(repo, repoDir);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
