package org.ywzj.decoration.all;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.block.DecorationBlock;
import org.ywzj.decoration.block.SewingMachineBlock;

import java.util.function.Supplier;

public class AllBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, YwzjDecoration.MOD_ID);

    public static final RegistryObject<Block> DECORATION_BLOCK = registerBlock("decoration_block",
            () -> new DecorationBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .sound(SoundType.WOOL)
                    .strength(0.5F)
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)));

    public static final RegistryObject<Block> SEWING_MACHINE = registerBlock("sewing_machine",
            () -> new SewingMachineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .strength(2.5F)
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)));

    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
