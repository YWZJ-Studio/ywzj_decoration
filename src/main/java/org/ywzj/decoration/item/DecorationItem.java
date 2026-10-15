package org.ywzj.decoration.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.ywzj.decoration.all.AllBlocks;
import org.ywzj.decoration.block.DecorationBlock;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.client.render.item.DecorationItemRenderer;
import org.ywzj.decoration.client.resource.ClientAssetsManager;
import org.ywzj.decoration.client.resource.display.BaseDisplay;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class DecorationItem extends Item {

    private final ResourceLocation displayId;

    public DecorationItem(ResourceLocation displayId, Properties properties) {
        super(properties);
        this.displayId = Objects.requireNonNull(displayId);
    }

    public ResourceLocation getDisplayId() {
        return displayId;
    }

    @Override
    public Component getName(ItemStack itemStack) {
        if (FMLEnvironment.dist.isClient()) {
            String description = descriptionOf(displayId);
            if (description != null) {
                return Component.translatable(description);
            }
        }
        return super.getName(itemStack);
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private static String descriptionOf(ResourceLocation displayId) {
        BaseDisplay display = ClientAssetsManager.INSTANCE.getDecorationDisplay(displayId).orElse(null);
        if (display == null || display.getDescription() == null || display.getDescription().isBlank()) {
            return null;
        }
        return display.getDescription();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private DecorationItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                Minecraft minecraft = Minecraft.getInstance();
                if (renderer == null) {
                    renderer = new DecorationItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return renderer;
            }

        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()
                && level.getBlockState(clickedPos).getBlock() instanceof DecorationBlock
                && level.getBlockEntity(clickedPos) instanceof DecorationBlockEntity decoration) {
            if (level.isClientSide()) {
                DecorationBlock.openScreen(decoration);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        if (!placeContext.canPlace()) {
            return InteractionResult.FAIL;
        }
        BlockState state = AllBlocks.DECORATION_BLOCK.get().getStateForPlacement(placeContext);
        if (state == null) {
            return InteractionResult.FAIL;
        }
        BlockPos pos = placeContext.getClickedPos();
        if (!level.setBlock(pos, state, 11)) {
            return InteractionResult.FAIL;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DecorationBlockEntity decoration) {
            decoration.setDisplayId(displayId);
            decoration.setYRot(player.getYRot() + 180.0F);
            decoration.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        level.playSound(player, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

}
