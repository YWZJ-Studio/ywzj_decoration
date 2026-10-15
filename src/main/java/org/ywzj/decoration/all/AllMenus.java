package org.ywzj.decoration.all;

import com.sighs.apricityui.screen.ApricityContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.menu.SewingMachineMenu;

public class AllMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, YwzjDecoration.MOD_ID);

    public static final RegistryObject<MenuType<ApricityContainerMenu>> SEWING_MACHINE =
            MENUS.register("sewing_machine", () -> IForgeMenuType.create(SewingMachineMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
