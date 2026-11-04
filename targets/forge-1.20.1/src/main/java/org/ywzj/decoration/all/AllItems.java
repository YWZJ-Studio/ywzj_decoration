package org.ywzj.decoration.all;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.item.ForgeDecorationItem;
import org.ywzj.decoration.item.ForgePlainTextureItem;
import org.ywzj.decoration.registry.DecorationItems;
import org.ywzj.decoration.registry.DecorationRegistry;
import org.ywzj.decoration.resource.DecorationPack;
import org.ywzj.decoration.resource.DecorationPackManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AllItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, YwzjDecoration.MOD_ID);

    public static final RegistryObject<ForgePlainTextureItem> PLAIN_TEXTURE_ITEM =
            registerItem(AllTabs.Category.NONE, "plain_texture_item", () -> {
                ForgePlainTextureItem item = new ForgePlainTextureItem(new Item.Properties().stacksTo(1));
                DecorationRegistry.setPlainTextureItem(item);
                return item;
            });

    public static final RegistryObject<BlockItem> SEWING_MACHINE =
            registerItem(AllTabs.Category.MISC, "sewing_machine", () -> {
                BlockItem item = new BlockItem(AllBlocks.SEWING_MACHINE.get(), new Item.Properties());
                DecorationRegistry.setSewingMachineItem(item);
                return item;
            });

    public static <T extends Item> RegistryObject<T> registerItem(AllTabs.Category category, String name, Supplier<T> item) {
        RegistryObject<T> registryObject = ITEMS.register(name, item);
        if (AllTabs.Category.MISC.equals(category)) {
            AllTabs.MISC_ITEMS.add(registryObject);
        }
        return registryObject;
    }

    public static void register(IEventBus eventBus) {
        Map<String, DeferredRegister<Item>> registers = new LinkedHashMap<>();
        registers.put(YwzjDecoration.MOD_ID, ITEMS);
        for (DecorationPack pack : DecorationPackManager.INSTANCE.getDecorationPacks()) {
            for (ResourceLocation displayId : pack.listDecorations()) {
                DeferredRegister<Item> register = registers.computeIfAbsent(displayId.getNamespace(),
                        namespace -> DeferredRegister.create(ForgeRegistries.ITEMS, namespace));
                RegistryObject<ForgeDecorationItem> item = register.register(displayId.getPath(),
                        () -> {
                            ForgeDecorationItem decorationItem =
                                    new ForgeDecorationItem(displayId, new Item.Properties().stacksTo(1));
                            DecorationItems.register(displayId, decorationItem);
                            return decorationItem;
                        });
            }
        }
        registers.values().forEach(register -> register.register(eventBus));
    }
}
