package net.luojiuoscar.isaac_disaster.client;

import net.minecraft.resources.ResourceLocation;
import net.luojiuoscar.isaac_disaster.networking.packet.ChargeBarUpdateS2CPacket.Action;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChargeBarStateTest {
    @Test
    void nullIdDoesNotPoisonTheVisibleStateSnapshot() {
        var data = ClientDataManager.getInstance();
        data.init();
        var id = ResourceLocation.fromNamespaceAndPath("test", "valid");
        data.updateChargeBar(id, Action.END, true, 0.5f, 0f, 0f);
        assertDoesNotThrow(() -> data.updateChargeBar(null, Action.END, true, 1f, 0f, 0f));
        var snapshot = assertDoesNotThrow(() -> {
            return data.getChargeBars(0f);
        });
        assertEquals(java.util.Map.of(id, 0.5f), snapshot);
        data.init();
    }

    @Test
    void tracksIndependentBarsAndCanShowAnEmptyBar() {
        var data = ClientDataManager.getInstance();
        data.init();
        var a = ResourceLocation.fromNamespaceAndPath("test", "a");
        var b = ResourceLocation.fromNamespaceAndPath("test", "b");
        data.updateChargeBar(a, Action.END, true, 0, 0f, 0f);
        data.updateChargeBar(b, Action.END, true, 0.75f, 0f, 0f);
        assertEquals(0, data.getChargeBars(0f).get(a));
        assertEquals(0.75f, data.getChargeBars(0f).get(b));
        data.updateChargeBar(a, Action.END, false, 0, 0f, 0f);
        assertFalse(data.getChargeBars(0f).containsKey(a));
        assertEquals(0.75f, data.getChargeBars(0f).get(b));
        data.init();
        assertTrue(data.getChargeBars(0f).isEmpty());
    }

    @Test
    void clampsProgressAndRejectsNonFiniteValues() {
        var data = ClientDataManager.getInstance();
        data.init();
        var id = ResourceLocation.fromNamespaceAndPath("test", "bar");
        data.updateChargeBar(id, Action.END, true, 2, 0f, 0f);
        assertEquals(1, data.getChargeBars(0f).get(id));
        data.updateChargeBar(id, Action.END, true, -1, 0f, 0f);
        assertEquals(0, data.getChargeBars(0f).get(id));
        data.updateChargeBar(id, Action.END, true, Float.NaN, 0f, 0f);
        assertFalse(data.getChargeBars(0f).containsKey(id));
        data.init();
    }

    @Test
    void sharedClockPredictsBothBarsAndHideCancelsOnlyItsOwnPrediction() {
        var data = ClientDataManager.getInstance();
        data.init();
        var a = ResourceLocation.fromNamespaceAndPath("test", "predicted_a");
        var b = ResourceLocation.fromNamespaceAndPath("test", "predicted_b");
        data.updateChargeBar(a, Action.START, true, 0f, 0.01f, 0f);
        data.updateChargeBar(b, Action.START, true, 0.5f, 0.02f, 0f);
        for (int i = 0; i < 10; i++) data.tickChargeBars();
        assertEquals(0.105f, data.getChargeBars(0.5f).get(a), 0.00001);
        assertEquals(0.71f, data.getChargeBars(0.5f).get(b), 0.00001);
        data.updateChargeBar(a, Action.END, false, 0f, 0f, 0f);
        assertFalse(data.getChargeBars(0f).containsKey(a));
        assertEquals(0.7f, data.getChargeBars(0f).get(b), 0.00001);
        data.clearChargeBars();
        assertTrue(data.getChargeBars(0f).isEmpty());
        data.init();
    }
}
