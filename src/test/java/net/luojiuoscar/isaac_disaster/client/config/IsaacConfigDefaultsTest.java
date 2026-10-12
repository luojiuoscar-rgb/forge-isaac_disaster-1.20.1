package net.luojiuoscar.isaac_disaster.client.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.config.IsaacClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IsaacConfigDefaultsTest {
    @AfterEach
    void unload() {
        Config.SPEC.setConfig(null);
        IsaacClientConfig.SPEC.setConfig(null);
    }

    @Test
    void newConfigsStartWithTheSameValuesAsEveryResetButton() {
        CommentedConfig common = CommentedConfig.inMemory();
        common.set("items", List.of());
        Config.SPEC.setConfig(common);
        IsaacClientConfig.SPEC.setConfig(CommentedConfig.inMemory());
        for (IsaacConfigEntry<?> entry : IsaacConfigCatalog.entries()) {
            assertEquals(entry.defaultValue(), entry.value().get(), entry.id());
        }
    }

    @Test
    void loadingAnExistingConfigPreservesCustomRangeAndIncrement() {
        CommentedConfig common = CommentedConfig.inMemory();
        common.set("items", List.of());
        common.set(Config.DEFAULT_BULLET_RANGE.getPath(), 24.0);
        common.set(Config.RANGE_BONUS.getPath(), 5.0);
        Config.SPEC.setConfig(common);
        assertEquals(24.0, Config.DEFAULT_BULLET_RANGE.get());
        assertEquals(5.0, Config.RANGE_BONUS.get());
    }
}
