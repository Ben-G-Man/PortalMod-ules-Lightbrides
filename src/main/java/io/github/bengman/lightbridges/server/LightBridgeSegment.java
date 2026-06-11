package io.github.bengman.lightbridges.server;

import io.github.bengman.lightbridges.shared.BridgeDTO;
import io.github.bengman.lightbridges.shared.BridgeSegmentDTO;
import io.github.bengman.lightbridges.shared.ModTags;
import io.github.bengman.lightbridges.shared.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import net.portalmod.common.sorted.portal.PortalEntity;

public class LightBridgeSegment {

    /* ---- Bridge Settings ---- */
    private static final double MAX_LENGTH = 128.0;
    public static final int MAX_SEGMENTS = 16;
    private static final double RAY_TRACE_STEP = 0.1;
    private static final Map<Long, Set<LightBridgeSegment>> BRIDGE_MAP = new HashMap<>();

    /* ---- Bridge Shape Fields ---- */
    public Vector3d start;
    public Vector3d end;

    public Direction direction; // forward
    public Direction up; // bridge surface normal (replaces roll system)

    /* derived axis */
    private Direction right;

    /* ---- Space Exploration ---- */
    private final Set<Long> occupied = new HashSet<>();
    private boolean dirty = true;

    public Vector3d bound;

    /* ---- Portal Traversal ---- */
    private LightBridgeSegment next;
    private UUID portalUUID;
    private PortalEntity traversedPortal;
    private final int depth;

    /* ---- Constructors ---- */

    public LightBridgeSegment(Vector3d origin, Direction direction, Direction up) {
        this(origin, direction, up, 0);
    }

    protected LightBridgeSegment(Vector3d origin, Direction direction, Direction up, int depth) {
        this.start = origin;
        this.end = origin;
        this.direction = direction;
        this.up = up;
        this.depth = depth;

        this.right = computeRight(direction, up);

        Vector3d dirVec = VectorUtils.directionToVector(direction);
        this.bound = start.add(dirVec.scale(MAX_LENGTH));
    }

    /* ---- Core Tick ---- */

    public BridgeDTO tick(World level) {
        boolean changed = false;

        if (dirty) {
            dirty = false;
            changed = checkForBridgeUpdate(level);
        }

        BridgeDTO downstream = (next != null) ? next.tick(level) : null;

        if (!changed && downstream == null)
            return null;

        ArrayList<BridgeSegmentDTO> segments = new ArrayList<>();
        segments.add(new BridgeSegmentDTO(start, end, up));

        if (downstream != null) {
            segments.addAll(downstream.getSegments());
        }

        return new BridgeDTO(segments);
    }

    public void onDestroy() {
        unregisterAll();

        if (next != null) {
            next.onDestroy();
            next = null;
        }
    }

    /* ---- Update ---- */

    private boolean checkForBridgeUpdate(World level) {
        boolean changed = false;

        BridgeTermination termination = calculateTermination(level);
        LightBridgeSegment nextSeg = null;

        if (!termination.getEnd().equals(end)) {
            end = termination.getEnd();
            changed = true;
        }

        if (termination.traversesPortal()) {
            nextSeg = traversePortal(termination.getPortal());
        }

        if (nextSeg == null) {
            sever();
        } else {
            if (this.next == null) {
                this.next = nextSeg;
            } else {
                this.next.start = nextSeg.start;
                this.next.direction = nextSeg.direction;
                this.next.up = nextSeg.up;
                this.next.right = nextSeg.right;
                this.next.bound = nextSeg.bound;
                this.next.dirty = true;
            }
        }

        return changed;
    }

    /* ---- Spatial Registration ---- */

    private void register(BlockPos pos) {
        long key = pos.asLong();
        occupied.add(key);
        BRIDGE_MAP.computeIfAbsent(key, k -> new HashSet<>()).add(this);
    }

    private void sever() {
        traversedPortal = null;
        portalUUID = null;

        if (next != null) {
            next.onDestroy();
            next = null;
        }
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

    /* ---- Ray / Termination ---- */

    private BridgeTermination calculateTermination(World level) {
        unregisterAll();

        Vector3d current = start;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        Vector3d dirVec = VectorUtils.directionToVector(direction);

        for (double d = 0; d <= MAX_LENGTH; d += RAY_TRACE_STEP) {

            current = start.add(dirVec.scale(d));

            PortalEntity portal = TraversalUtils.findPortalAt(current);

            if (portal != null) {
                Vector3d hit = TraversalUtils.getPortalPlaneIntersection(portal, this);
                return new BridgeTermination(hit, portal);
            }

            mutable.set(current.x, current.y, current.z);
            register(mutable);

            BlockState state = level.getBlockState(mutable);

            if (blocksBridge(state)) {
                VoxelShape shape = state.getCollisionShape(level, mutable);

                if (!shape.isEmpty()) {
                    BlockRayTraceResult hit = shape.clip(start, bound, mutable);

                    if (hit != null) {
                        return new BridgeTermination(hit.getLocation(), null);
                    }
                }
            }
        }

        return new BridgeTermination(current, null);
    }

    /* ---- Portal Traversal ---- */

    private LightBridgeSegment traversePortal(PortalEntity entrance) {
        PortalEntity exit = TraversalUtils.getPartnerPortal(entrance);

        if (exit == null)
            return null;

        if (!TraversalUtils.isDirectionOpposite(direction, exit.getDirection()))
            return null;

        Vector3d exitDir = TraversalUtils.teleportDirection(entrance,
                VectorUtils.directionToVector(direction));

        Vector3d exitPos = TraversalUtils.teleportPosition(entrance, end);

        Direction newDirection = VectorUtils.vectorToDirection(exitDir);
        Direction newUp = transformUp(entrance, exit);

        if (depth >= MAX_SEGMENTS) {
            sever();
            return null;
        }

        return new LightBridgeSegment(exitPos, newDirection, newUp, depth + 1);
    }

    /* ---- Frame Construction ---- */

    private Direction computeRight(Direction forward, Direction up) {
        // right = forward × up (discrete approximation)
        Vector3d f = VectorUtils.directionToVector(forward);
        Vector3d u = VectorUtils.directionToVector(up);

        Vector3d r = new Vector3d(
                f.y * u.z - f.z * u.y,
                f.z * u.x - f.x * u.z,
                f.x * u.y - f.y * u.x);

        return VectorUtils.vectorToDirection(r);
    }

    private Direction transformUp(PortalEntity entrance, PortalEntity exit) {
        Vector3d upVec = VectorUtils.directionToVector(up);
        Vector3d transformed = TraversalUtils.teleportDirection(entrance, upVec);
        return VectorUtils.vectorToDirection(transformed);
    }

    /* ---- Bridge Rules ---- */

    public static boolean blocksBridge(BlockState state) {
        if (state.isAir())
            return false;
        return !state.is(ModTags.Blocks.BRIDGE_PASSTHROUGH);
    }

    /* ---- Global Hook ---- */

    public static void notifyBlockChanged(BlockPos pos) {
        Set<LightBridgeSegment> segments = BRIDGE_MAP.get(pos.asLong());
        if (segments == null)
            return;

        for (LightBridgeSegment segment : segments) {
            segment.dirty = true;
        }
    }
}