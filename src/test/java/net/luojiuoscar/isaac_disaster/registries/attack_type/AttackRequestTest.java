package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttackRequestTest {
    @Test
    void generatedRejectsUnsupportedModes() {
        assertThrows(IllegalArgumentException.class, () -> AttackRequest.generated(
                null,
                new RecordingAttackType(),
                AttackOrigin.PLAYER_PRIMARY,
                AttackPipelineMode.BULLET_ONLY,
                true
        ));
    }

    @Test
    void withContextsCopiesProvidedContextsAndRejectsUnsupportedModes() {
        List<AttackContext> contexts = new ArrayList<>(List.of(testContext()));
        AttackRequest request = AttackRequest.withContexts(
                null,
                new RecordingAttackType(),
                AttackOrigin.SYSTEM,
                AttackPipelineMode.BULLET_ONLY,
                contexts,
                false
        );

        contexts.clear();

        assertEquals(1, request.getProvidedContexts().size());
        assertThrows(UnsupportedOperationException.class, () -> request.getProvidedContexts().add(testContext()));
        List<AttackContext> contextsWithNull = new ArrayList<>();
        contextsWithNull.add(null);
        assertThrows(NullPointerException.class, () -> AttackRequest.withContexts(
                null, new RecordingAttackType(), AttackOrigin.SYSTEM,
                AttackPipelineMode.BULLET_ONLY, contextsWithNull, false));
        assertThrows(IllegalArgumentException.class, () -> AttackRequest.withContexts(
                null,
                new RecordingAttackType(),
                AttackOrigin.SYSTEM,
                AttackPipelineMode.FULL,
                List.of(testContext()),
                false
        ));
    }

    private static AttackContext testContext() {
        return new AttackContext(
                null,
                null,
                ResourceLocation.fromNamespaceAndPath("test", "bullet"),
                new CompositeTrigger(),
                Map.of(),
                net.minecraft.world.phys.Vec3.ZERO,
                0.0f,
                0.0f
        );
    }

    private static final class RecordingAttackType extends AttackType {
        private RecordingAttackType() {
            super(0.0);
        }

        @Override
        public ResourceLocation getId() {
            return ResourceLocation.fromNamespaceAndPath("test", "attack");
        }

        @Override
        public List<AttackContext> getAttackContexts(net.minecraft.server.level.ServerPlayer player, int bulletCount) {
            return List.of();
        }

        @Override
        public void performAttack(List<AttackContext> ctxList) {
        }

        @Override
        public void makeSound(LivingEntity entity) {
        }

        @Override
        public void shoot(AttackContext ctx) {
        }

        @Override
        public int getBulletCount(Player player) {
            return 1;
        }
    }
}
