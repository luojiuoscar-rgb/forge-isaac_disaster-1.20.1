package net.luojiuoscar.isaac_disaster.datapack.sync;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.luojiuoscar.isaac_disaster.datapack.DatapackDataType;
import net.luojiuoscar.isaac_disaster.datapack.IsaacDatapackData;
import net.luojiuoscar.isaac_disaster.datapack.IsaacDatapackTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import java.util.LinkedHashMap;
import java.util.Map;

/** No client classes in this codec: it also loads on dedicated servers. */
public record GameplaySettingsS2CPacket(IsaacDatapackTypes.Snapshot snapshot) {
    private static final int MAX_JSON_LENGTH = 131072;

    public GameplaySettingsS2CPacket(FriendlyByteBuf buffer) {
        this(decode(JsonParser.parseString(buffer.readUtf(MAX_JSON_LENGTH))));
    }

    private static IsaacDatapackTypes.Snapshot decode(JsonElement json) {
        if (!json.isJsonObject()) throw new IllegalArgumentException("Gameplay snapshot must be an object");
        Map<String, DatapackDataType<?>> types = new LinkedHashMap<>();
        IsaacDatapackTypes.types().forEach(type -> types.put(type.file().toString(), type));
        Map<DatapackDataType<?>, Object> values = new LinkedHashMap<>();
        json.getAsJsonObject().entrySet().forEach(entry -> {
            var type = types.get(entry.getKey());
            if (type == null) throw new IllegalArgumentException("Unknown gameplay document: " + entry.getKey());
            values.put(type, type.codec().parse(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false,
                    error -> { throw new IllegalArgumentException(type.file() + ": " + error); }));
        });
        if (values.size() != types.size()) throw new IllegalArgumentException("Incomplete gameplay snapshot");
        return new IsaacDatapackTypes.Snapshot(values);
    }

    private static <T> JsonElement encode(DatapackDataType<T> type, IsaacDatapackTypes.Snapshot snapshot) {
        return type.codec().encodeStart(JsonOps.INSTANCE, snapshot.get(type)).getOrThrow(false,
                error -> { throw new IllegalArgumentException(type.file() + ": " + error); });
    }

    public void toBytes(FriendlyByteBuf buffer) {
        var json = new JsonObject();
        IsaacDatapackTypes.types().forEach(type -> json.add(type.file().toString(), encode(type, snapshot)));
        buffer.writeUtf(json.toString(), MAX_JSON_LENGTH);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> IsaacDatapackData.acceptClient(snapshot));
        context.setPacketHandled(true);
        return true;
    }
}
