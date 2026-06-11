package io.github.bengman.lightbridges.shared;

import io.github.bengman.lightbridges.LightBridges;
import io.github.bengman.lightbridges.server.LightBridgeSegment;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class BridgeEmitterTileEntity extends TileEntity implements ITickableTileEntity {

    public boolean active;
    public Vector3d start;
    public Direction direction;
    public Direction up = Direction.UP;

    private BridgeDTO bridge;

    private boolean initialized = false;

    private LightBridgeSegment bridgeRoot;

    public BridgeEmitterTileEntity() {
        super(LightBridges.EMITTER_TILE_ENTITY.get());
    }

    public BridgeDTO getBridge() {
        return bridge == null ? new BridgeDTO() : bridge;
    }

    /* ---- Bridge Logic ---- */

    @Override
    public void tick() {
        if (level == null) {
            return;
        }

        if (!initialized) {
            initialized = initialize();
        }

        if (!level.isClientSide) {

            if (bridgeRoot == null && active && initialized) {
                bridgeRoot = new LightBridgeSegment(start, direction, up);
            }

            if (bridgeRoot != null && !active) {
                bridgeRoot.onDestroy();
                bridgeRoot = null;
            }

            if (bridgeRoot != null) {

                BridgeDTO update = bridgeRoot.tick(level);

                if (update != null) {
                    bridge = update;
                    sync();
                }
            }
        }
    }

    private boolean initialize() {

        if (level == null) {
            return false;
        }

        if (level.isClientSide) {
            return true;
        }

        BlockState state = level.getBlockState(worldPosition);

        direction = state.getValue(BridgeEmitterBlock.FACING);

        if (direction == Direction.DOWN || direction == Direction.UP) {
            up = Direction.NORTH;
        } else {
            up = Direction.UP;
        }

        active = true;
        start = Vector3d.atCenterOf(worldPosition);

        sync();

        return true;
    }

    /* ---- Cleanup ---- */

    public void destroyBridge() {

        if (bridgeRoot != null) {
            bridgeRoot.onDestroy();
            bridgeRoot = null;
        }

        bridge = null;

        setChanged();

        if (level != null && !level.isClientSide) {

            BlockState state = getBlockState();

            level.sendBlockUpdated(
                    worldPosition,
                    state,
                    state,
                    3);
        }
    }

    /* ---- Synchronization ---- */

    private void sync() {

        setChanged();

        BlockState state = getBlockState();

        level.sendBlockUpdated(
                worldPosition,
                state,
                state,
                3);
    }

    private CompoundNBT writeEmitterData(CompoundNBT tag) {

        tag.putBoolean("active", active);

        if (start != null) {
            tag.putDouble("startX", start.x);
            tag.putDouble("startY", start.y);
            tag.putDouble("startZ", start.z);
        }

        if (direction != null) {
            tag.putInt("direction", direction.get3DDataValue());
        }

        if (up != null) {
            tag.putInt("up", up.get3DDataValue());
        }

        if (bridge != null) {
            tag.put("bridge", bridge.toNBT());
        }

        return tag;
    }

    private void readEmitterData(CompoundNBT tag) {

        if (tag.contains("active")) {
            active = tag.getBoolean("active");
        }

        if (tag.contains("startX")) {

            start = new Vector3d(
                    tag.getDouble("startX"),
                    tag.getDouble("startY"),
                    tag.getDouble("startZ"));
        }

        if (tag.contains("direction")) {
            direction = Direction.from3DDataValue(tag.getInt("direction"));
        }

        if (tag.contains("up")) {
            up = Direction.from3DDataValue(tag.getInt("up"));
        }

        if (tag.contains("bridge")) {
            bridge = BridgeDTO.fromNBT(tag.getCompound("bridge"));
        } else {
            bridge = null;
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {

        super.save(tag);

        return writeEmitterData(tag);
    }

    @Override
    public void load(BlockState state, CompoundNBT tag) {

        super.load(state, tag);

        readEmitterData(tag);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {

        CompoundNBT tag = new CompoundNBT();

        writeEmitterData(tag);

        return new SUpdateTileEntityPacket(
                worldPosition,
                0,
                tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readEmitterData(pkt.getTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {

        CompoundNBT tag = super.getUpdateTag();

        return writeEmitterData(tag);
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        readEmitterData(tag);
    }

    /* ---- Rendering Support ---- */

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bridge == null || bridge.getSegments().isEmpty()) {
            return super.getRenderBoundingBox();
        }

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (BridgeSegmentDTO segment : bridge.getSegments()) {

            Vector3d start = segment.getStart();
            Vector3d end = segment.getEnd();

            minX = Math.min(minX, Math.min(start.x, end.x));
            minY = Math.min(minY, Math.min(start.y, end.y));
            minZ = Math.min(minZ, Math.min(start.z, end.z));

            maxX = Math.max(maxX, Math.max(start.x, end.x));
            maxY = Math.max(maxY, Math.max(start.y, end.y));
            maxZ = Math.max(maxZ, Math.max(start.z, end.z));
        }

        return new AxisAlignedBB(
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ)
                .inflate(1.0);
    }
}