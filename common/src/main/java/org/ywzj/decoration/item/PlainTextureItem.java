package org.ywzj.decoration.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlainTextureItem extends Item {

    public static final String TAG_TEXTURE = "textureLocation";

    public PlainTextureItem(Properties properties) {
        super(properties);
    }

    public ItemStack createInstance(ResourceLocation textureLocation) {
        ItemStack itemStack = new ItemStack(this);
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putString(TAG_TEXTURE, textureLocation.toString());
        itemStack.setTag(tag);
        return itemStack;
    }

}
