package io.github.bengman.lightbridges.shared.utils;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public final class VectorUtils {

    private VectorUtils() {
    }

    /* ---- Direction Conversions ---- */

    public static Vector3d directionToVector(Direction direction) {

        return new Vector3d(
                direction.getStepX(),
                direction.getStepY(),
                direction.getStepZ());
    }

    public static Direction vectorToDirection(Vector3d vector) {

        Direction best = Direction.NORTH;
        double bestDot = Double.NEGATIVE_INFINITY;

        Vector3d normalized = vector.normalize();

        for (Direction direction : Direction.values()) {

            double dot = normalized.dot(
                    directionToVector(direction));

            if (dot > bestDot) {
                bestDot = dot;
                best = direction;
            }
        }

        return best;
    }

    /* ---- Direction Rotations ---- */

    public static Direction rotateDirection(Direction direction, Direction axis, int quarterTurns) {

        Vector3d vector = directionToVector(direction);

        for (int i = 0; i < Math.floorMod(quarterTurns, 4); i++) {
            vector = rotate90(vector, axis);
        }

        return vectorToDirection(vector);
    }

    private static Vector3d rotate90(Vector3d vector, Direction axis) {

        switch (axis) {

            case UP:
                return new Vector3d(-vector.z, vector.y, vector.x);

            case DOWN:
                return new Vector3d(vector.z, vector.y, -vector.x);

            case EAST:
                return new Vector3d(vector.x, -vector.z, vector.y);

            case WEST:
                return new Vector3d(vector.x, vector.z, -vector.y);

            case SOUTH:
                return new Vector3d(vector.y, -vector.x, vector.z);

            case NORTH:
                return new Vector3d(-vector.y, vector.x, vector.z);

            default:
                return vector;
        }
    }

    /* ---- Direction Relationships ---- */

    public static boolean areParallel(Direction a, Direction b) {

        return a == b || a == b.getOpposite();
    }

    public static boolean arePerpendicular(Direction a, Direction b) {

        return !areParallel(a, b);
    }
}