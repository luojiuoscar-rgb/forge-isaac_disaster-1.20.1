package net.luojiuoscar.isaac_disaster.datapack.gameplay.item;

import com.mojang.serialization.Codec;

public record MoneyIsPowerSettings(double damagePerCoin) {
    private static final MoneyIsPowerSettings DEFAULTS = new MoneyIsPowerSettings(0.007);
    public static final Codec<MoneyIsPowerSettings> CODEC = ItemSettingCodecs.singleDouble(
            "damage_per_coin", MoneyIsPowerSettings::defaults, MoneyIsPowerSettings::new, MoneyIsPowerSettings::damagePerCoin);

    public MoneyIsPowerSettings { ItemSettingCodecs.validate("damage_per_coin", damagePerCoin); }

    public static MoneyIsPowerSettings defaults() { return DEFAULTS; }
}
