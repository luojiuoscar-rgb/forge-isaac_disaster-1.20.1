package net.luojiuoscar.isaac_disaster.datapack;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.util.thread.EffectiveSide;


/** The active logical server and remote-client snapshots are deliberately separate. */
public final class IsaacDatapackData {
    private static volatile IsaacDatapackTypes.Snapshot serverData;
    private static volatile IsaacDatapackTypes.Snapshot clientData;
    private static MinecraftServer owner;

    private IsaacDatapackData() {}

    private static class Defaults {
        private static final IsaacDatapackTypes.Snapshot DATA = IsaacDatapackTypes.defaults();
    }

    public static IsaacDatapackTypes.Snapshot current() {
        var data = EffectiveSide.get().isServer() ? serverData : clientData;
        return data == null ? Defaults.DATA : data;
    }

    public static IsaacDatapackTypes.Snapshot forServer(MinecraftServer server) {
        if (server != owner || serverData == null) {
            throw new IllegalStateException("Isaac datapack data has not been loaded for this server");
        }
        return serverData;
    }

    static void install(MinecraftServer server, IsaacDatapackTypes.Snapshot data) {
        owner = server;
        serverData = data;
    }

    static void clearServer(MinecraftServer server) {
        if (owner == server) {
            serverData = null;
            owner = null;
        }
    }

    public static void acceptClient(IsaacDatapackTypes.Snapshot snapshot) { clientData = snapshot; }

    public static void clearClient() { clientData = null; }
}
