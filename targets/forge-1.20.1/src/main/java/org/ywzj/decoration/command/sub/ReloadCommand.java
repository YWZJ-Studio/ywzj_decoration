package org.ywzj.decoration.command.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.time.StopWatch;
import org.ywzj.decoration.client.resource.ClientAssetsManager;
import org.ywzj.decoration.custom.CommonAssetsManager;

import java.util.concurrent.TimeUnit;

public class ReloadCommand {

    private static final String RELOAD_NAME = "reload";

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> reloadCommand = Commands.literal(RELOAD_NAME);
        reloadCommand.executes(ReloadCommand::reload);
        return reloadCommand;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        StopWatch watch = StopWatch.createStarted();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            LocalPlayer player = Minecraft.getInstance().player;
            ClientAssetsManager.INSTANCE.reload(Minecraft.getInstance().getResourceManager());
            if (player != null && player.connection != null) {
                CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), true, player.level().registryAccess());
            }
        });
        MinecraftServer server = context.getSource().getServer();
        CommonAssetsManager.reload(server.getResourceManager());
        watch.stop();
        double time = watch.getTime(TimeUnit.MICROSECONDS) / 1000.0D;
        context.getSource().sendSystemMessage(Component.translatable("commands.decoration.reload.success", time));
        return Command.SINGLE_SUCCESS;
    }

}
