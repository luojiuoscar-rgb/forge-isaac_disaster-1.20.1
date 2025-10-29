package net.luojiuoscar.isaac_disaster.worldgen.dimension;

import com.mojang.serialization.Lifecycle;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.worldgen.dynamic.DynamicWorldUtils;
import net.luojiuoscar.isaac_disaster.worldgen.dynamic.MutableFixedBiomeSource;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Locale;

import static com.mojang.text2speech.Narrator.LOGGER;

/**
 * 工具类：用于动态注册与获取维度。
 */
public class DimensionUtils {

    /**
     * 创建一个空维度的 LevelStem（用于注册到 DynamicDimensionManager）
     */
    public static LevelStem createEmptyLevelStem(RegistryAccess registryAccess, Holder<Biome> biomeHolder) {
        // 获取需要的注册表访问器（有些表目前可能不直接用于构造器，但保留以备需要）
        HolderGetter<StructureSet> structureSets = registryAccess.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS);

        // 固定群系生成器（你的自定义 MutableFixedBiomeSource）
        MutableFixedBiomeSource biomeSource = new MutableFixedBiomeSource(biomeHolder);

        // 从 NOISE_SETTINGS 注册表中获取 overworld 的 NoiseGeneratorSettings holder（显式使用 ResourceLocation）
        Holder<NoiseGeneratorSettings> noiseSetting = noiseSettings.getOrThrow(
                ResourceKey.create(Registries.NOISE_SETTINGS, ResourceLocation.withDefaultNamespace("overworld"))
        );

        // 根据当前映射，NoiseBasedChunkGenerator 的构造器为 (BiomeSource, Holder<NoiseGeneratorSettings>)
        NoiseBasedChunkGenerator chunkGenerator = new NoiseBasedChunkGenerator(biomeSource, noiseSetting);

        // 获取 overworld 的 DimensionType holder（显式使用 ResourceLocation）
        Holder<DimensionType> dimTypeHolder = registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE)
                .getOrThrow(ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.withDefaultNamespace("overworld")));

        // 创建 LevelStem（DimensionType holder, ChunkGenerator）
        return new LevelStem(dimTypeHolder, chunkGenerator);
    }

    /**
     * 获取或创建维度键
     */
    public static ResourceKey<Level> getDimensionKey(String name) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, name));
    }

    /**
     * 获取指定名称的 ServerLevel（如果维度已存在）
     */
    public static ServerLevel getLevel(MinecraftServer server, String name) {
        ResourceKey<Level> key = getDimensionKey(name);
        return server.getLevel(key);
    }

    /**
     * 检查维度是否存在
     */
    public static boolean hasDimension(MinecraftServer server, String name) {
        return getLevel(server, name) != null;
    }

    /**
     * 获取默认的生物群系 Holder（plains）
     */
    public static Holder<Biome> getDefaultBiome(RegistryAccess access) {
        return access.lookupOrThrow(Registries.BIOME)
                .getOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace("plains")));
    }


    public static void createIsaacDerivedDimension(MinecraftServer server, String name) {
        ResourceKey<LevelStem> baseStemKey = ModDimensions.ISAACDIM_KEY;
        Registry<LevelStem> stemRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);

        LevelStem baseStem = stemRegistry.get(baseStemKey);
        if (baseStem == null) {
            LOGGER.error("无法找到基础维度模板: " + baseStemKey.location());
            return;
        }

        // 复制模板配置
        LevelStem newStem = new LevelStem(baseStem.type(), baseStem.generator());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, name.toLowerCase(Locale.ROOT));
        ResourceKey<LevelStem> newStemKey = ResourceKey.create(Registries.LEVEL_STEM, id);
        ResourceKey<Level> newLevelKey = ResourceKey.create(Registries.DIMENSION, id);

        addDimenison(server, newStemKey, newLevelKey, newStem);

        LOGGER.info("成功创建新维度: {}", newLevelKey.location());
    }

    /**
     * 向服务器注册新的维度（LevelStem + ServerLevel）
     */
    public static void addDimenison(MinecraftServer server,
                                    ResourceKey<LevelStem> stemKey,
                                    ResourceKey<Level> levelKey,
                                    LevelStem stem) {
        // 获取当前的维度注册表
        Registry<LevelStem> stemRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);

        // 检查是否已存在
        if (stemRegistry.containsKey(stemKey)) {
            LOGGER.warn("维度模板已存在: {}", stemKey.location());
            return;
        }

        try {
            // ✅ 1. 向注册表注入 LevelStem
            if (stemRegistry instanceof WritableRegistry<LevelStem> writable) {
                writable.register(stemKey, stem, Lifecycle.stable());
                LOGGER.info("已向注册表添加维度模板: {}", stemKey.location());
            } else {
                LOGGER.error("无法写入维度注册表（Registry 不是可写类型）");
                return;
            }

            // ✅ 2. 创建对应的 ServerLevel
            DynamicWorldUtils.createLevel(
                    server,
                    levelKey,
                    stem.type(),       // DimensionType
                    stem.generator()   // ChunkGenerator
            );

            LOGGER.info("✅ 已注册并创建新维度: {}", levelKey.location());

        } catch (Exception e) {
            LOGGER.error("注册维度失败: {}", e.getMessage());
            e.printStackTrace();
        }
    }


}
