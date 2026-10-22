package net.luojiuoscar.isaac_disaster.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.ConfigKey;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplaySettings;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;

/** Test-only overrides of the active world's immutable datapack snapshot. */
public final class GameTestDatapackSettings {
    private static final Gson GSON = new Gson();

    private GameTestDatapackSettings() {}

    public static Scope override(GameTestHelper helper) {
        return new Scope(helper.getLevel().getServer());
    }

    public static final class Scope implements AutoCloseable {
        private final MinecraftServer server;
        private final IsaacDatapackTypes.Snapshot original;
        private final JsonObject gameplay;

        private Scope(MinecraftServer server) {
            this.server = server;
            original = IsaacDatapackData.forServer(server);
            gameplay = original.gameplay().toJson();
        }

        public <T> void set(ConfigKey<T> key, T value) {
            var overrides = new JsonObject();
            overrides.add(key.id(), GSON.toJsonTree(value));
            install(overrides);
        }

        private void install(JsonObject overrides) {
            overrides.entrySet().forEach(entry -> gameplay.add(entry.getKey(), entry.getValue().deepCopy()));
            var values = new HashMap<>(original.values());
            values.put(IsaacDatapackTypes.GAMEPLAY, GameplaySettings.parse(gameplay));
            IsaacDatapackData.install(server, new IsaacDatapackTypes.Snapshot(values));
        }

        @Override
        public void close() {
            IsaacDatapackData.install(server, original);
        }
    }
}
