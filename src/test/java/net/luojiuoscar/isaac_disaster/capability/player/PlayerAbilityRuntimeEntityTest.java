package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerAbilityRuntimeEntityTest {
    private static final ResourceLocation KNIFE = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "moms_knife");
    private Field registryValue;
    private Object previousBullet;

    @BeforeEach
    void provideFallbackAttackWithoutForgeBootstrap() throws Exception {
        registryValue = RegistryObject.class.getDeclaredField("value");
        registryValue.setAccessible(true);
        previousBullet = registryValue.get(ModAttackTypes.BULLET);
        registryValue.set(ModAttackTypes.BULLET, TestAttackTypes.BULLET);
    }

    @AfterEach
    void restoreFallbackAttack() throws Exception {
        registryValue.set(ModAttackTypes.BULLET, previousBullet);
    }

    @Test
    void removingAnOldEntityCannotEraseItsReplacement() {
        RuntimeEntityIndex entities = new PlayerAbility().getRuntimeEntities();
        UUID first = UUID.randomUUID();
        UUID replacement = UUID.randomUUID();
        entities.register(KNIFE, first);
        entities.register(KNIFE, replacement);
        assertFalse(entities.unregister(KNIFE, first));
        assertEquals(replacement, entities.getId(KNIFE));
        assertTrue(entities.unregister(KNIFE, replacement));
        assertNull(entities.getId(KNIFE));
    }

    @Test
    void numberedSlotsRemainIndependentAndTheOriginalApiStillAddressesZero() {
        RuntimeEntityIndex entities = new PlayerAbility().getRuntimeEntities();
        UUID first = UUID.randomUUID();
        UUID last = UUID.randomUUID();
        UUID replacement = UUID.randomUUID();
        entities.register(KNIFE, first);
        entities.register(KNIFE, 15, last);
        entities.register(KNIFE, 15, replacement);
        assertEquals(first, entities.getId(KNIFE, 0));
        assertFalse(entities.unregister(KNIFE, 15, last));
        assertEquals(replacement, entities.getId(KNIFE, 15));
        assertTrue(entities.unregister(KNIFE, first));
        assertEquals(replacement, entities.getId(KNIFE, 15));
        entities.discardAll(null);
        assertNull(entities.getId(KNIFE, 15));
    }

    @Test
    void savingAndLoadingPreservesAbilitiesButClearsRuntimeEntities() {
        PlayerAbility ability = new PlayerAbility();
        ability.setPiercing(3);
        ability.getRuntimeEntities().register(KNIFE, UUID.randomUUID());
        CompoundTag saved = new CompoundTag();
        ability.saveNBTData(saved);
        CompoundTag withoutEntities = new CompoundTag();
        PlayerAbility baseline = new PlayerAbility();
        baseline.setPiercing(3);
        baseline.saveNBTData(withoutEntities);
        assertEquals(withoutEntities, saved);
        ability.loadNBTData(saved);
        assertEquals(3, ability.getPiercing());
        assertNull(ability.getRuntimeEntities().getId(KNIFE));
    }

    @Test
    void respawnCopyDoesNotCopyOrRetainRuntimeEntities() {
        PlayerAbility source = new PlayerAbility();
        source.setPiercing(5);
        source.getRuntimeEntities().register(KNIFE, UUID.randomUUID());
        PlayerAbility destination = new PlayerAbility();
        destination.getRuntimeEntities().register(KNIFE, UUID.randomUUID());
        destination.copyFrom(source, null);
        assertEquals(5, destination.getPiercing());
        assertNull(destination.getRuntimeEntities().getId(KNIFE));
        assertNotNull(source.getRuntimeEntities().getId(KNIFE));
    }

    @Test
    void resetAndUnloadingAnUnavailableWorldClearTheIndex() {
        PlayerAbility ability = new PlayerAbility();
        ability.getRuntimeEntities().register(KNIFE, UUID.randomUUID());
        ability.init();
        assertNull(ability.getRuntimeEntities().getId(KNIFE));
        ability.getRuntimeEntities().register(KNIFE, UUID.randomUUID());
        ability.getRuntimeEntities().discardAll(null);
        assertNull(ability.getRuntimeEntities().getId(KNIFE));
    }

    @Test
    void resettingChargeDoesNotForgetAnExistingEntity() {
        PlayerAbility ability = new PlayerAbility();
        UUID knife = UUID.randomUUID();
        ability.getRuntimeEntities().register(KNIFE, knife);
        ability.setChargeAmount(KNIFE, 10);
        ability.clearChargeStates();
        assertEquals(0, ability.getChargeAmount(KNIFE));
        assertEquals(knife, ability.getRuntimeEntities().getId(KNIFE));
    }
}
