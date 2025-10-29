package net.luojiuoscar.isaac_disaster.worldgen.dynamic;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.MinecraftForge;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import static com.mojang.text2speech.Narrator.LOGGER;

public class DynamicWorldUtils {

    /**
     * 创建新的 ServerLevel（运行时维度）
     */
    public static ServerLevel createLevel(MinecraftServer server,
                                          ResourceKey<Level> dimensionKey,
                                          Holder<DimensionType> dimensionType,
                                          ChunkGenerator generator) {

        ServerLevel existing = server.getLevel(dimensionKey);
        if (existing != null) {
            LOGGER.warn("维度已存在: {}", dimensionKey.location());
            return existing;
        }

        WorldData worldData = server.getWorldData();
        ServerLevel overworld = server.overworld();
        LevelStorageSource.LevelStorageAccess storageAccess = server.storageSource;
        ServerLevelData levelData = new DerivedLevelData(worldData, worldData.overworldData());

        Executor executor = server.executor;
        ChunkProgressListenerFactory chunkProgressListenerFactory = server.progressListenerFactory;
        ChunkProgressListener progressListener = chunkProgressListenerFactory.create(11);

        try {
            LevelStem stem = new LevelStem(dimensionType, generator);

            ServerLevel newLevel = new ServerLevel(
                    server,
                    executor,
                    storageAccess,
                    levelData,
                    dimensionKey,
                    stem,
                    progressListener,
                    false,
                    overworld.getSeed(),
                    List.<CustomSpawner>of(),
                    false,
                    (RandomSequences) null
            );

            server.levels.put(dimensionKey, newLevel);
            MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.LevelEvent.Load(newLevel));

            LOGGER.info("成功创建动态维度: {}", dimensionKey.location());
            return newLevel;

        } catch (Exception e) {
            LOGGER.error("创建动态维度失败: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 卸载指定维度（清空区块并标记未生成）
     */
    public static void unloadLevel(MinecraftServer server, ServerLevel level) throws IOException {
        ResourceKey<Level> key = level.dimension();

        try {
            level.save(null, true, false);
        } catch (Exception e) {
            LOGGER.error("保存维度时出错: {}", e.getMessage());
        }

        // 标记所有区块为未生成
        try {
            ChunkMap chunkMap = level.getChunkSource().chunkMap;
            chunkMap.getChunks().forEach(chunkHolder -> {
                if (chunkHolder != null) {
                    LevelChunk fullChunk = chunkHolder.getFullChunk();
                    if (fullChunk != null) {
                        // 使用 ImposterProtoChunk 包装原区块，禁止写入
                        ImposterProtoChunk emptyProto = new ImposterProtoChunk(fullChunk, false);
                        chunkHolder.replaceProtoChunk(emptyProto);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("标记区块为未生成失败: {}", e.getMessage());
        }

        MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.LevelEvent.Unload(level));

        level.close();
        server.levels.remove(key);

        LOGGER.info("🗑 已卸载动态维度并标记区块未生成: {}", key.location());
    }

}
