package org.ywzj.decoration.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.ywzj.decoration.blockentity.SewingMachineBlockEntity;
import org.ywzj.decoration.menu.SewingMachineMenu;
import org.ywzj.decoration.recipe.SewingRecipe;

public record ClientSewingAction(int containerId, ResourceLocation recipeId) {

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeResourceLocation(recipeId);
    }

    public static ClientSewingAction decode(FriendlyByteBuf buffer) {
        return new ClientSewingAction(buffer.readVarInt(), buffer.readResourceLocation());
    }

    public void handle(ServerPlayer player) {
        if (player == null || player.isSpectator()
                || !(player.containerMenu instanceof SewingMachineMenu menu)
                || menu.containerId != containerId || !menu.stillValid(player)
                || !player.level().mayInteract(player, menu.getBlockPos())) {
            return;
        }
        SewingMachineBlockEntity machine = menu.getMachine();
        if (machine == null || !machine.getOutput().isEmpty()) {
            return;
        }
        RecipeHolder<?> holder = player.level().getRecipeManager().byKey(recipeId).orElse(null);
        if (holder == null || !(holder.value() instanceof SewingRecipe sewing)) {
            return;
        }
        int[] consumption = sewing.planConsumption(player.getInventory());
        if (consumption == null) {
            return;
        }
        for (int slot = 0; slot < consumption.length; slot++) {
            if (consumption[slot] > 0) {
                player.getInventory().items.get(slot).shrink(consumption[slot]);
            }
        }
        player.getInventory().setChanged();
        machine.getOutput().setItem(0, sewing.getResultItem(player.level().registryAccess()));
        menu.broadcastChanges();
    }
}
