package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Stateless atlas drawing in camera-relative coordinates, usable along any beam axis. */
public final class HolyLightVisuals {
    public static final int IMPACT_RING_TICKS = 4;
    public static final int BEAM_EXPAND_TICKS = 2;
    public static final int BEAM_FADE_TICKS = 2;
    private static final int BEAM_HOLD_ROW = 10;
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final float BEAM_U0 = 224.5F / 256;
    private static final float BEAM_U1 = 255.5F / 256;
    private static final int CYLINDER_SIDES = 16;
    private static final double[] CYLINDER_COS = new double[CYLINDER_SIDES + 1];
    private static final double[] CYLINDER_SIN = new double[CYLINDER_SIDES + 1];

    static {
        for (int i = 0; i <= CYLINDER_SIDES; i++) {
            double angle = i * Math.PI * 2 / CYLINDER_SIDES;
            CYLINDER_COS[i] = Math.cos(angle);
            CYLINDER_SIN[i] = Math.sin(angle);
        }
    }

    private HolyLightVisuals() { }

    public static RenderType lightRenderType() {
        return HolyLightRenderTypes.LIGHT;
    }

    /** A negative fade age holds the bright profile; finishing is independent of beam lifetime. */
    public static int firingRow(double age, double fadeAge) {
        if (fadeAge >= 0) {
            return Math.min(31, BEAM_HOLD_ROW + (int) (fadeAge * (32 - BEAM_HOLD_ROW) / BEAM_FADE_TICKS));
        }
        if (age < BEAM_EXPAND_TICKS) {
            return Math.max(0, Math.min(BEAM_HOLD_ROW, (int) (age * (BEAM_HOLD_ROW + 1) / BEAM_EXPAND_TICKS)));
        }
        return BEAM_HOLD_ROW;
    }

    /** Draws the warning or impact atlas frame on the plane spanned by two unit vectors. */
    public static void drawRing(VertexConsumer vertices, Vec3 center, Vec3 right, Vec3 up,
                                double radius, boolean warning, double progress) {
        int frame = (int) (Math.max(0, Math.min(1, progress)) * 11);
        frame = Math.min(10, frame);
        float u0 = (frame * 16 + .5F) / 256;
        float u1 = (frame * 16 + 15.5F) / 256;
        float v0 = (warning ? .5F : 16.5F) / 32;
        float v1 = (warning ? 15.5F : 31.5F) / 32;
        Vec3 x = right.scale(radius);
        Vec3 y = up.scale(radius);
        vertex(vertices, center.subtract(x).subtract(y), u0, v0, 1);
        vertex(vertices, center.subtract(x).add(y), u0, v1, 1);
        vertex(vertices, center.add(x).add(y), u1, v1, 1);
        vertex(vertices, center.add(x).subtract(y), u1, v0, 1);
    }

    /** Samples one horizontal atlas row across a white beam's width. */
    public static void drawBeam(VertexConsumer vertices, Vec3 start, Vec3 end, Vec3 side,
                                double halfWidth, int row, float alpha) {
        Vec3 offset = side.scale(halfWidth);
        float v = (Math.max(0, Math.min(31, row)) + .5F) / 32;
        vertex(vertices, start.subtract(offset), BEAM_U0, v, alpha);
        vertex(vertices, start.add(offset), BEAM_U1, v, alpha);
        vertex(vertices, end.add(offset), BEAM_U1, v, alpha);
        vertex(vertices, end.subtract(offset), BEAM_U0, v, alpha);
    }

    public static Vec3 perpendicular(Vec3 axis) {
        Vec3 reference = Math.abs(axis.y) < .9D ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        return axis.cross(reference).normalize();
    }

    /** Rotates a billboard around its beam axis, with a stable fallback for axial views. */
    public static Vec3 billboardSide(Vec3 axis, Vec3 cameraRelativeStart) {
        Vec3 side = axis.cross(cameraRelativeStart);
        return side.lengthSqr() < 1.0E-12D ? perpendicular(axis) : side.normalize();
    }

    /** Draws a closed white cylinder; the caller controls expansion and contraction through radius. */
    public static void drawCylinder(VertexConsumer vertices, Vec3 start, Vec3 end, Vec3 axis,
                                  double radius, int row, float alpha) {
        if (alpha <= 0 || radius <= 0) return;
        Vec3 right = perpendicular(axis);
        Vec3 up = axis.cross(right).normalize();
        float v = (Math.max(0, Math.min(31, row)) + .5F) / 32;
        float u = (BEAM_U0 + BEAM_U1) * .5F;
        for (int i = 0; i < CYLINDER_SIDES; i++) {
            Vec3 a = right.scale(CYLINDER_COS[i] * radius).add(up.scale(CYLINDER_SIN[i] * radius));
            Vec3 b = right.scale(CYLINDER_COS[i + 1] * radius).add(up.scale(CYLINDER_SIN[i + 1] * radius));
            vertex(vertices, start.add(a), u, v, alpha);
            vertex(vertices, start.add(b), u, v, alpha);
            vertex(vertices, end.add(b), u, v, alpha);
            vertex(vertices, end.add(a), u, v, alpha);

            vertex(vertices, start, u, v, alpha);
            vertex(vertices, start.add(b), u, v, alpha);
            vertex(vertices, start.add(a), u, v, alpha);
            vertex(vertices, start, u, v, alpha);

            vertex(vertices, end, u, v, alpha);
            vertex(vertices, end.add(a), u, v, alpha);
            vertex(vertices, end.add(b), u, v, alpha);
            vertex(vertices, end, u, v, alpha);
        }
    }

    private static void vertex(VertexConsumer vertices, Vec3 point, float u, float v, float alpha) {
        vertices.vertex(IDENTITY, (float) point.x, (float) point.y, (float) point.z)
                .color(1F, 1F, 1F, alpha).uv(u, v).endVertex();
    }
}
