package org.ywzj.decoration.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.blockentity.SewingMachineBlockEntity;
import org.ywzj.decoration.menu.SewingMachineMenu;
import org.ywzj.decoration.recipe.SewingRecipe;

/**
 * Handles to the objects each target registers. Targets fill these while
 * registering; shared code only reads them.
 */
public final class DecorationRegistry {

    private static Block decorationBlock;
    private static Block sewingMachine;
    private static BlockEntityType<DecorationBlockEntity> decorationBlockEntity;
    private static BlockEntityType<SewingMachineBlockEntity> sewingMachineBlockEntity;
    private static Item plainTextureItem;
    private static Item sewingMachineItem;
    private static MenuType<?> sewingMachineMenu;
    private static RecipeType<SewingRecipe> sewingRecipeType;
    private static RecipeSerializer<SewingRecipe> sewingRecipeSerializer;

    private DecorationRegistry() {}

    public static Block decorationBlock() {
        return require(decorationBlock, "decoration block");
    }

    public static void setDecorationBlock(Block block) {
        decorationBlock = block;
    }

    public static Block sewingMachine() {
        return require(sewingMachine, "sewing machine block");
    }

    public static void setSewingMachine(Block block) {
        sewingMachine = block;
    }

    public static BlockEntityType<DecorationBlockEntity> decorationBlockEntity() {
        return require(decorationBlockEntity, "decoration block entity type");
    }

    public static void setDecorationBlockEntity(BlockEntityType<DecorationBlockEntity> type) {
        decorationBlockEntity = type;
    }

    public static BlockEntityType<SewingMachineBlockEntity> sewingMachineBlockEntity() {
        return require(sewingMachineBlockEntity, "sewing machine block entity type");
    }

    public static void setSewingMachineBlockEntity(BlockEntityType<SewingMachineBlockEntity> type) {
        sewingMachineBlockEntity = type;
    }

    public static Item plainTextureItem() {
        return require(plainTextureItem, "plain texture item");
    }

    public static void setPlainTextureItem(Item item) {
        plainTextureItem = item;
    }

    public static Item sewingMachineItem() {
        return require(sewingMachineItem, "sewing machine item");
    }

    public static void setSewingMachineItem(Item item) {
        sewingMachineItem = item;
    }

    public static MenuType<?> sewingMachineMenu() {
        return require(sewingMachineMenu, "sewing machine menu type");
    }

    public static void setSewingMachineMenu(MenuType<?> menuType) {
        sewingMachineMenu = menuType;
    }

    public static RecipeType<SewingRecipe> sewingRecipeType() {
        return require(sewingRecipeType, "sewing recipe type");
    }

    public static void setSewingRecipeType(RecipeType<SewingRecipe> recipeType) {
        sewingRecipeType = recipeType;
    }

    public static RecipeSerializer<SewingRecipe> sewingRecipeSerializer() {
        return require(sewingRecipeSerializer, "sewing recipe serializer");
    }

    public static void setSewingRecipeSerializer(RecipeSerializer<SewingRecipe> serializer) {
        sewingRecipeSerializer = serializer;
    }

    private static <T> T require(T value, String name) {
        if (value == null) {
            throw new IllegalStateException("Decoration " + name + " has not been registered yet");
        }
        return value;
    }
}
