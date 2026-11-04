package org.ywzj.decoration.resource;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.platform.DecorationPlatforms;
import org.ywzj.decoration.util.GetJarResources;
import org.ywzj.decoration.util.GsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Scans the game directory for decoration packs. The same logic runs on both
 * sides; only the call sites that build loader resource packs live in targets.
 */
public enum DecorationPackManager {

    INSTANCE;

    private static final Marker MARKER = MarkerManager.getMarker("DecorationPackFinder");
    private static final String PACK_META_FILE = "decoration_pack.meta.json";
    private static final String DEFAULT_DECORATION_PACK_PATH = "default_decoration";
    private static final String PACKS_DIRECTORY = "limitless_decoration";
    private static final String BACKUP_DIRECTORY = "limitless_decoration_backup";

    private PackType packType = PackType.SERVER_DATA;
    private Path packsPath;
    private Path backupPath;
    private List<DecorationPack> decorationPacks = List.of();

    public void initialize(PackType type) {
        this.packType = type;
        Path gameDirectory = DecorationPlatforms.get().gameDirectory();
        this.packsPath = gameDirectory.resolve(PACKS_DIRECTORY);
        this.backupPath = gameDirectory.resolve(BACKUP_DIRECTORY);
        try {
            Files.createDirectories(packsPath);
            Files.createDirectories(backupPath);
        } catch (IOException exception) {
            YwzjDecoration.LOGGER.warn(MARKER, "Failed to init decoration resource directory...", exception);
        }
        scanDecorationPacks();
    }

    public PackType packType() {
        return packType;
    }

    public void scanDecorationPacks() {
        if (packsPath == null) {
            throw new IllegalStateException("DecorationPackManager has not been initialized");
        }
        checkDefaultDecorationPack();
        YwzjDecoration.LOGGER.info(MARKER, "Start scanning for decoration packs in {}", packsPath);
        decorationPacks = scan(packsPath);
        YwzjDecoration.LOGGER.info(MARKER, "Found {} possible decoration pack(s)", decorationPacks.size());
    }

    public List<DecorationPack> getDecorationPacks() {
        return decorationPacks;
    }

    private void checkDefaultDecorationPack() {
        Path defaultDecorationPackPath = packsPath.resolve(DEFAULT_DECORATION_PACK_PATH);
        if (Files.isDirectory(defaultDecorationPackPath)) {
            try (InputStream streamExist = Files.newInputStream(defaultDecorationPackPath.resolve(PACK_META_FILE))) {
                PackMeta packMetaExist = GsonUtil.GSON.fromJson(new InputStreamReader(streamExist, StandardCharsets.UTF_8), PackMeta.class);
                if (packMetaExist != null) {
                    String versionExist = packMetaExist.getVersion();
                    try (InputStream streamJar = GetJarResources.readModFile("/" + DEFAULT_DECORATION_PACK_PATH + "/" + PACK_META_FILE)) {
                        PackMeta packMetaJar = GsonUtil.GSON.fromJson(new InputStreamReader(streamJar, StandardCharsets.UTF_8), PackMeta.class);
                        if (packMetaJar != null) {
                            String versionJar = packMetaJar.getVersion();
                            if (versionExist.compareTo(versionJar) >= 0) {
                                return;
                            }
                        }
                    }
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                String timestamp = LocalDateTime.now().format(formatter);
                GetJarResources.copyFolder(defaultDecorationPackPath.toUri(),
                        backupPath.resolve(DEFAULT_DECORATION_PACK_PATH + "_" + timestamp));
                GetJarResources.deleteFiles(defaultDecorationPackPath);
            } catch (Exception exception) {
                YwzjDecoration.LOGGER.warn(MARKER, "Failed to refresh the default decoration pack", exception);
            }
        }
        GetJarResources.copyModDirectory("/" + DEFAULT_DECORATION_PACK_PATH, defaultDecorationPackPath);
    }

    private static DecorationPack fromDirPath(Path path) {
        Path packMetaPath = path.resolve(PACK_META_FILE);
        try (InputStream stream = Files.newInputStream(packMetaPath)) {
            PackMeta packMeta = GsonUtil.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);
            validatePackMeta(packMeta);
            return new DecorationPack(path, packMeta);
        } catch (IOException | JsonSyntaxException | JsonIOException | InvalidVersionSpecificationException exception) {
            YwzjDecoration.LOGGER.warn(MARKER, "Failed to read info json: {}", path.getFileName());
            YwzjDecoration.LOGGER.warn(exception.getMessage());
        }
        return null;
    }

    private static DecorationPack fromZipPath(Path path) {
        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            ZipEntry packMetaEntry = zipFile.getEntry(PACK_META_FILE);
            if (packMetaEntry == null) {
                YwzjDecoration.LOGGER.error(MARKER, "Failed to load extension from ZIP {}. Error: {}", path.getFileName(),
                        "No " + PACK_META_FILE + " found");
                return null;
            }
            try (InputStream stream = zipFile.getInputStream(packMetaEntry)) {
                PackMeta packMeta = GsonUtil.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);
                validatePackMeta(packMeta);
                return new DecorationPack(path, packMeta);
            } catch (IOException | JsonSyntaxException | JsonIOException | InvalidVersionSpecificationException exception) {
                YwzjDecoration.LOGGER.error(MARKER, "Failed to load extension from ZIP {}. Error: {}", path.getFileName(), exception);
                return null;
            }
        } catch (IOException exception) {
            YwzjDecoration.LOGGER.error(MARKER, "Failed to load extension from ZIP {}. Error: {}", path.getFileName(), exception);
            return null;
        }
    }

    private static void validatePackMeta(PackMeta packMeta) throws InvalidVersionSpecificationException {
        if (packMeta == null) {
            throw new RuntimeException("Failed to read packMeta json");
        }
        if (packMeta.getNamespace() == null) {
            throw new RuntimeException("Failed to read namespace");
        }
        if (packMeta.getDependencies() != null) {
            List<String> incompatible = modVersionAllMatch(packMeta);
            if (!incompatible.isEmpty()) {
                throw new RuntimeException("Mod version mismatch: " + String.join(", ", incompatible));
            }
        }
    }

    private static List<DecorationPack> scan(Path path) {
        List<DecorationPack> packs = new ArrayList<>();
        Set<String> namespaces = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                try {
                    DecorationPack decorationPack = null;
                    if (Files.isDirectory(entry)) {
                        decorationPack = fromDirPath(entry);
                    } else if (entry.toString().endsWith(".zip")) {
                        decorationPack = fromZipPath(entry);
                    }
                    if (decorationPack != null) {
                        if (namespaces.contains(decorationPack.meta().getNamespace())) {
                            YwzjDecoration.LOGGER.error(MARKER, "- {}, Duplicated namespace: {}", decorationPack.path().getFileName(),
                                    decorationPack.meta().getNamespace());
                            continue;
                        }
                        namespaces.add(decorationPack.meta().getNamespace());
                        YwzjDecoration.LOGGER.info(MARKER, "- {}, Main namespace: {}", decorationPack.path().getFileName(),
                                decorationPack.meta().getNamespace());
                        packs.add(decorationPack);
                    }
                } catch (RuntimeException runtimeException) {
                    throw new RuntimeException("Failed to load decoration pack: " + entry + "\n" + runtimeException.getMessage());
                }
            }
        } catch (IOException ioException) {
            YwzjDecoration.LOGGER.error(MARKER, "Failed to scan decoration packs from {}. Error: {}", path, ioException);
        }
        return packs;
    }

    private static List<String> modVersionAllMatch(PackMeta info) throws InvalidVersionSpecificationException {
        HashMap<String, String> dependencies = info.getDependencies();
        List<String> incompatible = new ArrayList<>();
        for (Map.Entry<String, String> modIdAndVersion : dependencies.entrySet()) {
            if (!modVersionMatch(modIdAndVersion.getKey(), modIdAndVersion.getValue())) {
                incompatible.add(modIdAndVersion.getKey() + ": " + modIdAndVersion.getValue());
            }
        }
        return incompatible;
    }

    private static boolean modVersionMatch(String modId, String version) throws InvalidVersionSpecificationException {
        VersionRange versionRange = VersionRange.createFromVersionSpec(version);
        String installed = DecorationPlatforms.get().modVersion(modId);
        return installed != null && versionRange.containsVersion(new DefaultArtifactVersion(installed));
    }
}
