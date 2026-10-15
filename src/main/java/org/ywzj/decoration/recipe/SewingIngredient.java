package org.ywzj.decoration.recipe;

import net.minecraft.world.item.crafting.Ingredient;

public record SewingIngredient(Ingredient ingredient, int count) {

    public SewingIngredient {
        if (ingredient == null || ingredient.isEmpty() || count < 1 || count > 4096) {
            throw new IllegalArgumentException("Sewing material must have an ingredient and count between 1 and 4096");
        }
    }
}
