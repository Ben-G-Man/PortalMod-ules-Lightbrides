package io.github.bengman.lightbridges.client;

import io.github.bengman.lightbridges.LightBridges;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = LightBridges.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(
            FMLClientSetupEvent event) {

        ClientRegistry.bindTileEntityRenderer(
                LightBridges.EMITTER_TILE_ENTITY.get(),
                LightBridgeRenderer::new);
    }
}