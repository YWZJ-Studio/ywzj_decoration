package org.ywzj.decoration.resource;

import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * One directory or zip on disk that provides decoration assets and data.
 * Discovery and validation are loader independent; loading it as a resource
 * pack is implemented by each target.
 */
public record DecorationPack(Path path, PackMeta meta) {

    private String displayPrefix() {
        return "assets/" + meta.getNamespace() + "/display/decoration/";
    }

    @SuppressWarnings("removal")
    public List<ResourceLocation> listDecorations() {
        String prefix = displayPrefix();
        List<ResourceLocation> decorations = new ArrayList<>();
        if (Files.isDirectory(path)) {
            Path displayDir = path.resolve(prefix);
            if (!Files.isDirectory(displayDir)) {
                return decorations;
            }
            try (Stream<Path> stream = Files.walk(displayDir)) {
                stream.filter(Files::isRegularFile)
                        .filter(file -> file.getFileName().toString().endsWith(".json"))
                        .forEach(file -> {
                            String relative = displayDir.relativize(file).toString().replace('\\', '/');
                            decorations.add(ResourceLocation.tryParse(meta.getNamespace() + ":"
                                    + relative.substring(0, relative.length() - ".json".length())));
                        });
            } catch (IOException exception) {
                org.ywzj.decoration.YwzjDecoration.LOGGER.error("Failed to list decorations of pack {}", path, exception);
            }
        } else if (path.toString().endsWith(".zip")) {
            try (ZipFile zipFile = new ZipFile(path.toFile())) {
                zipFile.stream()
                        .filter(entry -> !entry.isDirectory())
                        .map(ZipEntry::getName)
                        .filter(name -> name.startsWith(prefix) && name.endsWith(".json"))
                        .forEach(name -> {
                            String relative = name.substring(prefix.length());
                            decorations.add(ResourceLocation.tryParse(meta.getNamespace() + ":"
                                    + relative.substring(0, relative.length() - ".json".length())));
                        });
            } catch (IOException exception) {
                org.ywzj.decoration.YwzjDecoration.LOGGER.error("Failed to list decorations of pack {}", path, exception);
            }
        }
        decorations.sort(Comparator.comparing(ResourceLocation::toString));
        return decorations;
    }

    public boolean hasFile(String relativePath) {
        if (Files.isDirectory(path)) {
            return Files.isRegularFile(path.resolve(relativePath));
        } else if (path.toString().endsWith(".zip")) {
            try (ZipFile zipFile = new ZipFile(path.toFile())) {
                return zipFile.getEntry(relativePath) != null;
            } catch (IOException exception) {
                return false;
            }
        }
        return false;
    }
}
