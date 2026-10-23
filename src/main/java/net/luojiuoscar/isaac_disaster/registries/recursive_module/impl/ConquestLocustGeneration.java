package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ConquestLocustGeneration extends LocustGeneration {
    public ConquestLocustGeneration(ResourceLocation familiar) {
        super(familiar);
    }

    @Override
    protected int getGenerationCount(ServerPlayer player, int amplifier) {
        if (amplifier <= 0) return 0;
        int maximum = getMaximumCount(amplifier);
        return player.getRandom().nextInt(maximum - amplifier + 1) + amplifier;
    }

    /** 返回征服蝗虫数量的闭区间上限，零强度不生成，溢出时饱和。 */
    public static int getMaximumCount(int amplifier) {
        return amplifier <= 0 ? 0 : (int) Math.min(Integer.MAX_VALUE, 2L + 2L * amplifier);
    }
}
