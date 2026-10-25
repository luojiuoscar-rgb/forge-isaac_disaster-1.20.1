package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightPhase;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises the renderer's real UV output and real atlas instead of only phase duration. */
class HolyLightRendererTest {
    private HolyLightSnapshot snapshot(int warningTicks) {
        return new HolyLightSnapshot(1, Vec3.ZERO, .8, 16, 100, warningTicks, true, 99);
    }

    private Capture beam(double age, int warningTicks) throws ReflectiveOperationException {
        var capture = new Capture();
        Method draw = HolyLightRenderer.class.getDeclaredMethod("drawBeam", VertexConsumer.class,
                HolyLightSnapshot.class, Vec3.class, HolyLightPhase.class,
                double.class, double.class, double.class);
        draw.setAccessible(true);
        draw.invoke(null, capture.consumer, snapshot(warningTicks), Vec3.ZERO, HolyLightPhase.FIRING, age, 1D, 0D);
        return capture;
    }

    private Capture ring(double age, HolyLightPhase phase, int warningTicks) throws ReflectiveOperationException {
        var capture = new Capture();
        Method draw = HolyLightRenderer.class.getDeclaredMethod("drawRing", VertexConsumer.class,
                HolyLightSnapshot.class, Vec3.class, HolyLightPhase.class, double.class);
        draw.setAccessible(true);
        draw.invoke(null, capture.consumer, snapshot(warningTicks), Vec3.ZERO, phase, age);
        return capture;
    }

    private BufferedImage atlas() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/isaac_disaster/textures/effects/holy_light.png")) {
            assertNotNull(input);
            return ImageIO.read(input);
        }
    }

    private double coreAlpha(BufferedImage atlas, Capture capture) {
        assertEquals(4, capture.vertices);
        int row = (int) (capture.v * atlas.getHeight());
        return (atlas.getRGB(240, row) >>> 24) / 255D;
    }

    @Test
    void beamRetainsABrightCoreUntilTheFinalTwoTicks() throws Exception {
        BufferedImage atlas = atlas();
        for (int warningTicks : new int[]{0, 35, 70}) {
            for (double age : new double[]{2, 7.5, 12, 13}) {
                Capture capture = beam(age, warningTicks);
                assertTrue(coreAlpha(atlas, capture) >= .7,
                        "A visible firing core must remain bright at tick " + age);
                int row = (int) (capture.v * atlas.getHeight());
                assertTrue((atlas.getRGB(232, row) >>> 24) / 255D >= .3,
                        "The beam must also keep its width instead of becoming a thin line at tick " + age);
            }
        }
    }

    @Test
    void theFinalContractionDoesNotStartUntilTheEndOfFiring() throws Exception {
        BufferedImage atlas = atlas();
        assertTrue(coreAlpha(atlas, beam(12.99, 35)) >= .7);
        assertTrue(coreAlpha(atlas, beam(14.5, 35)) < .6);
    }

    @Test
    void impactRingFinishesWithinFourTicksWhileTheBeamContinues() throws ReflectiveOperationException {
        assertEquals(4, ring(0, HolyLightPhase.FIRING, 35).vertices);
        assertEquals(4, ring(3, HolyLightPhase.FIRING, 35).vertices);
        for (double age : new double[]{4, 5, 10, 14.99}) {
            assertEquals(0, ring(age, HolyLightPhase.FIRING, 35).vertices,
                    "The impact ring must finish at 0.2 seconds, independently of the beam");
            assertEquals(4, beam(age, 35).vertices);
        }
    }

    @Test
    void warningRingStillUsesItsOwnScaledWarningDuration() throws ReflectiveOperationException {
        assertEquals(4, ring(69, HolyLightPhase.WARNING, 70).vertices);
        assertEquals(4, ring(17, HolyLightPhase.WARNING, 18).vertices);
    }

    private float scorchAlpha(HolyLightSnapshot snapshot, double age) throws ReflectiveOperationException {
        Method alpha = HolyLightRenderer.class.getDeclaredMethod("scorchAlpha", HolyLightSnapshot.class, double.class);
        alpha.setAccessible(true);
        return (float) alpha.invoke(null, snapshot, age);
    }

    @Test
    void scorchAppearsWhenFiringStartsAndFadesOnlyAfterFiringEnds() throws ReflectiveOperationException {
        for (int warningTicks : new int[]{0, 35, 70}) {
            HolyLightSnapshot snapshot = snapshot(warningTicks);
            if (warningTicks > 0) assertEquals(0, scorchAlpha(snapshot, warningTicks - .01));
            assertEquals(1, scorchAlpha(snapshot, warningTicks));
            assertEquals(1, scorchAlpha(snapshot, warningTicks + 14.99));
            assertEquals(1, scorchAlpha(snapshot, warningTicks + 15));
            assertTrue(scorchAlpha(snapshot, warningTicks + 16) < 1);
            assertEquals(.5F, scorchAlpha(snapshot, warningTicks + 65), .0001F);
            assertEquals(0, scorchAlpha(snapshot, warningTicks + 115));
        }
    }

    @Test
    void airborneStrikesNeverDrawScorch() throws ReflectiveOperationException {
        var air = new HolyLightSnapshot(1, Vec3.ZERO, .8, 16, 100, 35, false, 99);
        for (double age : new double[]{0, 34.99, 35, 49.99, 50, 100}) {
            assertEquals(0, scorchAlpha(air, age));
        }
    }

    private static final class Capture {
        private int vertices;
        private float v;
        private final VertexConsumer consumer = (VertexConsumer) Proxy.newProxyInstance(
                VertexConsumer.class.getClassLoader(), new Class<?>[]{VertexConsumer.class}, (proxy, method, args) -> {
                    if (method.getName().equals("uv") && args.length == 2) v = (float) args[1];
                    if (method.getName().equals("endVertex")) vertices++;
                    return method.getReturnType() == VertexConsumer.class ? proxy : null;
                });
    }
}
