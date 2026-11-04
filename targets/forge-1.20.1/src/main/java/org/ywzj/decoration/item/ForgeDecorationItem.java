package org.ywzj.decoration.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.ywzj.decoration.client.render.item.DecorationItemRenderer;

import java.util.function.Consumer;

public class ForgeDecorationItem extends DecorationItem {

    public ForgeDecorationItem(ResourceLocation displayId, Properties properties) {
        super(displayId, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private DecorationItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                Minecraft minecraft = Minecraft.getInstance();
                if (renderer == null) {
                    renderer = new DecorationItemRenderer(minecraft.getBlockEntityRenderDispatcher(), minecraft.getEntityModels());
                }
                return renderer;
            }
        });
    }
}
