package org.ywzj.decoration.all;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.block.DecorationBlock;
import org.ywzj.decoration.block.SewingMachineBlock;
import org.ywzj.decoration.registry.DecorationRegistry;

public class AllBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, YwzjDecoration.MOD_ID);

    public static final DeferredHolder<Block, Block> DECORATION_BLOCK = BLOCKS.register("decoration_block", () -> {
        Block block = new DecorationBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOL)
                .sound(SoundType.WOOL)
                .strength(0.5F)
                .noOcclusion()
                .isViewBlocking((state, level, pos) -> false));
        DecorationRegistry.setDecorationBlock(block);
        return block;
    });

    public static final DeferredHolder<Block, Block> SEWING_MACHINE = BLOCKS.register("sewing_machine", () -> {
        Block block = new SewingMachineBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(2.5F)
                .noOcclusion()
                .isViewBlocking((state, level, pos) -> false));
        DecorationRegistry.setSewingMachine(block);
        return block;
    });

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
