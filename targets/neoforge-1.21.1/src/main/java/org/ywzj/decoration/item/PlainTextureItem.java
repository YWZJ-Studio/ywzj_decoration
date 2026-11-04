package org.ywzj.decoration.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

public class PlainTextureItem extends Item {

    public static final String TAG_TEXTURE = "textureLocation";

    public PlainTextureItem(Properties properties) {
        super(properties);
    }

    public ItemStack createInstance(ResourceLocation textureLocation) {
        ItemStack itemStack = new ItemStack(this);
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_TEXTURE, textureLocation.toString());
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return itemStack;
    }

    @Nullable
    public static ResourceLocation textureOf(ItemStack itemStack) {
        CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        return tag.contains(TAG_TEXTURE) ? ResourceLocation.tryParse(tag.getString(TAG_TEXTURE)) : null;
    }
}
