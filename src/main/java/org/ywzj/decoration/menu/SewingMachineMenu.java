package org.ywzj.decoration.menu;

import com.sighs.apricityui.container.SlotLayout;
import com.sighs.apricityui.container.bind.ContainerBindType;
import com.sighs.apricityui.container.datasource.ContainerDataSource;
import com.sighs.apricityui.screen.ApricityContainerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.ywzj.decoration.all.AllMenus;
import org.ywzj.decoration.blockentity.SewingMachineBlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class SewingMachineMenu extends ApricityContainerMenu {

    public static final String TEMPLATE = "screens/sewing_machine.html";
    private static final SlotLayout LAYOUT = new SlotLayout(TEMPLATE, List.of(
            new SlotLayout.ContainerEntry("output", ContainerBindType.BLOCK_ENTITY, 0, 1, true),
            new SlotLayout.ContainerEntry("inventory", ContainerBindType.PLAYER, 1, 36, false)));

    private final BlockPos blockPos;
    @Nullable
    private final SewingMachineBlockEntity machine;

    public SewingMachineMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos(), null, new SimpleContainer(1));
    }

    public SewingMachineMenu(int id, Inventory inventory, SewingMachineBlockEntity machine) {
        this(id, inventory, machine.getBlockPos(), machine, machine.getOutput());
    }

    private SewingMachineMenu(int id, Inventory inventory, BlockPos pos,
                              @Nullable SewingMachineBlockEntity machine, Container output) {
        super(id, inventory, LAYOUT, Map.of("output", outputSource(output)), Map.of(),
                inventory.player instanceof ServerPlayer player ? player : null);
        this.blockPos = pos.immutable();
        this.machine = machine;
    }

    private static ContainerDataSource outputSource(Container output) {
        return new ContainerDataSource() {
            @Override
            public ContainerBindType bindType() {
                return ContainerBindType.BLOCK_ENTITY;
            }

            @Override
            public int capacity() {
                return 1;
            }

            @Override
            public Slot createSlot(int index, int x, int y) {
                return new UiSlot(output, index, x, y) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                };
            }
        };
    }

    @Override
    public MenuType<?> getType() {
        return AllMenus.SEWING_MACHINE.get();
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Nullable
    public SewingMachineBlockEntity getMachine() {
        return machine;
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && (player.level().isClientSide
                || machine != null && !machine.isRemoved() && player.level().getBlockEntity(blockPos) == machine
                && player.distanceToSqr(blockPos.getCenter()) <= 64.0D);
    }
}
