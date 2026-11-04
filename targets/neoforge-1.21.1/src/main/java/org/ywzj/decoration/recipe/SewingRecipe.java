package org.ywzj.decoration.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.ywzj.decoration.item.DecorationItem;
import org.ywzj.decoration.registry.DecorationRegistry;

import javax.annotation.Nullable;
import java.util.List;

public class SewingRecipe implements Recipe<RecipeInput> {

    private final ItemStack result;
    private final List<SewingIngredient> materials;

    public SewingRecipe(ItemStack result, List<SewingIngredient> materials) {
        if (!(result.getItem() instanceof DecorationItem) || result.isEmpty()
                || result.getCount() > result.getMaxStackSize()) {
            throw new IllegalArgumentException("Sewing result must be a registered decoration item with a valid count");
        }
        if (materials.isEmpty() || materials.size() > 64) {
            throw new IllegalArgumentException("Sewing recipe requires between 1 and 64 materials");
        }
        this.result = result.copy();
        this.materials = List.copyOf(materials);
    }

    public List<SewingIngredient> getMaterials() {
        return materials;
    }

    public ItemStack getResultStack() {
        return result.copy();
    }

    public ResourceLocation getDisplayId() {
        return ((DecorationItem) result.getItem()).getDisplayId();
    }

    @Nullable
    public int[] planConsumption(Inventory inventory) {
        int[] available = new int[inventory.items.size()];
        int[] required = materials.stream().mapToInt(SewingIngredient::count).toArray();
        boolean[][] matches = new boolean[available.length][required.length];
        for (int slot = 0; slot < available.length; slot++) {
            ItemStack stack = inventory.items.get(slot);
            available[slot] = stack.getCount();
            for (int material = 0; material < required.length; material++) {
                matches[slot][material] = materials.get(material).ingredient().test(stack);
            }
        }
        return MaterialAllocation.plan(available, required, matches);
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DecorationRegistry.sewingRecipeSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return DecorationRegistry.sewingRecipeType();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
