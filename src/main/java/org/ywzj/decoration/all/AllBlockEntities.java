package org.ywzj.decoration.all;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.blockentity.SewingMachineBlockEntity;

public class AllBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, YwzjDecoration.MOD_ID);

    public static final RegistryObject<BlockEntityType<DecorationBlockEntity>> DECORATION_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("decoration_block_entity", () ->
                    BlockEntityType.Builder.of(DecorationBlockEntity::new, AllBlocks.DECORATION_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<SewingMachineBlockEntity>> SEWING_MACHINE =
            BLOCK_ENTITIES.register("sewing_machine", () ->
                    BlockEntityType.Builder.of(SewingMachineBlockEntity::new, AllBlocks.SEWING_MACHINE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

}
