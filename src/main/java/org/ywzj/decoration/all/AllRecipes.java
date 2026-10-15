package org.ywzj.decoration.all;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.recipe.SewingRecipe;
import org.ywzj.decoration.recipe.SewingRecipeSerializer;

public class AllRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, YwzjDecoration.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, YwzjDecoration.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SewingRecipe>> SEWING_SERIALIZER =
            SERIALIZERS.register("sewing", SewingRecipeSerializer::new);
    public static final RegistryObject<RecipeType<SewingRecipe>> SEWING = TYPES.register("sewing", () -> new RecipeType<>() {
        @Override
        public String toString() {
            return YwzjDecoration.MOD_ID + ":sewing";
        }
    });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
