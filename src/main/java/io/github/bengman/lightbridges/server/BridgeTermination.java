package io.github.bengman.lightbridges.server;

import net.minecraft.util.math.vector.Vector3d;
import net.portalmod.common.sorted.portal.PortalEntity;

public class BridgeTermination {

    private final Vector3d end;
    private final PortalEntity portal;

    public BridgeTermination(
            Vector3d end,
            PortalEntity portal) {

        this.end = end;
        this.portal = portal;
    }

    public Vector3d getEnd() {
        return end;
    }

    public PortalEntity getPortal() {
        return portal;
    }

    public boolean traversesPortal() {
        return portal != null;
    }
}