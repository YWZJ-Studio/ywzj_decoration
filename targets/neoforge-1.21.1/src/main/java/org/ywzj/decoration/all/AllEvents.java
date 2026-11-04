package org.ywzj.decoration.all;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.ywzj.decoration.YwzjDecoration;
import org.ywzj.decoration.command.RootCommand;
import org.ywzj.decoration.resource.NeoForgeDecorationPackSource;

public class AllEvents {

    @EventBusSubscriber(modid = YwzjDecoration.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class AllModEvents {

        @SubscribeEvent
        public static void onAddPackFinders(AddPackFindersEvent event) {
            event.addRepositorySource(NeoForgeDecorationPackSource.INSTANCE);
        }
    }

    @EventBusSubscriber(modid = YwzjDecoration.MOD_ID)
    public static class AllGameEvents {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            RootCommand.register(event.getDispatcher());
        }
    }
}
