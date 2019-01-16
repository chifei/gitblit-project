package com.gitblit.console.servlet;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author miller
 */
class RepositoryTreePath {
    static Map<String, String> parse(String pathInfo) {
        // /r/h/f
        Map<String, String> params = Maps.newHashMap();
        int start = 1;
        int end = pathInfo.indexOf('/', start);
        if (end < 0) {
            return params;
        }
        params.put("r", pathInfo.substring(start, end));

        start = end + 1;
        end = pathInfo.indexOf('/', start);
        if (end < 0) {
            params.put("h", pathInfo.substring(start));
        } else {
            params.put("h", pathInfo.substring(start, end));
            params.put("f", pathInfo.substring(end + 1));
        }
        return params;
    }
}
