package org.ywzj.decoration.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.stream.Stream;

public class SewingRecipeSerializer implements RecipeSerializer<SewingRecipe> {

    private static final MapCodec<SewingIngredient> MATERIAL_CODEC = new MapCodec<>() {

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("ingredient"), ops.createString("count"));
        }

        @Override
        public <T> DataResult<SewingIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
            T ingredientNode = input.get("ingredient");
            DataResult<Ingredient> ingredient = ingredientNode != null
                    ? Ingredient.CODEC_NONEMPTY.parse(ops, ingredientNode)
                    : Ingredient.CODEC_NONEMPTY.parse(ops, ops.createMap(input.entries()));
            int count = 1;
            T countNode = input.get("count");
            if (countNode != null) {
                count = ops.getNumberValue(countNode, 1).intValue();
            }
            int resolvedCount = count;
            return ingredient.map(value -> new SewingIngredient(value, resolvedCount));
        }

        @Override
        public <T> RecordBuilder<T> encode(SewingIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            prefix.add("ingredient", Ingredient.CODEC_NONEMPTY.encodeStart(ops, input.ingredient()).getOrThrow());
            prefix.add("count", ops.createInt(input.count()));
            return prefix;
        }
    };

    private static final MapCodec<SewingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ItemStack.CODEC.fieldOf("result").forGetter(SewingRecipe::getResultStack),
                    MATERIAL_CODEC.codec().listOf().fieldOf("materials").forGetter(SewingRecipe::getMaterials))
            .apply(instance, SewingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, SewingIngredient> MATERIAL_STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, SewingIngredient::ingredient,
                    ByteBufCodecs.VAR_INT, SewingIngredient::count,
                    SewingIngredient::new);

    private static final StreamCodec<RegistryFriendlyByteBuf, SewingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC, SewingRecipe::getResultStack,
                    MATERIAL_STREAM_CODEC.apply(ByteBufCodecs.list()), SewingRecipe::getMaterials,
                    SewingRecipe::new);

    @Override
    public MapCodec<SewingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, SewingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
