package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect;

import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.PotionPuddleEffect;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import net.minecraft.world.effect.MobEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class GroundEffectPotionTest {
    @Test
    void potionPuddlesOptIntoNeutralContactWithoutChangingDamageTypes() {
        var type = new GroundEffectType<>(PotionPuddleEffect::new);
        var effect = type.create(1, 2, null, Vec3.ZERO, 1, 600, 0, 3);
        effect.setColor(0x000000);
        assertSame(type, effect.getType());
        assertEquals(0, effect.getColor());
        assertEquals(0, effect.damage());
        assertTrue(effect.affectsAllLivingEntities());
        assertFalse(GroundEffectTestFixtures.WATER.create(2, 1, null, Vec3.ZERO, 1, 100, 1, 4).affectsAllLivingEntities());
        assertFalse(GroundEffectTestFixtures.LEMON.create(3, 1, null, Vec3.ZERO, 1, 100, 1, 5).affectsAllLivingEntities());
    }
    @Test
    void colorNormalizesToRgbAndOnlyActualChangesInvalidateTheClientSnapshot() {
        var type = new GroundEffectType<>(PotionPuddleEffect::new);
        var effect = type.create(1, 2, null, Vec3.ZERO, 1, 600, 0, 3);
        assertEquals(0xFFFFFF, effect.getColor());
        int revision = effect.revision();
        effect.setColor(0xFF112233);
        assertEquals(0x112233, effect.getColor());
        assertEquals(revision + 1, effect.revision());
        effect.setColor(0xAA112233);
        assertEquals(revision + 1, effect.revision());
    }

    @Test
    void potionConfigurationIsImmutableAndInvalidUpdatesKeepThePreviousEffects() {
        var type = new GroundEffectType<>(PotionPuddleEffect::new);
        var effect = type.create(1, 2, null, Vec3.ZERO, 1, 600, 0, 3);
        var slow = new PotionProfile(MobEffects.MOVEMENT_SLOWDOWN, 100, 1);
        var weak = new PotionProfile(MobEffects.WEAKNESS, 60, 0);
        var profiles = new ArrayList<>(List.of(slow, weak));
        effect.setPotions(profiles);
        profiles.clear();
        assertEquals(List.of(slow, weak), effect.getPotions());
        effect.setPotions(Arrays.asList(slow, null));
        assertEquals(List.of(slow, weak), effect.getPotions());
        effect.setPotions(null);
        assertTrue(effect.getPotions().isEmpty());
    }
}
