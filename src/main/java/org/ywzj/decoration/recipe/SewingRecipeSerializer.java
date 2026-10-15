package org.ywzj.decoration.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

import java.util.ArrayList;
import java.util.List;

public class SewingRecipeSerializer implements RecipeSerializer<SewingRecipe> {

    @Override
    public SewingRecipe fromJson(ResourceLocation id, JsonObject json) {
        ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
        List<SewingIngredient> materials = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "materials")) {
            JsonObject material = GsonHelper.convertToJsonObject(element, "material");
            Ingredient ingredient = Ingredient.fromJson(material.has("ingredient") ? material.get("ingredient") : material);
            materials.add(new SewingIngredient(ingredient, GsonHelper.getAsInt(material, "count", 1)));
        }
        return new SewingRecipe(id, result, materials);
    }

    @Override
    public SewingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        ItemStack result = buffer.readItem();
        int size = buffer.readVarInt();
        if (size < 1 || size > 64) {
            throw new IllegalArgumentException("Invalid sewing material count: " + size);
        }
        List<SewingIngredient> materials = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            materials.add(new SewingIngredient(Ingredient.fromNetwork(buffer), buffer.readVarInt()));
        }
        return new SewingRecipe(id, result, materials);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, SewingRecipe recipe) {
        buffer.writeItem(recipe.getResultItem(RegistryAccess.EMPTY));
        buffer.writeVarInt(recipe.getMaterials().size());
        for (SewingIngredient material : recipe.getMaterials()) {
            material.ingredient().toNetwork(buffer);
            buffer.writeVarInt(material.count());
        }
    }
}
