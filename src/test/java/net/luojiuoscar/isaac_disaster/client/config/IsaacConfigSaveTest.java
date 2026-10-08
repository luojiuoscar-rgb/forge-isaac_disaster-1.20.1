package net.luojiuoscar.isaac_disaster.client.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.config.IsaacClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IsaacConfigSaveTest {
    @TempDir Path directory;
    private CommentedFileConfig file;
    private CommentedFileConfig commonFile;

    @BeforeEach
    void loadClientConfig() {
        IsaacClientConfig.SPEC.setConfig(CommentedConfig.inMemory());
        CommentedConfig commonData = CommentedConfig.inMemory();
        commonData.set("items", java.util.List.of());
        Config.spec().setConfig(commonData);
    }

    @AfterEach
    void unload() {
        IsaacClientConfig.SPEC.setConfig(null);
        Config.spec().setConfig(null);
        if (file != null) file.close();
        if (commonFile != null) commonFile.close();
    }

    @Test
    void invalidEditRejectsEntireBatch() {
        var pending = draft();
        pending.put(entry("attribute_indicator_enabled"), "false");
        pending.put(entry("attribute_indicator_scale"), "NaN");
        assertThrows(IllegalArgumentException.class,
                () -> IsaacConfigSave.save(IsaacConfigCategory.ATTRIBUTE_INDICATOR, pending, true));
        assertTrue(IsaacClientConfig.ATTRIBUTE_INDICATOR_ENABLED.get());
        assertEquals(1.0, IsaacClientConfig.ATTRIBUTE_INDICATOR_SCALE.get());
    }

    @Test
    void clientSaveUpdatesRuntimeAndItsFileWhileConnected() throws Exception {
        Path path = directory.resolve("isaac_disaster-client.toml");
        Path common = directory.resolve("isaac_disaster-common.toml");
        commonFile = CommentedFileConfig.builder(common).sync().build();
        commonFile.set("items", java.util.List.of());
        Config.spec().setConfig(commonFile);
        String commonBefore = Files.readString(common);
        double damageBefore = Config.DEFAULT_ATTACK_DAMAGE.get();
        file = CommentedFileConfig.builder(path).sync().autosave().build();
        IsaacClientConfig.SPEC.setConfig(file);
        var pending = draft();
        pending.put(entry("attribute_indicator_scale"), "2.0");
        pending.put(entry("attribute_indicator_enabled"), "false");
        IsaacConfigSave.save(IsaacConfigCategory.ATTRIBUTE_INDICATOR, pending, true);
        assertEquals(2.0, IsaacClientConfig.ATTRIBUTE_INDICATOR_SCALE.get());
        assertFalse(IsaacClientConfig.ATTRIBUTE_INDICATOR_ENABLED.get());
        try (var saved = CommentedFileConfig.of(path)) {
            saved.load();
            assertEquals(2.0, (Double) saved.get("attribute_indicator.scale"));
            assertEquals(false, saved.get("attribute_indicator.enabled"));
        }
        assertEquals(commonBefore, Files.readString(common));
        assertEquals(damageBefore, Config.DEFAULT_ATTACK_DAMAGE.get());
    }

    @Test
    void serverSaveUsesCommonFileAndLeavesClientFileAndValuesAlone() throws Exception {
        Path clientPath = directory.resolve("client.toml");
        file = CommentedFileConfig.builder(clientPath).sync().build();
        IsaacClientConfig.SPEC.setConfig(file);
        String clientBefore = Files.readString(clientPath);
        Path commonPath = directory.resolve("common.toml");
        commonFile = CommentedFileConfig.builder(commonPath).sync().build();
        commonFile.set("items", java.util.List.of());
        Config.spec().setConfig(commonFile);
        IsaacConfigSave.save(IsaacConfigCategory.PLAYER_STATS,
                Map.of(entry("default_attack_damage"), "4.5"), false);
        assertEquals(4.5, Config.DEFAULT_ATTACK_DAMAGE.get());
        try (var saved = CommentedFileConfig.of(commonPath)) {
            saved.load();
            assertEquals(4.5, (Double) saved.get(Config.DEFAULT_ATTACK_DAMAGE.getPath()));
        }
        assertEquals(clientBefore, Files.readString(clientPath));
        assertEquals(1.0, IsaacClientConfig.ATTRIBUTE_INDICATOR_SCALE.get());
    }

    @Test
    void failedAutosaveRestoresAllRuntimeValuesEvenWhenRollbackWritesFail() throws Exception {
        Path path = directory.resolve("client.toml");
        file = CommentedFileConfig.builder(path).sync().autosave().build();
        IsaacClientConfig.SPEC.setConfig(file);
        var pending = draft();
        pending.put(entry("attribute_indicator_enabled"), "false");
        pending.put(entry("attribute_indicator_scale"), "2.0");
        Files.delete(path);
        Files.createDirectory(path);
        assertThrows(RuntimeException.class,
                () -> IsaacConfigSave.save(IsaacConfigCategory.ATTRIBUTE_INDICATOR, pending, true));
        assertTrue(IsaacClientConfig.ATTRIBUTE_INDICATOR_ENABLED.get());
        assertEquals(6, IsaacClientConfig.ATTRIBUTE_INDICATOR_LEFT_MARGIN.get());
        assertEquals(0, IsaacClientConfig.ATTRIBUTE_INDICATOR_VERTICAL_OFFSET.get());
        assertEquals(1.0, IsaacClientConfig.ATTRIBUTE_INDICATOR_SCALE.get());
    }

    @Test
    void serverSaveIsBlockedWhileWorldIsLoadedAndCannotAcceptClientEntries() {
        assertThrows(IllegalStateException.class,
                () -> IsaacConfigSave.save(IsaacConfigCategory.PLAYER_STATS, Map.of(), true));
        assertThrows(IllegalArgumentException.class,
                () -> IsaacConfigSave.save(IsaacConfigCategory.PLAYER_STATS, draft(), false));
        assertTrue(IsaacClientConfig.ATTRIBUTE_INDICATOR_ENABLED.get());
    }

    @Test
    void nanInTomlIsCorrectedToFiniteDefaultOnLoadAndReload() throws Exception {
        Path path = directory.resolve("nonfinite.toml");
        file = CommentedFileConfig.builder(path).sync().build();
        for (String scale : new String[]{"nan", "+inf", "-inf"}) {
            Files.writeString(path, "[attribute_indicator]\nenabled = true\nleft_margin = 6\nvertical_offset = 0\nscale = " + scale + "\n");
            file.load();
            IsaacClientConfig.SPEC.setConfig(file);
            assertEquals(1.0, IsaacClientConfig.ATTRIBUTE_INDICATOR_SCALE.get(), scale);
        }
    }

    @Test
    void integerScaleInTomlCanBeReadAsAValidMultiplier() throws Exception {
        Path path = directory.resolve("integer-scale.toml");
        Files.writeString(path, "[attribute_indicator]\nenabled = true\nleft_margin = 6\nvertical_offset = 0\nscale = 2\n");
        file = CommentedFileConfig.builder(path).sync().build();
        file.load();
        IsaacClientConfig.SPEC.setConfig(file);
        assertEquals(2.0, IsaacClientConfig.ATTRIBUTE_INDICATOR_SCALE.get().doubleValue());
    }

    @Test
    void fileReloadChangesValuesReadByHud() throws Exception {
        Path path = directory.resolve("reload.toml");
        file = CommentedFileConfig.builder(path).sync().build();
        IsaacClientConfig.SPEC.setConfig(file);
        assertEquals(1.0, IsaacClientConfig.ATTRIBUTE_INDICATOR_SCALE.get());
        Files.writeString(path, "[attribute_indicator]\nenabled = false\nleft_margin = 12\nvertical_offset = -8\nscale = 1.75\n");
        file.load();
        IsaacClientConfig.SPEC.afterReload();
        assertEquals(1.75, IsaacClientConfig.ATTRIBUTE_INDICATOR_SCALE.get());
        assertFalse(IsaacClientConfig.ATTRIBUTE_INDICATOR_ENABLED.get());
        assertEquals(12, IsaacClientConfig.ATTRIBUTE_INDICATOR_LEFT_MARGIN.get());
        assertEquals(-8, IsaacClientConfig.ATTRIBUTE_INDICATOR_VERTICAL_OFFSET.get());
    }

    private Map<IsaacConfigEntry<?>, String> draft() {
        Map<IsaacConfigEntry<?>, String> pending = new LinkedHashMap<>();
        IsaacConfigCatalog.entriesFor(IsaacConfigCategory.ATTRIBUTE_INDICATOR)
                .forEach(entry -> pending.put(entry, entry.currentAsString()));
        return pending;
    }

    private IsaacConfigEntry<?> entry(String id) {
        return IsaacConfigCatalog.entries().stream().filter(entry -> entry.id().equals(id)).findFirst().orElseThrow();
    }
}
