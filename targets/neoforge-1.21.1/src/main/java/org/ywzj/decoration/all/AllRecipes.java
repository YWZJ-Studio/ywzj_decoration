package org.ywzj.decoration.all;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.recipe.SewingRecipe;
import org.ywzj.decoration.recipe.SewingRecipeSerializer;
import org.ywzj.decoration.registry.DecorationRegistry;

public class AllRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, YwzjDecoration.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, YwzjDecoration.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SewingRecipe>> SEWING_SERIALIZER =
            SERIALIZERS.register("sewing", () -> {
                SewingRecipeSerializer serializer = new SewingRecipeSerializer();
                DecorationRegistry.setSewingRecipeSerializer(serializer);
                return serializer;
            });
    public static final DeferredHolder<RecipeType<?>, RecipeType<SewingRecipe>> SEWING = TYPES.register("sewing", () -> {
        RecipeType<SewingRecipe> type = new RecipeType<>() {
            @Override
            public String toString() {
                return YwzjDecoration.MOD_ID + ":sewing";
            }
        };
        DecorationRegistry.setSewingRecipeType(type);
        return type;
    });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
