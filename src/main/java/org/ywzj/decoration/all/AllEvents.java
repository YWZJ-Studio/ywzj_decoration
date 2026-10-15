package org.ywzj.decoration.all;

import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.command.RootCommand;
import org.ywzj.decoration.resource.DecorationPackLoader;

public class AllEvents {

    @Mod.EventBusSubscriber(modid = YwzjDecoration.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class AllModEvents {

        @SubscribeEvent
        public static void onAddPackFinders(AddPackFindersEvent event) {
            event.addRepositorySource(DecorationPackLoader.INSTANCE);
        }

    }

    @Mod.EventBusSubscriber(modid = YwzjDecoration.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class AllForgeEvents {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            RootCommand.register(event.getDispatcher());
        }

    }

}
