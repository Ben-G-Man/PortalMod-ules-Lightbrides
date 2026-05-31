package io.github.bengman.lightbridges.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import io.github.bengman.lightbridges.shared.BridgeDTO;
import io.github.bengman.lightbridges.shared.BridgeEmitterTileEntity;
import io.github.bengman.lightbridges.shared.BridgeSegmentDTO;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class LightBridgeRenderer
        extends TileEntityRenderer<BridgeEmitterTileEntity> {

    public LightBridgeRenderer(
            TileEntityRendererDispatcher dispatcher) {

        super(dispatcher);
    }

    @Override
    public void render(
            BridgeEmitterTileEntity tile,
            float partialTicks,
            MatrixStack matrixStack,
            IRenderTypeBuffer buffer,
            int combinedLight,
            int combinedOverlay) {

        BridgeDTO bridge = tile.getBridge();

        if (bridge == null) {
            return;
        }

        BlockPos pos = tile.getBlockPos();

        IVertexBuilder builder = buffer.getBuffer(RenderType.lines());

        Matrix4f matrix = matrixStack.last().pose();

        for (BridgeSegmentDTO segment : bridge.getSegments()) {

            Vector3d start = segment.getStart().subtract(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ());

            Vector3d end = segment.getEnd().subtract(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ());

            Vector3d forward = end.subtract(start).normalize();

            /*
             * Construct an arbitrary perpendicular basis.
             */

            Vector3d worldUp = Math.abs(forward.y) > 0.99
                    ? new Vector3d(1, 0, 0)
                    : new Vector3d(0, 1, 0);

            Vector3d right = forward.cross(worldUp).normalize();

            Vector3d up = right.cross(forward).normalize();

            /*
             * Apply roll rotation around forward axis.
             */

            double rollRadians = Math.toRadians(segment.getRoll());

            double cos = Math.cos(rollRadians);
            double sin = Math.sin(rollRadians);

            Vector3d rolledRight = right.scale(cos).add(
                    up.scale(sin));

            Vector3d rolledUp = up.scale(cos).subtract(
                    right.scale(sin));

            double halfWidth = 0.5;
            double halfHeight = 1.0 / 32.0;

            Vector3d width = rolledRight.scale(halfWidth);

            Vector3d height = rolledUp.scale(halfHeight);

            /*
             * Prism corners.
             */

            Vector3d s1 = start.add(width).add(height);

            Vector3d s2 = start.subtract(width).add(height);

            Vector3d s3 = start.subtract(width).subtract(height);

            Vector3d s4 = start.add(width).subtract(height);

            Vector3d e1 = end.add(width).add(height);

            Vector3d e2 = end.subtract(width).add(height);

            Vector3d e3 = end.subtract(width).subtract(height);

            Vector3d e4 = end.add(width).subtract(height);

            /*
             * Render wireframe prism.
             */

            line(builder, matrix, s1, s2);
            line(builder, matrix, s2, s3);
            line(builder, matrix, s3, s4);
            line(builder, matrix, s4, s1);

            line(builder, matrix, e1, e2);
            line(builder, matrix, e2, e3);
            line(builder, matrix, e3, e4);
            line(builder, matrix, e4, e1);

            line(builder, matrix, s1, e1);
            line(builder, matrix, s2, e2);
            line(builder, matrix, s3, e3);
            line(builder, matrix, s4, e4);
        }
    }

    private void line(
            IVertexBuilder builder,
            Matrix4f matrix,
            Vector3d a,
            Vector3d b) {

        builder.vertex(
                matrix,
                (float) a.x,
                (float) a.y,
                (float) a.z)
                .color(1f, 0f, 0f, 1f)
                .endVertex();

        builder.vertex(
                matrix,
                (float) b.x,
                (float) b.y,
                (float) b.z)
                .color(1f, 0f, 0f, 1f)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(
            BridgeEmitterTileEntity tile) {

        return true;
    }
}