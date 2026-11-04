package org.ywzj.decoration.item;

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
import org.ywzj.decoration.block.DecorationBlock;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.platform.DecorationClientPlatform;
import org.ywzj.decoration.platform.DecorationPlatforms;
import org.ywzj.decoration.registry.DecorationRegistry;

import javax.annotation.Nullable;
import java.util.Objects;

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
        if (DecorationPlatforms.isClient()) {
            DecorationClientPlatform client = DecorationPlatforms.client();
            String description = client == null ? null : client.displayDescription(displayId);
            if (description != null) {
                return Component.translatable(description);
            }
        }
        return super.getName(itemStack);
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
                DecorationClientPlatform client = DecorationPlatforms.client();
                if (client != null) {
                    client.openDecorationScreen(decoration);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        if (!placeContext.canPlace()) {
            return InteractionResult.FAIL;
        }
        BlockState state = DecorationRegistry.decorationBlock().getStateForPlacement(placeContext);
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
