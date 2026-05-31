package io.github.bengman.lightbridges.shared;

import net.minecraft.block.PistonBlockStructureHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import io.github.bengman.lightbridges.LightBridges;
import io.github.bengman.lightbridges.server.LightBridgeSegment;

@Mod.EventBusSubscriber(modid = LightBridges.MOD_ID)
public class BridgeWorldEvents {

    @SubscribeEvent
    public static void onBlockPlace(
            BlockEvent.EntityPlaceEvent event) {

        World level = (World) event.getWorld();

        if (level.isClientSide) {
            return;
        }

        LightBridgeSegment.notifyBlockChanged(
                event.getPos());
    }

    @SubscribeEvent
    public static void onBlockBreak(
            BlockEvent.BreakEvent event) {

        World level = event.getPlayer().level;

        if (level.isClientSide) {
            return;
        }

        LightBridgeSegment.notifyBlockChanged(
                event.getPos());
    }

    @SubscribeEvent
    public static void onExplosionDetonate(
            ExplosionEvent.Detonate event) {

        World level = event.getWorld();

        if (level.isClientSide) {
            return;
        }

        for (BlockPos pos : event.getAffectedBlocks()) {

            LightBridgeSegment.notifyBlockChanged(pos);
        }
    }

    @SubscribeEvent
    public static void onPistonEvent(
            PistonEvent.Post event) {

        World level = (World) event.getWorld();

        if (level.isClientSide) {
            return;
        }

        Direction direction = event.getDirection();

        PistonBlockStructureHelper helper = event.getStructureHelper();

        for (BlockPos pos : helper.getToPush()) {

            LightBridgeSegment.notifyBlockChanged(pos);

            LightBridgeSegment.notifyBlockChanged(
                    pos.relative(direction));
        }

        for (BlockPos pos : helper.getToDestroy()) {

            LightBridgeSegment.notifyBlockChanged(pos);
        }
    }
}
