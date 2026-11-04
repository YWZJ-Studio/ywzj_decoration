package org.ywzj.decoration.util;

import org.ywzj.decoration.YwzjDecoration;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public final class GetJarResources {

    private GetJarResources() {}

    public static void copyModDirectory(String sourcePath, Path targetPath) {
        URL url = YwzjDecoration.class.getResource(sourcePath);
        try {
            if (url != null) {
                copyFolder(url.toURI(), targetPath);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (URISyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Nullable
    public static InputStream readModFile(String filePath) {
        URL url = YwzjDecoration.class.getResource(filePath);
        try {
            if (url != null) {
                return url.openStream();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static void copyFolder(URI sourceURI, Path targetPath) throws IOException {
        if (Files.isDirectory(targetPath)) {
            deleteFiles(targetPath);
        }
        try (Stream<Path> stream = Files.walk(Paths.get(sourceURI), Integer.MAX_VALUE)) {
            stream.forEach(source -> {
                Path target = targetPath.resolve(sourceURI.relativize(source.toUri()).toString());
                try {
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });
        }
    }

    public static void deleteFiles(Path targetPath) throws IOException {
        Files.walkFileTree(targetPath, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

}
