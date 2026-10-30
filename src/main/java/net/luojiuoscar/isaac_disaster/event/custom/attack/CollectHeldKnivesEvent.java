package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.HeldMomsKnifeGroup.Pose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;

/** Collects deterministic held entries; listeners must not roll firing chances here. */
public final class CollectHeldKnivesEvent extends Event {
    private final ServerPlayer player;
    private final Map<ResourceLocation, List<Pose>> groups = new LinkedHashMap<>();

    public CollectHeldKnivesEvent(ServerPlayer player) {
        this.player = Objects.requireNonNull(player, "player");
    }

    public ServerPlayer getPlayer() { return player; }

    /** Adds one immutable group per source; list order determines its internal slot order. */
    public void addEntries(ResourceLocation sourceId, List<Pose> poses) {
        groups.putIfAbsent(Objects.requireNonNull(sourceId, "sourceId"), List.copyOf(poses));
    }

    public Map<ResourceLocation, List<Pose>> getGroups() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(groups));
    }
}
