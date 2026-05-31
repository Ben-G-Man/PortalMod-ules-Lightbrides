package io.github.bengman.lightbridges.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.bengman.lightbridges.shared.BridgeDTO;
import io.github.bengman.lightbridges.shared.BridgeSegmentDTO;
import io.github.bengman.lightbridges.shared.ModTags;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class LightBridgeSegment {

    private static final double MAX_LENGTH = 128.0;

    private static final Map<Long, Set<LightBridgeSegment>> BRIDGE_MAP = new HashMap<>();

    public static final int MAX_SEGMENTS = 16;

    public Vector3d start;
    public Vector3d end;
    public Vector3d direction;
    public Vector3d bound;
    public float roll;

    private BlockPos obstruction;

    private LightBridgeSegment next;

    private boolean dirty = true;

    private final Set<Long> occupied = new HashSet<>();

    public LightBridgeSegment(
            Vector3d origin,
            Vector3d direction,
            float roll) {

        this.start = origin;
        this.end = origin;
        this.direction = direction.normalize();
        this.roll = roll;

        bound = start.add(
                this.direction.scale(MAX_LENGTH));
    }

    public void onDestroy() {

        unregisterAll();

        if (next != null) {
            next.onDestroy();
            next = null;
        }
    }

    public static void notifyBlockChanged(
            BlockPos pos) {

        Set<LightBridgeSegment> segments = BRIDGE_MAP.get(pos.asLong());

        if (segments == null) {
            return;
        }

        for (LightBridgeSegment segment : segments) {
            segment.dirty = true;
        }
    }

    public BridgeDTO tick(World level) {

        boolean changed = false;

        if (dirty) {

            dirty = false;

            changed = recompute(level);
        }

        BridgeDTO downstream = null;

        if (next != null) {
            downstream = next.tick(level);
        }

        if (!changed && downstream == null) {
            return null;
        }

        ArrayList<BridgeSegmentDTO> segments = new ArrayList<>();

        segments.add(
                new BridgeSegmentDTO(
                        start,
                        end,
                        roll));

        if (downstream != null) {
            segments.addAll(
                    downstream.getSegments());
        }

        return new BridgeDTO(segments);
    }

    private boolean recompute(World level) {

        Vector3d oldEnd = end;

        unregisterAll();

        Vector3d current = start;

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        double step = 0.1;

        for (double d = 0; d <= MAX_LENGTH; d += step) {

            current = start.add(
                    direction.scale(d));

            mutable.set(
                    current.x,
                    current.y,
                    current.z);

            register(mutable);

            BlockState state = level.getBlockState(mutable);

            if (blocksBridge(state)) {

                VoxelShape shape = state.getCollisionShape(
                        level,
                        mutable);

                if (!shape.isEmpty()) {

                    BlockRayTraceResult hit = shape.clip(
                            start,
                            bound,
                            mutable);

                    if (hit != null) {

                        obstruction = mutable.immutable();

                        end = hit.getLocation()
                                .subtract(
                                        direction.scale(0.01));

                        break;
                    }
                }
            }

            end = current;
        }

        return !end.equals(oldEnd);
    }

    private boolean blocksBridge(
            BlockState state) {

        if (state.isAir()) {
            return false;
        }

        return !state.is(
                ModTags.Blocks.BRIDGE_PASSTHROUGH);
    }

    private void register(BlockPos pos) {

        long key = pos.asLong();

        occupied.add(key);

        BRIDGE_MAP
                .computeIfAbsent(
                        key,
                        k -> new HashSet<>())
                .add(this);
    }

    private void unregisterAll() {

        for (long key : occupied) {

            Set<LightBridgeSegment> set = BRIDGE_MAP.get(key);

            if (set != null) {

                set.remove(this);

                if (set.isEmpty()) {
                    BRIDGE_MAP.remove(key);
                }
            }
        }

        occupied.clear();
    }
}