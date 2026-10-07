package net.luojiuoscar.isaac_disaster.client.hud;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.charge_bar.ChargeBarType;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Fixed circular slots: 8, 16, 24...; each ring begins at upper right and proceeds clockwise. */
public final class ChargeBarLayout {
    public static final int GAP = 2;
    private static final int FIRST_RING_CAPACITY = 8;
    private static final double MINIMUM_RADIUS = 12;

    public record Entry(ResourceLocation id, ChargeBarType type, float progress) {
    }

    public record Position(double x, double y) {
    }

    public static List<Entry> sorted(Collection<Entry> entries) {
        if (entries == null) {
            IsaacDisaster.LOGGER.warn("Skipping charge bar layout with no entries collection");
            return List.of();
        }
        return entries.stream().filter(entry -> {
            if (entry == null || entry.id() == null || entry.type() == null) {
                IsaacDisaster.LOGGER.warn("Skipping incomplete charge bar entry: {}", entry);
                return false;
            }
            // Invalid styles were already logged at construction; avoid logging every HUD frame.
            return entry.type().isValid();
        }).sorted(Comparator
                .comparingInt((Entry entry) -> entry.type().priority()).reversed()
                .thenComparing(entry -> entry.id().toString())).toList();
    }

    /** Offsets of the visible indicator's center, in GUI units; null means skip an invalid slot. */
    public static Position position(int index) {
        if (index < 0) {
            IsaacDisaster.LOGGER.warn("Skipping invalid charge bar slot: {}", index);
            return null;
        }
        int ring = 1;
        long slot = index;
        while (slot >= (long) FIRST_RING_CAPACITY * ring) {
            slot -= (long) FIRST_RING_CAPACITY * ring;
            ring++;
        }
        // Transparent corners of circular indicators need no rectangular diagonal clearance.
        double baseRadius = Math.max(MINIMUM_RADIUS,
                (ChargeRingGeometry.SIZE + GAP) / (2 * Math.sin(Math.PI / FIRST_RING_CAPACITY)));
        double radius = baseRadius * ring;
        double angle = -Math.PI / 4 + slot * 2 * Math.PI / (FIRST_RING_CAPACITY * ring);
        return new Position(radius * Math.cos(angle), radius * Math.sin(angle));
    }

    private ChargeBarLayout() {
    }
}
