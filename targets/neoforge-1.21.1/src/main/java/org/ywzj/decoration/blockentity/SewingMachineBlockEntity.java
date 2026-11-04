package org.ywzj.decoration.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.ywzj.decoration.menu.SewingMachineMenu;
import org.ywzj.decoration.registry.DecorationRegistry;

public class SewingMachineBlockEntity extends BlockEntity implements MenuProvider {

    private final SimpleContainer output = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            SewingMachineBlockEntity.this.setChanged();
        }
    };

    public SewingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(DecorationRegistry.sewingMachineBlockEntity(), pos, state);
    }

    public SimpleContainer getOutput() {
        return output;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ywzj_decoration.sewing_machine");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SewingMachineMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!output.isEmpty()) {
            tag.put("Output", output.getItem(0).save(registries, new CompoundTag()));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        output.setItem(0, tag.contains("Output")
                ? ItemStack.parseOptional(registries, tag.getCompound("Output"))
                : ItemStack.EMPTY);
    }
}
