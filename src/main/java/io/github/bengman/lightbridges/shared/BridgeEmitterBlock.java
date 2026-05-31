package io.github.bengman.lightbridges.shared;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BridgeEmitterBlock extends ContainerBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 5, 0, 16, 11, 4);

    private static final VoxelShape SHAPE_NORTH = Block.box(0, 5, 12, 16, 11, 16);

    private static final VoxelShape SHAPE_EAST = Block.box(0, 5, 0, 4, 11, 16);

    private static final VoxelShape SHAPE_WEST = Block.box(12, 5, 0, 16, 11, 16);

    private static final VoxelShape SHAPE_DOWN = Block.box(0, 12, 5, 16, 16, 11);

    private static final VoxelShape SHAPE_UP = Block.box(0, 0, 5, 16, 4, 11);

    public BridgeEmitterBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH));
    }

    /* ---- Block Placement ---- */

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
        return new BridgeEmitterTileEntity();
    }

    /* ---- Block Shape ---- */

    @Override
    public VoxelShape getShape(BlockState state,
            IBlockReader world,
            BlockPos pos,
            ISelectionContext context) {

        switch (state.getValue(FACING)) {
            case NORTH:
                return SHAPE_NORTH;

            case SOUTH:
                return SHAPE_SOUTH;

            case EAST:
                return SHAPE_EAST;

            case WEST:
                return SHAPE_WEST;

            case UP:
                return SHAPE_UP;

            case DOWN:
                return SHAPE_DOWN;

            default:
                return SHAPE_SOUTH;
        }
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState p_196247_1_, IBlockReader p_196247_2_, BlockPos p_196247_3_) {
        return VoxelShapes.empty();
    }

    /* ---- Cleanup ---- */

    @Override
    public void onRemove(
            BlockState state,
            World level,
            BlockPos pos,
            BlockState newState,
            boolean isMoving) {

        if (state.getBlock() != newState.getBlock()) {

            TileEntity tile = level.getBlockEntity(pos);

            if (tile instanceof BridgeEmitterTileEntity) {

                BridgeEmitterTileEntity emitter = (BridgeEmitterTileEntity) tile;

                emitter.destroyBridge();
            }

            super.onRemove(
                    state,
                    level,
                    pos,
                    newState,
                    isMoving);
        }
    }
}