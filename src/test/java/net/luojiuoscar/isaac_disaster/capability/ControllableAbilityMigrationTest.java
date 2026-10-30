package net.luojiuoscar.isaac_disaster.capability;

import java.lang.reflect.Field;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbility;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.RegistryObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControllableAbilityMigrationTest {
    @Test
    void legacyBeamCountBecomesControllableAndIsNotPersistedAgain() throws Exception {
        Field value = RegistryObject.class.getDeclaredField("value");
        value.setAccessible(true);
        Object previous = value.get(ModAttackTypes.BULLET);
        value.set(ModAttackTypes.BULLET, TestAttackTypes.BULLET);
        try {
            PlayerAbility ability = new PlayerAbility();
            ability.setControllable(2);
            CompoundTag tag = new CompoundTag();
            ability.saveNBTData(tag);
            tag.putInt("tractor", 1);
            ability.loadNBTData(tag);
            assertEquals(3, ability.getControllable());
            ability.saveNBTData(tag);
            assertFalse(tag.contains("tractor"));
            ability.loadNBTData(tag);
            assertEquals(3, ability.getControllable());
        } finally {
            value.set(ModAttackTypes.BULLET, previous);
        }
    }
}
