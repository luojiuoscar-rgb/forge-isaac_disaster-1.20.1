package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.BulletAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.NeptunusAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.tags.IChargeableAttack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class AttackChargeDispatchTest {
    @Test
    void defaultReleaseClearsButNeptunusCanKeepChargeByOverriding() {
        var ordinary = new RecordingChargeAttack();
        ordinary.handleChargeInput(null, false);
        assertEquals(1, ordinary.clears);

        var cleared = new AtomicBoolean();
        var neptunus = new NeptunusAttack(0.0) {
            @Override public boolean isChargeEligible(ServerPlayer player) { return true; }
            @Override public void clearCharge(ServerPlayer player) { cleared.set(true); }
            @Override public void syncCharge(ServerPlayer player) { }
        };
        neptunus.handleChargeInput(null, true);
        neptunus.handleChargeInput(null, false);
        assertFalse(cleared.get(), "Dispatch must respect Neptunus's input overrides");
    }

    @Test
    void losingEligibilityClearsChargeWithoutTickingTheAttack() {
        var attack = new RecordingChargeAttack();
        attack.tickAttack(null);
        assertEquals(1, attack.ticks);
        attack.eligible = false;
        attack.tickAttack(null);
        attack.handleChargeInput(null, false);
        assertEquals(1, attack.ticks);
        assertEquals(2, attack.clears);
    }

    private static final class RecordingChargeAttack extends BulletAttack implements IChargeableAttack {
        private boolean eligible = true;
        private int clears;
        private int ticks;

        private RecordingChargeAttack() { super(0.0); }

        @Override public int getTotalCharge(Player player) { return 47; }
        @Override public boolean isChargeEligible(ServerPlayer player) { return eligible; }
        @Override public void clearCharge(ServerPlayer player) { clears++; }
        @Override public void syncCharge(ServerPlayer player) { }
        @Override public void onTick(ServerPlayer player) { ticks++; }
    }
}
