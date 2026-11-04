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
import net.minecraftforge.resource.DelegatingPackResources;
import net.minecraftforge.resource.PathPackResources;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.util.GsonUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Turns the loader-independent packs discovered by
 * {@link DecorationPackManager} into Forge resource packs.
 */
public enum ForgeDecorationPackSource implements RepositorySource {

    INSTANCE;

    private static final Marker MARKER = MarkerManager.getMarker("DecorationPackFinder");
    private static final byte[] SEWN_ITEM_MODEL_JSON =
            "{\"parent\":\"ywzj_decoration:shared/sewn_decoration\"}".getBytes(StandardCharsets.UTF_8);

    @Override
    public void loadPacks(Consumer<Pack> onLoad) {
        for (Pack pack : decorationPacksAsResource()) {
            onLoad.accept(pack);
        }
    }

    private List<Pack> decorationPacksAsResource() {
        List<Pack> packs = new ArrayList<>();
        List<PathPackResources> extensionPacks = new ArrayList<>();
        PackType packType = DecorationPackManager.INSTANCE.packType();
        for (DecorationPack decorationPack : DecorationPackManager.INSTANCE.getDecorationPacks()) {
            Set<ResourceLocation> itemModels = new LinkedHashSet<>();
            for (ResourceLocation displayId : decorationPack.listDecorations()) {
                itemModels.add(new ResourceLocation(displayId.getNamespace(), "models/item/" + displayId.getPath() + ".json"));
            }
            PathPackResources packResources = new PathPackResources(decorationPack.meta().getNamespace(), false, decorationPack.path()) {

                private final SecureJar secureJar = SecureJar.from(decorationPack.path());

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
            Pack pack = Pack.readMetaAndCreate("ywzj_decoration_resources_" + decorationPack.meta().getNamespace(),
                    Component.translatable(decorationPack.meta().getTitle()),
                    true,
                    (id) -> new DelegatingPackResources(id,
                            false,
                            new PackMetadataSection(Component.translatable(decorationPack.meta().getDescription()),
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
}
