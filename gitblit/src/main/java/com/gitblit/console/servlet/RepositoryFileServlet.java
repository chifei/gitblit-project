package com.gitblit.console.servlet;

import com.gitblit.console.servlet.model.RepositoryTreePath;
import com.gitblit.manager.IRepositoryManager;
import com.gitblit.servlet.JsonServlet;
import com.gitblit.utils.JGitUtils;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import groovy.json.internal.Charsets;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author chi
 */
@Singleton
public class RepositoryFileServlet extends JsonServlet {
    private IRepositoryManager repositoryManager;

    @Inject
    public RepositoryFileServlet(IRepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        Map<String, String> params = RepositoryTreePath.parse(pathInfo);
        if (params.get("r") == null) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            response.getWriter().print("invalid repository");
            return;
        }
        if (params.get("h") == null) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            response.getWriter().print("invalid branch");
            return;
        }
        Repository r = repositoryManager.getRepository(params.get("r"));
        final String blobPath = params.get("f");
        RevCommit commit = getCommit(r, params);

        String source = JGitUtils.getStringContent(r, commit.getTree(), blobPath, Charsets.UTF_8.name());
        if (source == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            response.getWriter().print("missing file");
            return;
        }
        Map<String, String> map = Maps.newHashMap();
        map.put("content", source);
        this.serialize(response, map);
    }

    protected RevCommit getCommit(Repository r, Map<String, String> params) {
        RevCommit commit = JGitUtils.getCommit(r, params.get("h"));
        if (commit == null) {
            return null;
        }
        return commit;
    }
}
