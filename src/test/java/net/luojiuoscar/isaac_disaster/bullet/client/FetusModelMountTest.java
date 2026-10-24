package net.luojiuoscar.isaac_disaster.bullet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.FetusMomsKnifeState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FetusModelMountTest {
    @Test
    void theRealYoungHumanoidChestAndTheGameplayKnifeShareTheSameHeight() {
        for (HumanoidModel<?> model : models()) {
            assertTrue(model.young);
            model.setAllVisible(false);
            model.body.visible = true;
            for (double scale : new double[]{0.5, 1, 2}) {
                for (Vec3 forward : new Vec3[]{new Vec3(0, 0, 1), new Vec3(1, 0, 0)}) {
                    Capture vertices = render(model, forward, scale);
                    var blade = FetusMomsKnifeState.blade(Vec3.ZERO, forward, scale, 0.21 * scale);
                    assertTrue(vertices.count > 0);
                    assertEquals((vertices.minY + vertices.maxY) / 2, blade.anchor().y, 1e-6,
                            "The blade handle must be at the rendered baby's chest height");
                }
            }
        }
    }

    @Test
    void theKnifeLengthMatchesTheRenderedBabyRatherThanTheAdultModelScale() {
        for (HumanoidModel<?> model : models()) {
            for (double scale : new double[]{0.5, 1, 2}) {
                Capture vertices = render(model, new Vec3(0, 0, 1), scale);
                var blade = FetusMomsKnifeState.blade(Vec3.ZERO, new Vec3(0, 0, 1), scale, 0.21 * scale);
                double height = vertices.maxY - vertices.minY;
                assertEquals(height, blade.tip().distanceTo(blade.anchor()), 0.01 * scale,
                        "The knife should be as long as the visible fetus, including small clothing inflation");
                assertTrue(blade.anchor().y > vertices.minY && blade.anchor().y < vertices.maxY);
            }
        }
    }

    private static HumanoidModel<?>[] models() {
        return new HumanoidModel<?>[]{
                new PlayerModel<Player>(LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64).bakeRoot(), false),
                new PlayerModel<Player>(LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64).bakeRoot(), true),
                new SkeletonModel<Skeleton>(SkeletonModel.createBodyLayer().bakeRoot())
        };
    }

    private static Capture render(HumanoidModel<?> model, Vec3 forward, double scale) {
        PoseStack poses = new PoseStack();
        BulletClientRenderer.applyFetusPose(poses, forward, scale);
        Capture vertices = new Capture();
        model.renderToBuffer(poses, vertices, 0, 0, 1, 1, 1, 1);
        return vertices;
    }

    private static final class Capture implements VertexConsumer {
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        int count;
        @Override public VertexConsumer vertex(double x, double y, double z) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            count++;
            return this;
        }
        @Override public VertexConsumer color(int red, int green, int blue, int alpha) { return this; }
        @Override public VertexConsumer uv(float u, float v) { return this; }
        @Override public VertexConsumer overlayCoords(int u, int v) { return this; }
        @Override public VertexConsumer uv2(int u, int v) { return this; }
        @Override public VertexConsumer normal(float x, float y, float z) { return this; }
        @Override public void endVertex() {}
        @Override public void defaultColor(int red, int green, int blue, int alpha) {}
        @Override public void unsetDefaultColor() {}
    }
}
