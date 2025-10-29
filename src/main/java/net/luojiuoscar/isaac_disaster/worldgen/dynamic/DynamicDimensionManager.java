package net.luojiuoscar.isaac_disaster.worldgen.dynamic;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.worldgen.dimension.ModDimensions;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.List;

import static com.mojang.text2speech.Narrator.LOGGER;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DynamicDimensionManager {

    public static final ResourceKey<Level> DYNAMIC_DIMENSION =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "dynamic_world"));

    public static final ResourceKey<DimensionType> FISSURE_TYPE = ModDimensions.ISAACDIM_DIM_TYPE;

    private static final List<ResourceKey<Biome>> BIOME_CHOICES = List.of(
            Biomes.PLAINS,
            Biomes.DESERT,
            Biomes.FOREST,
            Biomes.SNOWY_TAIGA,
            Biomes.SAVANNA
    );

    private static ResourceKey<Biome> selectedBiome = null;

    private static boolean isUnloading = false; // 防止卸载事件递归

    public static ServerLevel createDynamicDimension(MinecraftServer server) {
        RegistryAccess access = server.registryAccess();

        Holder<DimensionType> dimensionType = access.registryOrThrow(Registries.DIMENSION_TYPE)
                .getHolderOrThrow(FISSURE_TYPE);

        Holder<NoiseGeneratorSettings> noiseSettings = access.registryOrThrow(Registries.NOISE_SETTINGS)
                .getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD);

        RandomSource random = server.overworld().random;
        selectedBiome = BIOME_CHOICES.get(random.nextInt(BIOME_CHOICES.size()));

        Holder<Biome> biomeHolder = access.registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(selectedBiome);

        ChunkGenerator generator = new DynamicChunkGenerator(
                new MutableFixedBiomeSource(biomeHolder),
                noiseSettings
        );

        ServerLevel existing = server.getLevel(DYNAMIC_DIMENSION);
        if (existing != null) {
            return existing;
        }

        return DynamicWorldUtils.createLevel(server, DYNAMIC_DIMENSION, dimensionType, generator);
    }

    public static void unloadDynamicDimension(MinecraftServer server) throws IOException {
        if (isUnloading) return; // 防止递归
        isUnloading = true;

        LOGGER.info("TRIGGERED UNLOAD");
        ServerLevel level = server.getLevel(DYNAMIC_DIMENSION);
        if (level == null) {
            LOGGER.warn("DIMENSION: {} IS NULL", DYNAMIC_DIMENSION.location());
            isUnloading = false;
            return;
        }
        DynamicWorldUtils.unloadLevel(server, level);

        isUnloading = false;
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) throws IOException {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(DYNAMIC_DIMENSION)) {
            unloadDynamicDimension(level.getServer());
        }
    }
}
