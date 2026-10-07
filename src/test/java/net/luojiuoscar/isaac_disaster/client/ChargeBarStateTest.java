package net.luojiuoscar.isaac_disaster.client;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChargeBarStateTest {
    @Test
    void nullIdDoesNotPoisonTheVisibleStateSnapshot() {
        var data = ClientDataManager.getInstance();
        data.init();
        var id = ResourceLocation.fromNamespaceAndPath("test", "valid");
        data.updateChargeBar(id, true, 0.5f);
        assertDoesNotThrow(() -> data.updateChargeBar(null, true, 1f));
        assertEquals(java.util.Map.of(id, 0.5f), assertDoesNotThrow(data::getChargeBars));
        data.init();
    }

    @Test
    void tracksIndependentBarsAndCanShowAnEmptyBar() {
        var data = ClientDataManager.getInstance();
        data.init();
        var a = ResourceLocation.fromNamespaceAndPath("test", "a");
        var b = ResourceLocation.fromNamespaceAndPath("test", "b");
        data.updateChargeBar(a, true, 0);
        data.updateChargeBar(b, true, 0.75f);
        assertEquals(0, data.getChargeBars().get(a));
        assertEquals(0.75f, data.getChargeBars().get(b));
        data.updateChargeBar(a, false, 0);
        assertFalse(data.getChargeBars().containsKey(a));
        assertEquals(0.75f, data.getChargeBars().get(b));
        data.init();
        assertTrue(data.getChargeBars().isEmpty());
    }

    @Test
    void clampsProgressAndRejectsNonFiniteValues() {
        var data = ClientDataManager.getInstance();
        data.init();
        var id = ResourceLocation.fromNamespaceAndPath("test", "bar");
        data.updateChargeBar(id, true, 2);
        assertEquals(1, data.getChargeBars().get(id));
        data.updateChargeBar(id, true, -1);
        assertEquals(0, data.getChargeBars().get(id));
        data.updateChargeBar(id, true, Float.NaN);
        assertFalse(data.getChargeBars().containsKey(id));
        data.init();
    }
}
