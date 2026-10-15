package org.ywzj.decoration.all;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.item.DecorationItem;
import org.ywzj.decoration.item.PlainTextureItem;
import org.ywzj.decoration.resource.DecorationPackLoader;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AllItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, YwzjDecoration.MOD_ID);
    private static final Map<ResourceLocation, RegistryObject<DecorationItem>> DECORATIONS = new LinkedHashMap<>();

    public static final RegistryObject<PlainTextureItem> PLAIN_TEXTURE_ITEM =
            registerItem(AllTabs.Category.NONE, "plain_texture_item", () -> new PlainTextureItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<BlockItem> SEWING_MACHINE =
            registerItem(AllTabs.Category.MISC, "sewing_machine",
                    () -> new BlockItem(AllBlocks.SEWING_MACHINE.get(), new Item.Properties()));

    public static ItemStack createDecorationItemStack(ResourceLocation displayId) {
        RegistryObject<DecorationItem> item = DECORATIONS.get(displayId);
        return item == null ? ItemStack.EMPTY : item.get().getDefaultInstance();
    }

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
        for (DecorationPackLoader.DecorationPack pack : DecorationPackLoader.INSTANCE.getDecorationPacks()) {
            for (ResourceLocation displayId : pack.listDecorations()) {
                DeferredRegister<Item> register = registers.computeIfAbsent(displayId.getNamespace(),
                        namespace -> DeferredRegister.create(ForgeRegistries.ITEMS, namespace));
                RegistryObject<DecorationItem> item = register.register(displayId.getPath(),
                        () -> new DecorationItem(displayId, new Item.Properties().stacksTo(1)));
                DECORATIONS.put(displayId, item);
            }
        }
        registers.values().forEach(register -> register.register(eventBus));
    }

}
