package com.gitblit.console.servlet;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author miller
 */
public class RepositoryTreePathTest {

    @Test
    public void parse() {
        String path = "/test.git/master/temp/second";
        Map<String, String> params = RepositoryTreePath.parse(path);
        assertEquals(params.get("r"), "test.git");
        assertEquals(params.get("h"), "master");
        assertEquals(params.get("f"), "temp/second");
        path = "/test.git/master";
        params = RepositoryTreePath.parse(path);
        assertEquals(params.get("r"), "test.git");
        assertEquals(params.get("h"), "master");
        assertNull(params.get("f"));
    }
}