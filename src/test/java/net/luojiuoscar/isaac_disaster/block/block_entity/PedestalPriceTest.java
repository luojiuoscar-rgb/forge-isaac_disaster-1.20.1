package net.luojiuoscar.isaac_disaster.block.block_entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedestalPriceTest {
    @Test
    void freePriceHasNoPaymentType() {
        PedestalPrice price = PedestalPrice.free();

        assertEquals(PedestalPrice.Type.FREE, price.type());
        assertEquals(0, price.amount());
        assertTrue(price.isFree());
    }

    @Test
    void paidPriceHasExactlyOnePaymentType() {
        PedestalPrice life = PedestalPrice.life(2);
        PedestalPrice money = PedestalPrice.money(15);

        assertEquals(PedestalPrice.Type.LIFE, life.type());
        assertEquals(2, life.amount());
        assertFalse(life.isFree());
        assertEquals(PedestalPrice.Type.MONEY, money.type());
        assertEquals(15, money.amount());
        assertFalse(money.isFree());
    }

    @Test
    void negativePriceFallsBackToFree() {
        assertEquals(PedestalPrice.free(), PedestalPrice.life(-1));
        assertEquals(PedestalPrice.free(), PedestalPrice.money(-1));
    }

    @Test
    void nullTypeFallsBackToFree() {
        assertEquals(PedestalPrice.free(), new PedestalPrice(null, 10));
    }

    @Test
    void nonzeroFreePriceFallsBackToFree() {
        assertEquals(PedestalPrice.free(), new PedestalPrice(PedestalPrice.Type.FREE, 10));
    }

    @Test
    void zeroPaidPriceNormalizesToFree() {
        assertEquals(PedestalPrice.free(), PedestalPrice.life(0));
        assertEquals(PedestalPrice.free(), PedestalPrice.money(0));
        assertEquals(PedestalPrice.Type.FREE, new PedestalPrice(PedestalPrice.Type.LIFE, 0).type());
    }
}
