package net.luojiuoscar.isaac_disaster.datagen.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.datagen.LootModifierManager;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.loot.LootModifier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ForgeGlobalLootModifiersProvider implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput output;
    private boolean replace = false;

    public ForgeGlobalLootModifiersProvider(PackOutput output) {
        this.output = output;
    }

    public void replacing() {
        this.replace = true;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Map<String, LootModifier> map = LootModifierManager.getAll();
        List<ResourceLocation> entries = new ArrayList<>();

        for (Map.Entry<String, LootModifier> entry : map.entrySet()) {
            entries.add(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, entry.getKey()));
        }

        // Forge 的 JSON 结构
        JsonObject forgeJson = new JsonObject();
        forgeJson.addProperty("replace", this.replace);
        forgeJson.add("entries", GSON.toJsonTree(entries.stream().map(ResourceLocation::toString).collect(Collectors.toList())));

        Path forgePath = this.output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve("forge")
                .resolve("loot_modifiers")
                .resolve("global_loot_modifiers.json");

        CompletableFuture<?> future = DataProvider.saveStable(cache, forgeJson, forgePath);
        return future;
    }

    @Override
    public String getName() {
        return "Forge Global Loot Modifiers";
    }
}
