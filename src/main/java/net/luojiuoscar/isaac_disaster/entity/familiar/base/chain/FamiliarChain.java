package net.luojiuoscar.isaac_disaster.entity.familiar.base.chain;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Per-owner, per-tick positions in cross-type admission order; values hold no player/entity references. */
final class FamiliarChain {
    static final double SPACING = 1.0;
    private static final Map<ServerPlayer, Frame> FRAMES = new WeakHashMap<>();

    private FamiliarChain() { }

    record Link(int slot, Vec3 target, Vec3 outward) {
        Vec3 placement() { return target.add(outward.scale(SPACING)); }
    }

    /** Unregistered newcomers resolve after existing chain followers, so admission and recall use the same slots. */
    static Link link(ServerPlayer owner, AbstractIsaacFamiliarEntity familiar) {
        PlayerFamiliarData data = owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).resolve().orElse(null);
        Vec3 hover = owner.position().add(0, owner.getBbHeight() * 0.55, 0);
        Vec3 behind = Vec3.directionFromRotation(0, owner.getYRot()).scale(-1).normalize();
        if (data == null) return new Link(0, hover, behind);
        Frame frame = FRAMES.computeIfAbsent(owner, ignored -> new Frame());
        frame.refresh(owner, data, hover, behind);
        int slot = frame.slots.getOrDefault(familiar.getUUID(), frame.members.size());
        if (slot == 0) return new Link(slot, hover, behind);
        Member previous = frame.members.get(slot - 1);
        return new Link(slot, previous.position(), previous.outward());
    }

    private record Member(Vec3 position, Vec3 outward) { }

    private static final class Frame {
        private long tick = Long.MIN_VALUE;
        private long revision = Long.MIN_VALUE;
        private final Map<UUID, Integer> slots = new HashMap<>();
        private final List<Member> members = new ArrayList<>();

        void refresh(ServerPlayer owner, PlayerFamiliarData data, Vec3 hover, Vec3 behind) {
            long now = owner.level().getGameTime();
            if (tick == now && revision == data.getRuntimeRosterRevision()) return;
            tick = now;
            revision = data.getRuntimeRosterRevision();
            slots.clear();
            members.clear();
            Vec3 target = hover;
            Vec3 outward = behind;
            for (UUID id : data.getEntityIdsInAdmissionOrder()) {
                if (!(owner.serverLevel().getEntity(id) instanceof AbstractChainFamiliarEntity familiar) || familiar.isRemoved() || familiar.getOwner() != owner
                        || familiar.level() != owner.level() || !data.containsEntity(familiar.getFamiliarType(), id)) continue;
                Vec3 offset = familiar.position().subtract(target);
                if (offset.lengthSqr() > 1e-10) outward = offset.normalize();
                slots.put(id, members.size());
                members.add(new Member(familiar.position(), outward));
                target = familiar.position();
            }
        }
    }
}
