package io.github.bengman.lightbridges.shared;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class BridgeSegmentDTO {
    private Vector3d start;
    private Vector3d end;
    private Direction up;

    public BridgeSegmentDTO(Vector3d start, Vector3d end, Direction up) {
        this.start = start;
        this.end = end;
        this.up = up;
    }

    public Vector3d getStart() {
        return start;
    }

    public Vector3d getEnd() {
        return end;
    }

    public Direction getUp() {
        return up;
    }

    public Direction getDirection() {

        Vector3d delta = end.subtract(start).normalize();

        if (delta.x > 0.5)
            return Direction.EAST;
        if (delta.x < -0.5)
            return Direction.WEST;

        if (delta.y > 0.5)
            return Direction.UP;
        if (delta.y < -0.5)
            return Direction.DOWN;

        if (delta.z > 0.5)
            return Direction.SOUTH;

        return Direction.NORTH;
    }

    /* ---- Serialization ---- */

    public CompoundNBT toNBT() {

        CompoundNBT tag = new CompoundNBT();

        tag.putDouble("startX", start.x);
        tag.putDouble("startY", start.y);
        tag.putDouble("startZ", start.z);

        tag.putDouble("endX", end.x);
        tag.putDouble("endY", end.y);
        tag.putDouble("endZ", end.z);

        tag.putInt("up", up.get3DDataValue()); // TODO: Fix client-server stuff

        return tag;
    }

    public static BridgeSegmentDTO fromNBT(
            CompoundNBT tag) {

        Vector3d start = new Vector3d(
                tag.getDouble("startX"),
                tag.getDouble("startY"),
                tag.getDouble("startZ"));

        Vector3d end = new Vector3d(
                tag.getDouble("endX"),
                tag.getDouble("endY"),
                tag.getDouble("endZ"));

        Direction up = Direction.from3DDataValue(tag.getInt("up"));

        return new BridgeSegmentDTO(start, end, up);
    }
}
