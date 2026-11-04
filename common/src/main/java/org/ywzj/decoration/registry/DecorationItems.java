package org.ywzj.decoration.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Decoration items are registered dynamically, one per decoration display
 * found in the scanned packs. Targets register them and publish the instance
 * here so shared block logic can create matching drops.
 */
public final class DecorationItems {

    private static final Map<ResourceLocation, Item> DECORATIONS = new LinkedHashMap<>();

    private DecorationItems() {}

    public static synchronized void register(ResourceLocation displayId, Item item) {
        DECORATIONS.put(displayId, item);
    }

    public static synchronized Map<ResourceLocation, Item> decorations() {
        return Map.copyOf(DECORATIONS);
    }

    public static ItemStack createDecorationItemStack(@Nullable ResourceLocation displayId) {
        if (displayId == null) {
            return ItemStack.EMPTY;
        }
        Item item;
        synchronized (DecorationItems.class) {
            item = DECORATIONS.get(displayId);
        }
        return item == null ? ItemStack.EMPTY : item.getDefaultInstance();
    }
}
