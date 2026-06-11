package io.github.bengman.lightbridges.shared.utils;

import io.github.bengman.lightbridges.server.LightBridgeSegment;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.common.sorted.portal.PortalEnd;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.common.sorted.portal.PortalManager;
import net.portalmod.common.sorted.portal.PortalPair;
import net.portalmod.core.math.Vec3;

public final class TraversalUtils {

    private TraversalUtils() {
    }

    /* ---- Portal Discovery ---- */

    public static PortalEntity findPortalAt(Vector3d point) {

        for (PortalPair pair : PortalManager.getInstance().getPortalMap().values()) {

            PortalEntity portalA = pair.get(PortalEnd.PRIMARY);
            PortalEntity portalB = pair.get(PortalEnd.SECONDARY);

            if (isPortalAt(point, portalA)) {
                return portalA;
            }

            if (isPortalAt(point, portalB)) {
                return portalB;
            }
        }

        return null;
    }

    private static boolean isPortalAt(Vector3d point, PortalEntity portal) {

        if (portal == null) {
            return false;
        }

        if (!portal.isOpen()) {
            return false;
        }

        return portal.getBoundingBox()
                .inflate(0.05)
                .contains(point);
    }

    /* ---- Portal Pair Lookup ---- */

    public static PortalEntity getPartnerPortal(PortalEntity portal) {

        if (portal == null) {
            return null;
        }

        if (portal.getEnd() == PortalEnd.NONE) {
            return null;
        }

        return PortalManager.getInstance().get(
                portal.getGunUUID(),
                getOppositePortalEnd(portal.getEnd()));
    }

    public static PortalEnd getOppositePortalEnd(PortalEnd end) {

        switch (end) {
            case PRIMARY:
                return PortalEnd.SECONDARY;

            case SECONDARY:
                return PortalEnd.PRIMARY;

            default:
                return PortalEnd.NONE;
        }
    }

    /* ---- Portal Transformations ---- */

    public static Vector3d teleportPosition(PortalEntity portal, Vector3d point) {

        Vec3 result = portal.teleportPoint(
                new Vec3(
                        point.x,
                        point.y,
                        point.z));

        return new Vector3d(
                result.x,
                result.y,
                result.z);
    }

    public static Vector3d teleportDirection(PortalEntity portal, Vector3d direction) {

        Vec3 result = portal.teleportVector(
                new Vec3(
                        direction.x,
                        direction.y,
                        direction.z));

        return new Vector3d(
                result.x,
                result.y,
                result.z).normalize();
    }

    public static boolean isDirectionOpposite(Direction direction, Direction direction2) {
        return direction.getAxis() == direction2.getAxis()
                && direction.getAxisDirection() != direction2.getAxisDirection();
    }

    public static Vector3d getPortalPlaneIntersection(PortalEntity portal, LightBridgeSegment segment) {

        Vector3d start = segment.start;
        Direction dir = segment.direction;

        Vector3d dirVec = VectorUtils.directionToVector(dir);

        Vector3d planePoint = new Vector3d(
                portal.getX(),
                portal.getY(),
                portal.getZ());

        Vector3d normal = VectorUtils.directionToVector(portal.getDirection());

        double denom = dirVec.dot(normal);

        if (Math.abs(denom) < 1e-6) {
            return segment.end;
        }

        double t = planePoint.subtract(start).dot(normal) / denom;

        if (t < 0) {
            return segment.end;
        }

        return start.add(dirVec.scale(t));
    }
}