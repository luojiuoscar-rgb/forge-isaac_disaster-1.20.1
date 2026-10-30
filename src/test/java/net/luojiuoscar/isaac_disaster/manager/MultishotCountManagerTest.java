package net.luojiuoscar.isaac_disaster.manager;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultishotCountManagerTest {
    @ParameterizedTest
    @CsvSource({"0,0,0,0", "0,0,1,1", "0,0,3,1", "1,0,0,2", "0,1,0,3", "1,1,0,4",
            "1,0,1,2", "0,1,1,3", "1,0,3,4", "2,2,2,8", "0,0,-5,0", "-1,1,0,3"})
    void itemCombinationsKeepTheirExistingNonadditiveFormula(int eyes, int spiders, int visions, int expected) {
        assertEquals(expected, MultishotCountManager.bonus(eyes, spiders, visions));
    }

    @Test
    void largeRecoverableInputsDoNotOverflowIntoNegativeBonuses() {
        assertEquals(Integer.MAX_VALUE, MultishotCountManager.bonus(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
}
