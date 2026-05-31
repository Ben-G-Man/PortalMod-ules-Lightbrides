package io.github.bengman.lightbridges;

import io.github.bengman.lightbridges.shared.BridgeEmitterBlock;
import io.github.bengman.lightbridges.shared.BridgeEmitterTileEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(LightBridges.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LightBridges {

    public static final String MOD_ID = "lightbridges";

    /* ---- Blocks ---- */

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            LightBridges.MOD_ID);

    public static final RegistryObject<Block> EMITTER_BLOCK = BLOCKS.register("bridge_emitter",
            () -> new BridgeEmitterBlock(AbstractBlock.Properties
                    .of(Material.METAL)
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()));

    /* ---- Items ---- */

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            LightBridges.MOD_ID);

    public static final RegistryObject<Item> EMITTER_ITEM = ITEMS.register("bridge_emitter",
            () -> new BlockItem(EMITTER_BLOCK.get(), new Item.Properties()));

    /* ---- Block Entities ---- */

    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister
            .create(ForgeRegistries.TILE_ENTITIES, LightBridges.MOD_ID);

    public static final RegistryObject<TileEntityType<BridgeEmitterTileEntity>> EMITTER_TILE_ENTITY = TILE_ENTITIES
            .register(
                    "bridge_emitter",
                    () -> TileEntityType.Builder.of(
                            BridgeEmitterTileEntity::new,
                            EMITTER_BLOCK.get()).build(null));

    /* ---- Setup ---- */

    public LightBridges() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILE_ENTITIES.register(bus);
    }
}