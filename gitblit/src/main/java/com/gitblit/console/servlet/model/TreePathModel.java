package com.gitblit.console.servlet.model;

import org.eclipse.jgit.lib.FileMode;

import java.io.Serializable;

/**
 * @author miller
 */
public class TreePathModel implements Serializable {
    private static final long serialVersionUID = 127060587131650218L;
    public final String name;
    public final String path;
    public final long size;
    public final int mode;
    public final String objectId;
    public final String commitId;
    public final boolean isSymlink;
    public final boolean isSubmodule;
    public final boolean isTree;
    public final boolean isFile;
    public final String filestoreOid;
    public boolean isParentPath;

    public TreePathModel(String name, String path, String filestoreOid, long size, int mode, String objectId, String commitId) {
        this.name = name;
        this.path = path;
        this.filestoreOid = filestoreOid;
        this.size = size;
        this.mode = mode;
        this.objectId = objectId;
        this.commitId = commitId;
        this.isSymlink = FileMode.SYMLINK.equals(this.mode);
        this.isSubmodule = FileMode.GITLINK.equals(this.mode);
        this.isTree = FileMode.TREE.equals(this.mode);
        this.isFile = FileMode.REGULAR_FILE.equals(this.mode) || FileMode.EXECUTABLE_FILE.equals(this.mode) || FileMode.MISSING.equals(this.mode) && !this.isSymlink && !this.isSubmodule && !this.isTree;
    }
}
