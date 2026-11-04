package org.ywzj.decoration.all;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.blockentity.DecorationBlockEntity;
import org.ywzj.decoration.blockentity.SewingMachineBlockEntity;
import org.ywzj.decoration.registry.DecorationRegistry;

public class AllBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, YwzjDecoration.MOD_ID);

    public static final RegistryObject<BlockEntityType<DecorationBlockEntity>> DECORATION_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("decoration_block_entity", () -> {
                BlockEntityType<DecorationBlockEntity> type = BlockEntityType.Builder
                        .of(DecorationBlockEntity::new, AllBlocks.DECORATION_BLOCK.get())
                        .build(null);
                DecorationRegistry.setDecorationBlockEntity(type);
                return type;
            });

    public static final RegistryObject<BlockEntityType<SewingMachineBlockEntity>> SEWING_MACHINE =
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
