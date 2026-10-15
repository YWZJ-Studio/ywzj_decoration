package org.ywzj.decoration.all;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.resource.DecorationPackLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AllTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, YwzjDecoration.MOD_ID);
    public static final List<Supplier<? extends ItemLike>> MISC_ITEMS = new ArrayList<>();

    public static final RegistryObject<CreativeModeTab> TAB_MISC = TABS.register("tab_misc", () ->
            CreativeModeTab
                    .builder()
                    .title(Component.translatable("tab.misc"))
                    .icon(() -> AllItems.SEWING_MACHINE.get().getDefaultInstance())
                    .displayItems((displayParams, output) ->
                            MISC_ITEMS.forEach(itemLike -> output.accept(itemLike.get())))
                    .build());

    public static void register(IEventBus eventBus) {
        DecorationPackLoader.INSTANCE.getDecorationPacks().forEach(decorationPack -> {
            String namespace = decorationPack.meta().getNamespace();
            List<ResourceLocation> decorations = decorationPack.listDecorations();
            TABS.register("tab_" + namespace, () ->
                    CreativeModeTab
                            .builder()
                            .title(Component.translatable(decorationPack.meta().getTitle()))
                            .icon(packIcon(decorationPack, decorations))
                            .displayItems((displayParams, output) ->
                                    decorations.forEach(displayId -> output.accept(AllItems.createDecorationItemStack(displayId))))
                            .build());
        });
        TABS.register(eventBus);
    }

    private static Supplier<ItemStack> packIcon(DecorationPackLoader.DecorationPack decorationPack, List<ResourceLocation> decorations) {
        String namespace = decorationPack.meta().getNamespace();
        if (decorationPack.hasFile("assets/" + namespace + "/textures/tab.png")) {
            return () -> AllItems.PLAIN_TEXTURE_ITEM.get()
                    .createInstance(YwzjDecoration.resourceLocation(namespace + ":textures/tab.png"));
        }
        return () -> decorations.isEmpty()
                ? AllItems.SEWING_MACHINE.get().getDefaultInstance()
                : AllItems.createDecorationItemStack(decorations.get(0));
    }

    public enum Category {
        MISC,
        NONE
    }

}
