package net.luojiuoscar.isaac_disaster.worldgen.dynamic;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 一个“可变固定群系”的 BiomeSource。
 * 它允许在运行时切换所使用的 Biome，从而实现“每次进入维度随机一个群系”的效果。
 *
 * ⚠ 注意：
 *  - 该类的 Codec 仅用于运行时使用，不会在世界保存时持久化。
 *  - 如果希望世界重新加载后仍然保持相同的群系选择，需要为 Codec 增加序列化逻辑。
 */
public class MutableFixedBiomeSource extends BiomeSource {

    private volatile Holder<Biome> selectedBiome;

    public MutableFixedBiomeSource(Holder<Biome> initial) {
        super(); // 调用无参构造器（适配 1.20.1 BiomeSource）
        this.selectedBiome = Objects.requireNonNull(initial, "initial biome cannot be null");
    }

    /**
     * 返回当前实例作为 codec 占位符。
     * （不会进行序列化，仅在运行时生成维度时有效）
     */
    @Override
    protected Codec<? extends BiomeSource> codec() {
        @SuppressWarnings("unchecked")
        Codec<? extends BiomeSource> c = (Codec<? extends BiomeSource>) (Codec) Codec.unit(this);
        return c;
    }

    /**
     * 核心方法：指定坐标返回当前选定的 Biome。
     */
    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return this.selectedBiome;
    }

    /**
     * 必须实现的方法：返回当前可能的所有 Biome（这里只有一个）。
     */
    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(selectedBiome);
    }

    /** 动态修改当前选中的群系。 */
    public void setSelectedBiome(Holder<Biome> biome) {
        this.selectedBiome = Objects.requireNonNull(biome, "biome cannot be null");
    }

    /** 获取当前所选群系。 */
    public Holder<Biome> getSelectedBiome() {
        return this.selectedBiome;
    }
}
