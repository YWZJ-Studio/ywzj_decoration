package org.ywzj.decoration.resource;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import cpw.mods.jarhandling.SecureJar;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.resource.DelegatingPackResources;
import net.minecraftforge.resource.PathPackResources;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.util.GetJarResources;
import org.ywzj.decoration.util.GsonUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public enum DecorationPackLoader implements RepositorySource {

    INSTANCE;
    private static final Marker MARKER = MarkerManager.getMarker("DecorationPackFinder");
    public PackType packType;
    private static final String PACK_META_FILE = "decoration_pack.meta.json";
    private static final String DEFAULT_DECORATION_PACK_PATH = "default_decoration";
    private static final byte[] SEWN_ITEM_MODEL_JSON =
            "{\"parent\":\"ywzj_decoration:shared/sewn_decoration\"}".getBytes(StandardCharsets.UTF_8);
    private static final Path DECORATION_PACKS_PATH = FMLPaths.GAMEDIR.get().resolve("limitless_decoration");
    private static final Path DECORATION_PACKS_BACKUP_PATH = FMLPaths.GAMEDIR.get().resolve("limitless_decoration_backup");
    private List<DecorationPack> decorationPacks = List.of();

    static {
        try {
            File folder = DECORATION_PACKS_PATH.toFile();
            if (!folder.isDirectory()) {
                Files.createDirectories(folder.toPath());
            }
            folder = DECORATION_PACKS_BACKUP_PATH.toFile();
            if (!folder.isDirectory()) {
                Files.createDirectories(folder.toPath());
            }
        } catch (Exception exception) {
            YwzjDecoration.LOGGER.warn(MARKER, "Failed to init decoration resource directory...", exception);
        }
    }

    @Override
    public void loadPacks(Consumer<Pack> pOnLoad) {
        for (Pack pack : decorationPacksAsResource()) {
            pOnLoad.accept(pack);
        }
    }

    public void scanDecorationPacks() {
        checkDefaultDecorationPack();
        YwzjDecoration.LOGGER.info(MARKER, "Start scanning for decoration packs in {}", DECORATION_PACKS_PATH);
        decorationPacks = scanDecorationPacks(DECORATION_PACKS_PATH);
        YwzjDecoration.LOGGER.info(MARKER, "Found {} possible decoration pack(s)", decorationPacks.size());
    }

    private void checkDefaultDecorationPack() {
        Path defaultDecorationPackPath = DECORATION_PACKS_PATH.resolve(DEFAULT_DECORATION_PACK_PATH);
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
                        DECORATION_PACKS_BACKUP_PATH.resolve(DEFAULT_DECORATION_PACK_PATH + "_" + timestamp));
                GetJarResources.deleteFiles(defaultDecorationPackPath);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        GetJarResources.copyModDirectory("/" + DEFAULT_DECORATION_PACK_PATH, defaultDecorationPackPath);
    }

    private List<Pack> decorationPacksAsResource() {
        List<Pack> packs = new ArrayList<>();
        List<PathPackResources> extensionPacks = new ArrayList<>();
        for (DecorationPack decorationPack : decorationPacks) {
            Set<ResourceLocation> itemModels = new LinkedHashSet<>();
            for (ResourceLocation displayId : decorationPack.listDecorations()) {
                itemModels.add(new ResourceLocation(displayId.getNamespace(), "models/item/" + displayId.getPath() + ".json"));
            }
            PathPackResources packResources = new PathPackResources(decorationPack.meta.getNamespace(), false, decorationPack.path) {

                private final SecureJar secureJar = SecureJar.from(decorationPack.path);

                protected Path resolve(String... paths) {
                    if (paths.length < 1) {
                        throw new IllegalArgumentException("Missing path");
                    } else {
                        return this.secureJar.getPath(String.join("/", paths));
                    }
                }

                public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
                    IoSupplier<InputStream> resource = super.getResource(type, location);
                    if (resource == null && type == PackType.CLIENT_RESOURCES && itemModels.contains(location)) {
                        return () -> new ByteArrayInputStream(SEWN_ITEM_MODEL_JSON);
                    }
                    return resource;
                }

                public void listResources(PackType type, String namespace, String path, ResourceOutput resourceOutput) {
                    super.listResources(type, namespace, path, resourceOutput);
                    if (type == PackType.CLIENT_RESOURCES) {
                        String prefix = path.isEmpty() ? "" : path + "/";
                        for (ResourceLocation location : itemModels) {
                            if (location.getNamespace().equals(namespace) && location.getPath().startsWith(prefix)
                                    && super.getResource(type, location) == null) {
                                resourceOutput.accept(location, () -> new ByteArrayInputStream(SEWN_ITEM_MODEL_JSON));
                            }
                        }
                    }
                }

            };
            extensionPacks.add(packResources);
            Pack pack = Pack.readMetaAndCreate("ywzj_decoration_resources_" + decorationPack.meta.getNamespace(),
                    Component.translatable(decorationPack.meta.getTitle()),
                    true,
                    (id) -> new DelegatingPackResources(id,
                            false,
                            new PackMetadataSection(Component.translatable(decorationPack.meta.getDescription()),
                                    SharedConstants.getCurrentVersion().getPackVersion(packType)), extensionPacks) {
                        public IoSupplier<InputStream> getRootResource(String... paths) {
                            if (paths.length == 1 && paths[0].equals("pack.png")) {
                                return packResources.getRootResource("pack.png");
                            }
                            return null;
                        }
                    }, packType, Pack.Position.BOTTOM, PackSource.BUILT_IN);
            packs.add(pack);
        }
        return packs;
    }

    private static DecorationPack fromDirPath(Path path) {
        Path packMetaPath = path.resolve(PACK_META_FILE);
        try (InputStream stream = Files.newInputStream(packMetaPath)) {
            PackMeta packMeta = GsonUtil.GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PackMeta.class);
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

    private static List<DecorationPack> scanDecorationPacks(Path path) {
        List<DecorationPack> decorationPacks = new ArrayList<>();
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
                            YwzjDecoration.LOGGER.error(MARKER, "- {}, Duplicated namespace: {}", decorationPack.path.getFileName(),
                                    decorationPack.meta.getNamespace());
                            continue;
                        }
                        namespaces.add(decorationPack.meta().getNamespace());
                        YwzjDecoration.LOGGER.info(MARKER, "- {}, Main namespace: {}", decorationPack.path.getFileName(),
                                decorationPack.meta.getNamespace());
                        decorationPacks.add(decorationPack);
                    }
                } catch (RuntimeException runtimeException) {
                    throw new RuntimeException("Failed to load decoration pack: " + entry + "\n" + runtimeException.getMessage());
                }
            }
        } catch (IOException ioException) {
            YwzjDecoration.LOGGER.error(MARKER, "Failed to scan decoration packs from {}. Error: {}", path, ioException);
        }
        return decorationPacks;
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
        return ModList.get().getModContainerById(modId).map(mod -> {
            ArtifactVersion modVersion = mod.getModInfo().getVersion();
            return versionRange.containsVersion(modVersion);
        }).orElse(false);
    }

    public List<DecorationPack> getDecorationPacks() {
        return decorationPacks;
    }

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
                                decorations.add(new ResourceLocation(meta.getNamespace(),
                                        relative.substring(0, relative.length() - ".json".length())));
                            });
                } catch (IOException exception) {
                    YwzjDecoration.LOGGER.error("Failed to list decorations of pack {}", path, exception);
                }
            } else if (path.toString().endsWith(".zip")) {
                try (ZipFile zipFile = new ZipFile(path.toFile())) {
                    zipFile.stream()
                            .filter(entry -> !entry.isDirectory())
                            .map(ZipEntry::getName)
                            .filter(name -> name.startsWith(prefix) && name.endsWith(".json"))
                            .forEach(name -> {
                                String relative = name.substring(prefix.length());
                                decorations.add(new ResourceLocation(meta.getNamespace(),
                                        relative.substring(0, relative.length() - ".json".length())));
                            });
                } catch (IOException exception) {
                    YwzjDecoration.LOGGER.error("Failed to list decorations of pack {}", path, exception);
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

}
