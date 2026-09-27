package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrajectorySequenceTest {
    private static final ResourceLocation WIGGLE = ResourceLocation.parse("isaac_disaster:wiggle_worm");
    private static final ResourceLocation GRAVITY = ResourceLocation.parse("isaac_disaster:gravity");

    @Test
    void mergesPositiveStacksWithoutReorderingAndConvertsAmplifierOnce() {
        TrajectorySequence sequence = new TrajectorySequence();
        sequence.add(WIGGLE, 1);
        sequence.add(GRAVITY, 2);
        sequence.add(WIGGLE, 3);
        sequence.add(GRAVITY, 0);
        sequence.add(GRAVITY, -1);

        assertEquals(List.of(new TrajectorySpec(WIGGLE, 3), new TrajectorySpec(GRAVITY, 1)),
                sequence.snapshot());
    }

    @Test
    void copiesAndSnapshotsRemainIndependent() {
        TrajectorySequence sequence = new TrajectorySequence();
        sequence.add(WIGGLE, 1);
        List<TrajectorySpec> snapshot = sequence.snapshot();
        TrajectorySequence copy = sequence.copy();
        copy.add(WIGGLE, 1);
        sequence.add(GRAVITY, 1);

        assertEquals(List.of(new TrajectorySpec(WIGGLE, 0)), snapshot);
        assertEquals(List.of(new TrajectorySpec(WIGGLE, 1)), copy.snapshot());
        assertEquals(2, sequence.snapshot().size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
    }
}
