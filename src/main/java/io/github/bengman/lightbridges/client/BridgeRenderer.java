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

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class BridgeRenderer extends TileEntityRenderer<BridgeEmitterTileEntity> {

    private static final double HALF_WIDTH = 0.5;
    private static final double HALF_HEIGHT = 1.0 / 32.0;

    public BridgeRenderer(TileEntityRendererDispatcher dispatcher) {
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

        if (bridge == null || bridge.getSegments().isEmpty()) {
            return;
        }

        BlockPos origin = tile.getBlockPos();

        IVertexBuilder builder = buffer.getBuffer(RenderType.lines());
        Matrix4f matrix = matrixStack.last().pose();

        for (BridgeSegmentDTO segment : bridge.getSegments()) {

            Vector3d start = segment.getStart().subtract(
                    origin.getX(),
                    origin.getY(),
                    origin.getZ());

            Vector3d end = segment.getEnd().subtract(
                    origin.getX(),
                    origin.getY(),
                    origin.getZ());

            Direction forward = segment.getDirection();
            Direction up = segment.getUp();

            Vector3d forwardVector = directionToVector(forward);
            Vector3d upVector = directionToVector(up);

            Vector3d rightVector = forwardVector.cross(upVector);

            Vector3d width = rightVector.scale(HALF_WIDTH);
            Vector3d height = upVector.scale(HALF_HEIGHT);

            Vector3d s1 = start.add(width).add(height);
            Vector3d s2 = start.subtract(width).add(height);
            Vector3d s3 = start.subtract(width).subtract(height);
            Vector3d s4 = start.add(width).subtract(height);

            Vector3d e1 = end.add(width).add(height);
            Vector3d e2 = end.subtract(width).add(height);
            Vector3d e3 = end.subtract(width).subtract(height);
            Vector3d e4 = end.add(width).subtract(height);

            drawBox(builder, matrix,
                    s1, s2, s3, s4,
                    e1, e2, e3, e4);
        }
    }

    private void drawBox(
            IVertexBuilder builder,
            Matrix4f matrix,
            Vector3d s1,
            Vector3d s2,
            Vector3d s3,
            Vector3d s4,
            Vector3d e1,
            Vector3d e2,
            Vector3d e3,
            Vector3d e4) {

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

    private void line(
            IVertexBuilder builder,
            Matrix4f matrix,
            Vector3d start,
            Vector3d end) {

        builder.vertex(
                matrix,
                (float) start.x,
                (float) start.y,
                (float) start.z)
                .color(0, 0, 255, 255)
                .endVertex();

        builder.vertex(
                matrix,
                (float) end.x,
                (float) end.y,
                (float) end.z)
                .color(0, 0, 255, 255)
                .endVertex();
    }

    private Vector3d directionToVector(Direction direction) {

        return new Vector3d(
                direction.getStepX(),
                direction.getStepY(),
                direction.getStepZ());
    }
}