package net.luojiuoscar.isaac_disaster.worldgen.dynamic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class DynamicChunkGenerator extends NoiseBasedChunkGenerator {

    public static final Codec<DynamicChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(DynamicChunkGenerator::getBiomeSource),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(DynamicChunkGenerator::generatorSettings)
            ).apply(instance, instance.stable(DynamicChunkGenerator::new))
    );

    public DynamicChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public static DynamicChunkGenerator createDefault(HolderGetter<Biome> biomeGetter, Holder<NoiseGeneratorSettings> settings) {
        // 默认使用主世界群系源（或之后由随机BiomeSource替代）
        BiomeSource biomeSource = new MutableFixedBiomeSource(biomeGetter.getOrThrow(Biomes.PLAINS));
        return new DynamicChunkGenerator(biomeSource, settings);
    }
}
