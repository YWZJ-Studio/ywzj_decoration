package org.ywzj.decoration.all;

import com.sighs.apricityui.screen.ApricityContainerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.menu.SewingMachineMenu;
import org.ywzj.decoration.registry.DecorationRegistry;

public class AllMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, YwzjDecoration.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ApricityContainerMenu>> SEWING_MACHINE =
            MENUS.register("sewing_machine", () -> {
                MenuType<ApricityContainerMenu> type = IMenuTypeExtension.create(
                        (id, inventory, buffer) -> new SewingMachineMenu(id, inventory, buffer));
                DecorationRegistry.setSewingMachineMenu(type);
                return type;
            });

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
