package net.luojiuoscar.isaac_disaster.client.gui;

import net.luojiuoscar.isaac_disaster.client.gui.charge_bar.ChargeBarPrediction;
import net.luojiuoscar.isaac_disaster.networking.packet.ChargeBarUpdateS2CPacket.Action;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChargeBarPredictionTest {
    @Test
    void advancesBetweenPacketsAndWaitsForAuthoritativeFullCharge() {
        var state = new ChargeBarPrediction();
        state.update(Action.START, 0f, 1f / 47, 0);
        assertEquals(0.5f / 47, state.sample(0.5), 0.00001);
        assertEquals(46f / 47, state.sample(46), 0.00001);
        assertTrue(state.sample(47) < 1f);
        assertTrue(state.sample(100) < 1f);
        state.update(Action.END, 1f, 0f, 100);
        assertEquals(1f, state.sample(100));
        assertEquals(1f, state.sample(110));
    }

    @Test
    void blendsSmallCorrectionsWithoutAnArrivalJump() {
        var state = new ChargeBarPrediction();
        state.update(Action.START, 0.2f, 0.01f, 0);
        assertEquals(0.3f, state.sample(10), 0.00001);
        state.update(Action.CORRECT, 0.28f, 0.01f, 10);
        assertEquals(0.3f, state.sample(10), 0.00001);
        assertEquals(0.31f, state.sample(13), 0.00001);
    }

    @Test
    void resetsConsumptionStopsAndLargeCorrectionsImmediately() {
        var state = new ChargeBarPrediction();
        state.update(Action.START, 0.8f, 0.01f, 0);
        state.update(Action.START, 0f, 0.01f, 10);
        assertEquals(0f, state.sample(10));
        state.update(Action.END, 0.4f, 0f, 11);
        assertEquals(0.4f, state.sample(50));
        state.update(Action.CORRECT, 0.8f, 0.01f, 50);
        assertEquals(0.8f, state.sample(50));
        state.update(Action.END, 0f, 0f, 51);
        assertEquals(0f, state.sample(100));
    }

    @Test
    void barsAdvanceIndependently() {
        var first = new ChargeBarPrediction();
        var second = new ChargeBarPrediction();
        first.update(Action.START, 0f, 0.01f, 0);
        second.update(Action.START, 0.5f, 0.02f, 0);
        assertEquals(0.1f, first.sample(10), 0.00001);
        assertEquals(0.7f, second.sample(10), 0.00001);
        first.update(Action.END, 0f, 0f, 10);
        assertEquals(0.7f, second.sample(10), 0.00001);
    }
}
