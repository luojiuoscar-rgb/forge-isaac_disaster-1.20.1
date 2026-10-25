package net.luojiuoscar.isaac_disaster.gametest;

import com.google.gson.JsonObject;
import net.luojiuoscar.isaac_disaster.datapack.IsaacDatapackData;
import net.luojiuoscar.isaac_disaster.datapack.IsaacDatapackTypes;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplaySettings;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.function.Consumer;

public final class GameplayTestSettings {
    private GameplayTestSettings() { }

    public static void with(GameTestHelper helper, Consumer<JsonObject> changes, Runnable action) {
        var server = helper.getLevel().getServer();
        var original = IsaacDatapackData.forServer(server);
        JsonObject json = original.gameplay().toJson();
        changes.accept(json);
        var values = new HashMap<>(original.values());
        values.put(IsaacDatapackTypes.GAMEPLAY, GameplaySettings.parse(json));
        try {
            var install = IsaacDatapackData.class.getDeclaredMethod("install", MinecraftServer.class,
                    IsaacDatapackTypes.Snapshot.class);
            install.setAccessible(true);
            install.invoke(null, server, new IsaacDatapackTypes.Snapshot(values));
            try { action.run(); }
            finally { install.invoke(null, server, original); }
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }
}
