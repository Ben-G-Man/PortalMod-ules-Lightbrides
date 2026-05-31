package io.github.bengman.lightbridges.shared;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;

public class BridgeSegmentDTO {
    private Vector3d start;
    private Vector3d end;
    private float roll;

    public BridgeSegmentDTO(Vector3d start, Vector3d end, float roll) {
        this.start = start;
        this.end = end;
        this.roll = roll;
    }

    public Vector3d getStart() {
        return start;
    }

    public Vector3d getEnd() {
        return end;
    }

    public float getRoll() {
        return roll;
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

        tag.putFloat("roll", roll);

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

        float roll = tag.getFloat("roll");

        return new BridgeSegmentDTO(start, end, roll);
    }
}
