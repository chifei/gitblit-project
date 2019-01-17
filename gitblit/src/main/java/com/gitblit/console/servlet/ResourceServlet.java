package com.gitblit.console.servlet;

import com.gitblit.console.util.MediaTypes;
import com.google.common.io.ByteStreams;
import com.google.inject.Singleton;
import org.apache.http.HttpStatus;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author chi
 */
@Singleton
public class ResourceServlet extends HttpServlet {
    private static final String BASE_PATH = "static";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        try (InputStream inputStream = resource(pathInfo);
             ServletOutputStream outputStream = response.getOutputStream()) {
            if (inputStream == null) {
                response.setStatus(HttpStatus.SC_NOT_FOUND);
                return;
            }
            response.setContentType(MediaTypes.getMediaType(pathInfo));
            ByteStreams.copy(inputStream, outputStream);
        }
    }

    private InputStream resource(String path) throws FileNotFoundException {
        String dir = "D:\\Workspace\\upwork\\gitblit-project\\gitblit\\src\\main\\resources\\static";
        return new FileInputStream(new File(dir, path));
////
//        ClassLoader loader = Thread.currentThread().getContextClassLoader();
//        return loader.getResourceAsStream(BASE_PATH + path);
    }
}
