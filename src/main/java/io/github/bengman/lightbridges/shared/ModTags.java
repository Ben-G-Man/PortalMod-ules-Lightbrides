package io.github.bengman.lightbridges.shared;

import io.github.bengman.lightbridges.LightBridges;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

public class ModTags {

    public static class Blocks {

        public static final ITag.INamedTag<Block> BRIDGE_PASSTHROUGH = BlockTags.bind(
                new ResourceLocation(
                        LightBridges.MOD_ID,
                        "bridge_passthrough").toString());
    }
}
