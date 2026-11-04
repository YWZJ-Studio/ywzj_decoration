package org.ywzj.decoration.all;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.blockentity.SewingMachineBlockEntity;
import org.ywzj.decoration.registry.DecorationRegistry;

public class AllBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, YwzjDecoration.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DecorationBlockEntity>> DECORATION_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("decoration_block_entity", () -> {
                BlockEntityType<DecorationBlockEntity> type = BlockEntityType.Builder
                        .of(DecorationBlockEntity::new, AllBlocks.DECORATION_BLOCK.get())
                        .build(null);
                DecorationRegistry.setDecorationBlockEntity(type);
                return type;
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SewingMachineBlockEntity>> SEWING_MACHINE =
            BLOCK_ENTITIES.register("sewing_machine", () -> {
                BlockEntityType<SewingMachineBlockEntity> type = BlockEntityType.Builder
                        .of(SewingMachineBlockEntity::new, AllBlocks.SEWING_MACHINE.get())
                        .build(null);
                DecorationRegistry.setSewingMachineBlockEntity(type);
                return type;
            });

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
