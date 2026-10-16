package net.luojiuoscar.isaac_disaster.gametest;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.GameplayConfig;
import net.luojiuoscar.isaac_disaster.datapack.IsaacDatapackData;
import net.luojiuoscar.isaac_disaster.datapack.gameplay.item.ItemSettings;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.CompletionException;

/** Isolated namespace: run with forge.enabledGameTestNamespaces=isaac_disaster_datapack. */
@GameTestHolder("isaac_disaster_datapack")
@PrefixGameTestTemplate(false)
public final class WorldDatapackGameTests {
    @GameTest(template = "trajectory_empty", batch = "datapack_settings", timeoutTicks = 200)
    public static void worldInitializationAndReloadAreAuthoritative(GameTestHelper helper) throws Exception {
        var server = helper.getLevel().getServer();
        var root = server.getWorldPath(LevelResource.ROOT);
        var file = root.resolve("datapacks/isaac_disaster/data/isaac_disaster/gameplay/settings.json");
        helper.assertTrue(Files.isRegularFile(file), "Bootstrap mixin must create the world pack before discovery");
        helper.assertTrue(Files.isRegularFile(root.resolve("isaac_disaster_datapack.initialized")), "Missing initialization marker");
        helper.assertTrue(server.getPackRepository().getSelectedIds().contains("file/isaac_disaster"), "Generated pack must be enabled");
        var originalText = Files.readString(file);
        var shield = file.getParent().resolve("item/holy_shield.json");
        helper.assertTrue(Files.isRegularFile(shield), "Configurable item defaults must be generated");
        var originalShield = Files.readString(shield);
        double template = Config.HEALTH_BONUS.get();
        double initial = GameplayConfig.HEALTH_BONUS.get();
        var packs = new ArrayList<>(server.getPackRepository().getSelectedIds());
        var override = Files.createTempDirectory(root.resolve("datapacks"), "isaac_override_");
        var overrideFile = override.resolve("data/isaac_disaster/gameplay/settings.json");
        var overrideShield = overrideFile.getParent().resolve("item/holy_shield.json");
        try {
            Config.HEALTH_BONUS.set(template == 99999.0 ? 1.0 : template + 1.0);
            helper.assertTrue(GameplayConfig.HEALTH_BONUS.get() == initial, "Live TOML must not affect the loaded world");
            Files.writeString(file, "{\"health_bonus\":37.0}");
            Files.writeString(shield, "{\"strength_per_stack\":9.5}");
            server.reloadResources(packs).join();
            helper.assertTrue(GameplayConfig.HEALTH_BONUS.get() == 37.0, "Successful reload must publish the new value");
            helper.assertTrue(IsaacDatapackData.forServer(server).get(ItemSettings.HOLY_SHIELD).strengthPerStack() == 9.5,
                    "Item settings must be part of the committed server snapshot");

            Files.createDirectories(overrideFile.getParent());
            Files.writeString(override.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":15,\"description\":\"Test map\"}}");
            Files.writeString(overrideFile, "{\"health_bonus\":53.0}");
            Files.createDirectories(overrideShield.getParent());
            Files.writeString(overrideShield, "{\"strength_per_stack\":11.0}");
            var initialPacks = new ArrayList<>(packs);
            initialPacks.remove("file/isaac_disaster");
            initialPacks.add("file/" + override.getFileName());
            MinecraftServer.configurePackRepository(server.getPackRepository(),
                    new DataPackConfig(initialPacks, java.util.List.of()), false, server.getWorldData().enabledFeatures());
            var selected = new ArrayList<>(server.getPackRepository().getSelectedIds());
            helper.assertTrue(selected.indexOf("file/isaac_disaster") < selected.indexOf("file/" + override.getFileName()),
                    "First-discovered generated pack must stay below map packs");
            server.reloadResources(selected).join();
            helper.assertTrue(GameplayConfig.HEALTH_BONUS.get() == 53.0, "Map pack must override the generated template");
            helper.assertTrue(ItemSettings.holyShield().strengthPerStack() == 11.0, "Map pack must override item data");
            server.reloadResources(packs).join();

            var oversized = new com.google.gson.JsonObject();
            oversized.addProperty("health_bonus", 57.0);
            oversized.addProperty("magicNumberIntroduction", "a".repeat(131073));
            Files.writeString(file, oversized.toString());
            boolean sizeRejected = false;
            try { server.reloadResources(packs).join(); }
            catch (CompletionException expected) { sizeRejected = true; }
            helper.assertTrue(sizeRejected, "Oversized snapshots must fail before publication, even without online players");
            helper.assertTrue(GameplayConfig.HEALTH_BONUS.get() == 37.0, "Oversized reload must preserve previous gameplay settings");
            helper.assertTrue(ItemSettings.holyShield().strengthPerStack() == 9.5, "Oversized reload must preserve previous item data");
            Files.writeString(file, "{\"health_bonus\":37.0}");

            Files.writeString(shield, "{\"strength_per_stack\":false}");
            var feedback = new ArrayList<String>();
            var commandSource = server.createCommandSourceStack().withSource(new CommandSource() {
                @Override public void sendSystemMessage(Component message) { feedback.add(message.getString()); }
                @Override public boolean acceptsSuccess() { return false; }
                @Override public boolean acceptsFailure() { return true; }
                @Override public boolean shouldInformAdmins() { return false; }
            });
            ReloadCommand.reloadPacks(packs, commandSource);
            helper.assertTrue(feedback.contains(Component.translatable("commands.reload.failure").getString()),
                    "Reload failure must use the vanilla command feedback");
            boolean rejected = false;
            try { server.reloadResources(packs).join(); }
            catch (CompletionException expected) { rejected = true; }
            helper.assertTrue(rejected, "Wrong JSON types must fail reload");
            helper.assertTrue(GameplayConfig.HEALTH_BONUS.get() == 37.0, "Failed reload must retain the previous snapshot");
            helper.assertTrue(ItemSettings.holyShield().strengthPerStack() == 9.5, "Failed reload must retain previous item data");
        } finally {
            Config.HEALTH_BONUS.set(template);
            Files.writeString(file, originalText);
            Files.writeString(shield, originalShield);
            server.reloadResources(packs).join();
            Files.deleteIfExists(overrideFile);
            Files.deleteIfExists(overrideShield);
            Files.deleteIfExists(overrideShield.getParent());
            Files.deleteIfExists(override.resolve("pack.mcmeta"));
            Files.deleteIfExists(override.resolve("data/isaac_disaster/gameplay"));
            Files.deleteIfExists(override.resolve("data/isaac_disaster"));
            Files.deleteIfExists(override.resolve("data"));
            Files.deleteIfExists(override);
        }
        helper.succeed();
    }
}
