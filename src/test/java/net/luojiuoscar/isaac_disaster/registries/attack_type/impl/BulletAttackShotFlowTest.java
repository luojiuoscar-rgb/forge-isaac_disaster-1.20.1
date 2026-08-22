package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BulletAttackShotFlowTest {
    @Test
    void cancelledShootEventPreventsSpawning() {
        RecordingBulletAttack attack = new RecordingBulletAttack();

        attack.allowSpawn = false;
        attack.finishShotForTest(testContext(), null);
        assertFalse(attack.spawnCalled);

        attack.spawnCalled = false;
        attack.allowSpawn = true;
        attack.finishShotForTest(testContext(), null);
        assertTrue(attack.spawnCalled);
    }

    @Test
    void nullOwnerContextIsIgnored() {
        RecordingBulletAttack attack = new RecordingBulletAttack();

        attack.shoot(testContext());

        assertFalse(attack.createBulletCalled);
        assertFalse(attack.spawnCalled);
    }

    private static AttackContext testContext() {
        return new AttackContext(
                null,
                null,
                ResourceLocation.fromNamespaceAndPath("test", "bullet"),
                new CompositeTrigger(),
                Map.of(),
                Vec3.ZERO,
                0.0f,
                0.0f
        );
    }

    private static final class RecordingBulletAttack extends BulletAttack {
        private boolean allowSpawn = true;
        private boolean spawnCalled = false;
        private boolean createBulletCalled = false;

        private RecordingBulletAttack() {
            super(0.0);
        }

        private void finishShotForTest(AttackContext context, TearBullet bullet) {
            finalizeShot(context, bullet);
        }

        @Override
        protected TearBullet createBullet(AttackContext context) {
            createBulletCalled = true;
            return null;
        }

        @Override
        protected boolean postShootEvent(AttackContext context, TearBullet bullet) {
            return allowSpawn;
        }

        @Override
        protected void spawnBullet(AttackContext context, TearBullet bullet) {
            spawnCalled = true;
        }
    }
}
