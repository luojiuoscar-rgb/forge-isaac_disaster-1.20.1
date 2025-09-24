package net.luojiuoscar.isaac_disaster.manager;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

/**
 * 数值管理类：集中管理属性的基准值
 * 支持通过配置文件修改
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class StatManager {
    // 1. 配置文件
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.IntValue BASE_HEALTH_BONUS;


    static {
        // 配置数值的默认值和范围
        BUILDER.push("Passive Item Stats"); // 配置分组

        // 生命值增量基准  默认10
        BASE_HEALTH_BONUS = BUILDER
                .comment("Base value of health increment")
                .defineInRange("base_health_bonus", 10, 1, 10000);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    // 获取基准值
    public static int getBaseHealthBonus() {
        return BASE_HEALTH_BONUS.get(); // 从配置中获取
    }
}