package org.ywzj.decoration.all;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.item.DecorationItem;
import org.ywzj.decoration.item.PlainTextureItem;
import org.ywzj.decoration.registry.DecorationItems;
import org.ywzj.decoration.registry.DecorationRegistry;
import org.ywzj.decoration.resource.DecorationPack;
import org.ywzj.decoration.resource.DecorationPackManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AllItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, YwzjDecoration.MOD_ID);

    public static final DeferredHolder<Item, PlainTextureItem> PLAIN_TEXTURE_ITEM =
            registerItem(AllTabs.Category.NONE, "plain_texture_item", () -> {
                PlainTextureItem item = new PlainTextureItem(new Item.Properties().stacksTo(1));
                DecorationRegistry.setPlainTextureItem(item);
                return item;
            });

    public static final DeferredHolder<Item, BlockItem> SEWING_MACHINE =
            registerItem(AllTabs.Category.MISC, "sewing_machine", () -> {
                BlockItem item = new BlockItem(AllBlocks.SEWING_MACHINE.get(), new Item.Properties());
                DecorationRegistry.setSewingMachineItem(item);
                return item;
            });

    public static <T extends Item> DeferredHolder<Item, T> registerItem(AllTabs.Category category, String name, Supplier<T> item) {
        DeferredHolder<Item, T> holder = ITEMS.register(name, item);
        if (AllTabs.Category.MISC.equals(category)) {
            AllTabs.MISC_ITEMS.add(holder);
        }
        return holder;
    }

    public static void register(IEventBus eventBus) {
        Map<String, DeferredRegister<Item>> registers = new LinkedHashMap<>();
        registers.put(YwzjDecoration.MOD_ID, ITEMS);
        for (DecorationPack pack : DecorationPackManager.INSTANCE.getDecorationPacks()) {
            for (ResourceLocation displayId : pack.listDecorations()) {
                DeferredRegister<Item> register = registers.computeIfAbsent(displayId.getNamespace(),
                        namespace -> DeferredRegister.create(Registries.ITEM, namespace));
                register.register(displayId.getPath(), () -> {
                    DecorationItem item = new DecorationItem(displayId, new Item.Properties().stacksTo(1));
                    DecorationItems.register(displayId, item);
                    return item;
                });
            }
        }
        registers.values().forEach(register -> register.register(eventBus));
    }
}
