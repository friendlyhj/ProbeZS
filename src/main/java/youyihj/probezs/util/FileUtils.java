package youyihj.probezs.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

/**
 * @author youyihj
 */
public class FileUtils {
    public static void createFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, Collections.singleton(content), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }
}
