package net.luojiuoscar.isaac_disaster.registries.split_module;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SplitModulePriorityTest {
    @Test
    void cricketsBodySharesTheFirstApplicableSplitTier() {
        assertEquals(SplitModulePriority.THE_PARASITE.priority(), SplitModulePriority.CRICKETS_BODY.priority());
        assertEquals(SplitModulePriority.COMPOUND_FRACTURE.priority(), SplitModulePriority.CRICKETS_BODY.priority());
    }
}
