package net.luojiuoscar.isaac_disaster.registries.attack_type;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttackPipelineModeTest {
    @Test
    void exposesOnlyLifecycleNamedExecutionModes() {
        assertEquals(
                EnumSet.of(
                        AttackPipelineMode.FULL,
                        AttackPipelineMode.PLAN_PREPARE_AND_EXECUTE,
                        AttackPipelineMode.PREPARE_AND_EXECUTE,
                        AttackPipelineMode.EXECUTE_ONLY),
                EnumSet.allOf(AttackPipelineMode.class));
    }
}
