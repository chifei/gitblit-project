import com.gitblit.ConsoleServer;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.stream.Stream;

/**
 * @author chi
 */
public class Main {
    static {
        System.setProperty("log4j.rootLogger", "DEBUG");
    }

    public static void main(String[] args) {
        new Main().init(args);
    }

    private void init(String[] args) {
        String dir = System.getProperty("user.home");
        File home = new File(dir, ".gitblit");
        if (!home.isDirectory()) {
            home.delete();
            home.mkdirs();
        }

        if (!isInitialized(home)) {
            URL files = Resources.getResource("files.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(files.openStream()))) {
                String line = reader.readLine();
                while (line != null) {
                    URL resource = Resources.getResource(line);
                    File targetFile = new File(home, line);
                    Files.createParentDirs(targetFile);
                    try (InputStream inputStream = resource.openStream(); OutputStream outputStream = new FileOutputStream(targetFile)) {
                        ByteStreams.copy(inputStream, outputStream);
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String[] a = {"--baseFolder", new File(home, "data").getAbsolutePath()};
        String[] arguments = Stream.of(args, a).flatMap(Stream::of).toArray(String[]::new);
        ConsoleServer.main(arguments);
    }

    private boolean isInitialized(File dir) {
        return new File(dir, "data").exists();
    }
}
