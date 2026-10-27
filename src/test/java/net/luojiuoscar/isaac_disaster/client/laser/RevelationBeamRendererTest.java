package net.luojiuoscar.isaac_disaster.client.laser;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.RevelationBeamS2CPacket;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client.HolyLightVisuals;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RevelationBeamRendererTest {
    private static final ResourceLocation DIMENSION = ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");
    private static final UUID OWNER = UUID.randomUUID();
    private static final UUID BEAM = UUID.randomUUID();

    @Test
    void firstClosingSnapshotDisplaysOnlyTheRemainingTwoTicksWithoutAnImpactBurst() throws Exception {
        try (var client = new ClientFixture()) {
            RevelationBeamRenderer.update(beam(28, true));
            assertTrue(map("BEAMS").containsKey(BEAM), "A viewer entering on the final pulse must still see the closing beam");
            var state = (RevelationBeamRenderer.BeamState) map("BEAMS").get(BEAM);
            assertEquals(28, state.age(100));
            assertEquals(0, state.fadeAge(100));
            var ring = new Capture();
            RevelationBeamRenderer.drawImpactRing(ring.consumer, frame(state.age(100), state.fadeAge(100)));
            assertEquals(0, ring.vertices, "Joining at the end must not replay the firing ring");
            RevelationBeamRenderer.onClientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
            RevelationBeamRenderer.update(beam(28, true));
            assertSame(state, map("BEAMS").get(BEAM));
            assertEquals(1, state.fadeAge(101), "A repeated closing snapshot cannot restart its fade");
            RevelationBeamRenderer.onClientTick(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
            assertTrue(map("BEAMS").isEmpty(), "The late closing beam expires after its two remaining ticks");
        }
    }

    @Test
    void firingHasVolumeAcrossBothRadialDirectionsEvenFromASideView() {
        var capture = new Capture();
        RevelationBeamRenderer.drawBeam(capture.consumer, new RevelationBeamRenderer.BeamFrame(
                new Vec3(3, 0, 0), new Vec3(0, 0, 1), 16, 1, 8, -1, new Vec3(3, 0, .8)));
        double spanX = capture.points.stream().mapToDouble(v -> v.x).max().orElseThrow()
                - capture.points.stream().mapToDouble(v -> v.x).min().orElseThrow();
        double spanY = capture.points.stream().mapToDouble(v -> v.y).max().orElseThrow()
                - capture.points.stream().mapToDouble(v -> v.y).min().orElseThrow();
        assertTrue(spanX >= .99, "A beam viewed from the side must have real cylindrical depth");
        assertTrue(spanY >= .99, "The perpendicular radial direction must also retain the beam width");
    }

    @Test
    void firingUsesWhiteLightInsteadOfTheOldGoldGlow() {
        var capture = new Capture();
        RevelationBeamRenderer.drawBeam(capture.consumer, frame(8, -1));
        assertFalse(capture.colors.isEmpty());
        for (float[] color : capture.colors) {
            assertEquals(1F, color[0], .001F);
            assertEquals(color[0], color[1], .001F, "The holy beam has a white core and white edges");
            assertEquals(color[0], color[2], .001F, "The old gold tint must be removed");
        }
        assertTrue(capture.vertices > 4, "An axial view must include the closed cylinder and its end faces");
    }

    @Test
    void firingHoldsTheBrightWideAtlasRowForTheEntireExistingBeamLifetime() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/isaac_disaster/textures/effects/holy_light.png")) {
            assertNotNull(input);
            var atlas = ImageIO.read(input);
            for (double age : new double[]{2, 8, 15, 28, 60}) {
                var capture = new Capture();
                RevelationBeamRenderer.drawBeam(capture.consumer, frame(age, -1));
                int row = (int) (capture.v * atlas.getHeight());
                assertTrue((atlas.getRGB(240, row) >>> 24) / 255D >= .7, "The firing core remains bright");
                assertTrue((atlas.getRGB(232, row) >>> 24) / 255D >= .3, "The beam retains its soft width");
            }
        }
    }

    @Test
    void impactRingEndsAtFourTicksWithoutEndingTheBeam() {
        for (double age : new double[]{0, 1, 3.99}) {
            var ring = new Capture();
            RevelationBeamRenderer.drawImpactRing(ring.consumer, frame(age, -1));
            assertEquals(4, ring.vertices);
        }
        for (double age : new double[]{4, 10, 28}) {
            var capture = new Capture();
            RevelationBeamRenderer.drawImpactRing(capture.consumer, frame(age, -1));
            assertEquals(0, capture.vertices);
            RevelationBeamRenderer.drawBeam(capture.consumer, frame(age, -1));
            assertTrue(capture.vertices > 0);
        }
    }

    @Test
    void lateSnapshotsStartAtTheCurrentAgeAndNeverReplayTheImpactRing() {
        var packet = beam(20, false);
        var state = new RevelationBeamRenderer.BeamState(packet, 100);
        state.update(packet, 100);
        assertEquals(20, state.age(100));
        state.update(packet, 102);
        assertEquals(22, state.age(102), "A duplicate snapshot does not rewind the beam");
        state.update(beam(24, false), 104);
        assertEquals(24, state.age(104));
        var ring = new Capture();
        RevelationBeamRenderer.drawImpactRing(ring.consumer, frame(state.age(104), state.fadeAge(104)));
        assertEquals(0, ring.vertices);
    }

    @Test
    void finishingContractsForTwoTicksAndDuplicatePacketsDoNotExtendIt() {
        var state = new RevelationBeamRenderer.BeamState(beam(0, false), 100);
        state.update(beam(0, false), 100);
        assertEquals(-1, state.fadeAge(127));
        state.update(beam(28, true), 128);
        assertEquals(0, state.fadeAge(128));
        assertEquals(1, state.fadeAge(129));
        state.update(beam(28, true), 129);
        assertEquals(2, state.fadeAge(130));
        assertEquals(31, HolyLightVisuals.firingRow(state.age(130), state.fadeAge(130)));
    }

    @Test
    void worldTransitionsAndLogoutClearFiredBeams() throws Exception {
        Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        ClientLevel dummyWorld = (ClientLevel) unsafe.getClass().getMethod("allocateInstance", Class.class)
                .invoke(unsafe, ClientLevel.class);
        Method useWorld = RevelationBeamRenderer.class.getDeclaredMethod("useWorld", ClientLevel.class);
        useWorld.setAccessible(true);
        try {
            useWorld.invoke(null, dummyWorld);
            map("BEAMS").put(BEAM, new RevelationBeamRenderer.BeamState(beam(0, false), 0));
            useWorld.invoke(null, new Object[]{null});
            assertTrue(map("BEAMS").isEmpty());
        } finally {
            useWorld.invoke(null, new Object[]{null});
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, Object> map(String name) throws Exception {
        Field field = RevelationBeamRenderer.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<UUID, Object>) field.get(null);
    }

    private static RevelationBeamRenderer.BeamFrame frame(double age, double fadeAge) {
        return new RevelationBeamRenderer.BeamFrame(new Vec3(0, 0, 3), new Vec3(0, 0, 1),
                16, 1, age, fadeAge, new Vec3(0, 0, 3.8));
    }

    private static RevelationBeamS2CPacket beam(int age, boolean finished) {
        return new RevelationBeamS2CPacket(BEAM, OWNER, 12, DIMENSION, Vec3.ZERO,
                new Vec3(0, 0, 1), 64, 1, finished, true, false, age);
    }

    private static final class ClientFixture implements AutoCloseable {
        private final Field singleton = Minecraft.class.getDeclaredField("instance");
        private final Object previous;
        private final Method useWorld = RevelationBeamRenderer.class.getDeclaredMethod("useWorld", ClientLevel.class);

        private ClientFixture() throws Exception {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);
            Method allocate = unsafeClass.getMethod("allocateInstance", Class.class);
            Minecraft minecraft = (Minecraft) allocate.invoke(unsafe, Minecraft.class);
            ClientLevel level = (ClientLevel) allocate.invoke(unsafe, ClientLevel.class);
            Field dimension = Level.class.getDeclaredField("dimension");
            dimension.setAccessible(true);
            dimension.set(level, Level.OVERWORLD);
            minecraft.level = level;
            singleton.setAccessible(true);
            previous = singleton.get(null);
            singleton.set(null, minecraft);
            useWorld.setAccessible(true);
            useWorld.invoke(null, level);
            Field clock = RevelationBeamRenderer.class.getDeclaredField("clientTicks");
            clock.setAccessible(true);
            clock.setLong(null, 100);
        }

        @Override
        public void close() throws Exception {
            useWorld.invoke(null, new Object[]{null});
            singleton.set(null, previous);
        }
    }

    private static final class Capture {
        private int vertices;
        private float v;
        private final List<float[]> colors = new ArrayList<>();
        private final List<Vec3> points = new ArrayList<>();
        private final VertexConsumer consumer = (VertexConsumer) Proxy.newProxyInstance(VertexConsumer.class.getClassLoader(),
                new Class<?>[]{VertexConsumer.class}, (proxy, method, args) -> {
                    if (method.getName().equals("vertex") && args.length == 4) {
                        points.add(new Vec3(((Number) args[1]).doubleValue(), ((Number) args[2]).doubleValue(), ((Number) args[3]).doubleValue()));
                    }
                    if (method.getName().equals("color") && args[0] instanceof Float) {
                        colors.add(new float[]{(float) args[0], (float) args[1], (float) args[2]});
                    }
                    if (method.getName().equals("uv")) v = (float) args[1];
                    if (method.getName().equals("endVertex")) vertices++;
                    return method.getReturnType() == VertexConsumer.class ? proxy : null;
                });
    }
}
