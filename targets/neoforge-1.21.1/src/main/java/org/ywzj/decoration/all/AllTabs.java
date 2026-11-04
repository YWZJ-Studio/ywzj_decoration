package org.ywzj.decoration.all;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.item.PlainTextureItem;
import org.ywzj.decoration.registry.DecorationItems;
import org.ywzj.decoration.registry.DecorationRegistry;
import org.ywzj.decoration.resource.DecorationPack;
import org.ywzj.decoration.resource.DecorationPackManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AllTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, YwzjDecoration.MOD_ID);
    public static final List<Supplier<? extends ItemLike>> MISC_ITEMS = new ArrayList<>();

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_MISC = TABS.register("tab_misc", () ->
            CreativeModeTab
                    .builder()
                    .title(Component.translatable("tab.misc"))
                    .icon(() -> DecorationRegistry.sewingMachineItem().getDefaultInstance())
                    .displayItems((displayParams, output) ->
                            MISC_ITEMS.forEach(itemLike -> output.accept(itemLike.get())))
                    .build());

    public static void register(IEventBus eventBus) {
        DecorationPackManager.INSTANCE.getDecorationPacks().forEach(decorationPack -> {
            String namespace = decorationPack.meta().getNamespace();
            List<ResourceLocation> decorations = decorationPack.listDecorations();
            TABS.register("tab_" + namespace, () ->
                    CreativeModeTab
                            .builder()
                            .title(Component.translatable(decorationPack.meta().getTitle()))
                            .icon(packIcon(decorationPack, decorations))
                            .displayItems((displayParams, output) ->
                                    decorations.forEach(displayId -> output.accept(DecorationItems.createDecorationItemStack(displayId))))
                            .build());
        });
        TABS.register(eventBus);
    }

    private static Supplier<ItemStack> packIcon(DecorationPack decorationPack, List<ResourceLocation> decorations) {
        String namespace = decorationPack.meta().getNamespace();
        if (decorationPack.hasFile("assets/" + namespace + "/textures/tab.png")) {
            return () -> {
                Item plainTexture = DecorationRegistry.plainTextureItem();
                if (plainTexture instanceof PlainTextureItem textureItem) {
                    return textureItem.createInstance(YwzjDecoration.resourceLocation(namespace + ":textures/tab.png"));
                }
                return plainTexture.getDefaultInstance();
            };
        }
        return () -> decorations.isEmpty()
                ? DecorationRegistry.sewingMachineItem().getDefaultInstance()
                : DecorationItems.createDecorationItemStack(decorations.get(0));
    }

    public enum Category {
        MISC,
        NONE
    }
}
