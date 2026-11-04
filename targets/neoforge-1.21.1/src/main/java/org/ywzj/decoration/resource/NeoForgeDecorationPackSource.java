package org.ywzj.decoration.resource;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlags;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Exposes packs discovered by {@link DecorationPackManager} to NeoForge's pack
 * repository. NeoForge 1.21 replaced Forge's PathPackResources and
 * DelegatingPackResources, so this class implements the vanilla pack model
 * directly for both directory and zip packs.
 */
public enum NeoForgeDecorationPackSource implements RepositorySource {

    INSTANCE;

    private static final byte[] SEWN_ITEM_MODEL_JSON =
            "{\"parent\":\"ywzj_decoration:shared/sewn_decoration\"}".getBytes(StandardCharsets.UTF_8);

    @Override
    public void loadPacks(Consumer<Pack> onLoad) {
        for (DecorationPack decorationPack : DecorationPackManager.INSTANCE.getDecorationPacks()) {
            Set<ResourceLocation> itemModels = new LinkedHashSet<>();
            for (ResourceLocation displayId : decorationPack.listDecorations()) {
                itemModels.add(ResourceLocation.tryParse(
                        displayId.getNamespace() + ":models/item/" + displayId.getPath() + ".json"));
            }
            String id = "ywzj_decoration_resources_" + decorationPack.meta().getNamespace();
            PackLocationInfo location = new PackLocationInfo(id,
                    Component.translatable(decorationPack.meta().getTitle()),
                    PackSource.BUILT_IN,
                    Optional.empty());
            Pack.Metadata metadata = new Pack.Metadata(
                    Component.translatable(decorationPack.meta().getDescription()),
                    net.minecraft.server.packs.repository.PackCompatibility.COMPATIBLE,
                    FeatureFlags.VANILLA_SET,
                    List.of());
            Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {

                @Override
                public PackResources openPrimary(PackLocationInfo locationInfo) {
                    return new DecorationPackResources(locationInfo, decorationPack, itemModels);
                }

                @Override
                public PackResources openFull(PackLocationInfo locationInfo, Pack.Metadata packMetadata) {
                    return openPrimary(locationInfo);
                }
            };
            Pack pack = new Pack(location, supplier, metadata,
                    new PackSelectionConfig(true, Pack.Position.BOTTOM, false));
            onLoad.accept(pack);
        }
    }

    private static final class DecorationPackResources extends AbstractPackResources {

        private final DecorationPack pack;
        private final Set<ResourceLocation> itemModels;
        private final PathPackResources directoryResources;
        private final ZipFile zipFile;

        private DecorationPackResources(PackLocationInfo location, DecorationPack pack, Set<ResourceLocation> itemModels) {
            super(location);
            this.pack = pack;
            this.itemModels = itemModels;
            if (Files.isDirectory(pack.path())) {
                this.directoryResources = new PathPackResources(location, pack.path());
                this.zipFile = null;
            } else {
                this.directoryResources = null;
                try {
                    this.zipFile = new ZipFile(pack.path().toFile());
                } catch (IOException exception) {
                    throw new RuntimeException("Failed to open decoration pack " + pack.path(), exception);
                }
            }
        }

        @Override
        public IoSupplier<InputStream> getRootResource(String... paths) {
            if (directoryResources != null) {
                return directoryResources.getRootResource(paths);
            }
            String entryName = String.join("/", paths);
            ZipEntry entry = zipFile.getEntry(entryName);
            return entry == null ? null : () -> zipFile.getInputStream(entry);
        }

        @Override
        public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
            IoSupplier<InputStream> resource = read(type, location);
            if (resource == null && type == PackType.CLIENT_RESOURCES && itemModels.contains(location)) {
                return () -> new ByteArrayInputStream(SEWN_ITEM_MODEL_JSON);
            }
            return resource;
        }

        private IoSupplier<InputStream> read(PackType type, ResourceLocation location) {
            if (directoryResources != null) {
                return directoryResources.getResource(type, location);
            }
            String entryName = type.getDirectory() + "/" + location.getNamespace() + "/" + location.getPath();
            ZipEntry entry = zipFile.getEntry(entryName);
            return entry == null ? null : () -> zipFile.getInputStream(entry);
        }

        @Override
        public void listResources(PackType type, String namespace, String path, ResourceOutput resourceOutput) {
            if (directoryResources != null) {
                directoryResources.listResources(type, namespace, path, resourceOutput);
            } else {
                listZipResources(type, namespace, path, resourceOutput);
            }
            if (type == PackType.CLIENT_RESOURCES) {
                String prefix = path.isEmpty() ? "" : path + "/";
                for (ResourceLocation location : itemModels) {
                    if (location.getNamespace().equals(namespace) && location.getPath().startsWith(prefix)
                            && read(type, location) == null) {
                        resourceOutput.accept(location, () -> new ByteArrayInputStream(SEWN_ITEM_MODEL_JSON));
                    }
                }
            }
        }

        private void listZipResources(PackType type, String namespace, String path, ResourceOutput resourceOutput) {
            String prefix = type.getDirectory() + "/" + namespace + "/" + (path.isEmpty() ? "" : path + "/");
            List<? extends ZipEntry> entries = zipFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> entry.getName().startsWith(prefix))
                    .toList();
            for (ZipEntry entry : entries) {
                String relative = entry.getName().substring(prefix.length());
                ResourceLocation location = ResourceLocation.tryParse(namespace + ":" + relative);
                if (location != null) {
                    resourceOutput.accept(location, () -> zipFile.getInputStream(entry));
                }
            }
        }

        @Override
        public Set<String> getNamespaces(PackType type) {
            Set<String> namespaces = new HashSet<>();
            String root = type.getDirectory() + "/";
            if (directoryResources != null) {
                Path directory = pack.path().resolve(type.getDirectory());
                if (Files.isDirectory(directory)) {
                    try (Stream<Path> stream = Files.list(directory)) {
                        stream.filter(Files::isDirectory).forEach(child -> namespaces.add(child.getFileName().toString()));
                    } catch (IOException ignored) {
                    }
                }
            } else {
                for (ZipEntry entry : zipFile.stream().toList()) {
                    String name = entry.getName();
                    if (name.startsWith(root) && name.length() > root.length()) {
                        String remainder = name.substring(root.length());
                        int slash = remainder.indexOf('/');
                        if (slash > 0) {
                            namespaces.add(remainder.substring(0, slash));
                        }
                    }
                }
            }
            if (namespaces.isEmpty()) {
                namespaces.add(pack.meta().getNamespace());
            }
            return namespaces;
        }

        @Override
        public void close() {
            if (directoryResources != null) {
                directoryResources.close();
            }
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
