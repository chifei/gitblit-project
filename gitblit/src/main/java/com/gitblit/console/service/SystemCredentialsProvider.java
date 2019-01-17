package com.gitblit.console.service;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * @author chi
 */
public class SystemCredentialsProvider extends UsernamePasswordCredentialsProvider {
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    public SystemCredentialsProvider() {
        super(USERNAME, PASSWORD);
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items)
        throws UnsupportedCredentialItem {
        for (CredentialItem i : items) {
            if (i instanceof CredentialItem.Username) {
                ((CredentialItem.Username) i).setValue(USERNAME);
                continue;
            }
            if (i instanceof CredentialItem.Password) {
                ((CredentialItem.Password) i).setValue(PASSWORD.toCharArray());
                continue;
            }
            if (i instanceof CredentialItem.StringType) {
                if (i.getPromptText().equals("Password: ")) { //$NON-NLS-1$
                    ((CredentialItem.StringType) i).setValue(PASSWORD);
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
}
