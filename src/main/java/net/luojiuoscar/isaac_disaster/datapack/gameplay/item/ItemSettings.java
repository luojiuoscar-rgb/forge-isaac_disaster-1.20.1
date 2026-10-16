package net.luojiuoscar.isaac_disaster.datapack.gameplay.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.datapack.DatapackDataType;
import net.luojiuoscar.isaac_disaster.datapack.IsaacDatapackData;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Each configurable item owns its document and typed settings. */
public final class ItemSettings {
    public static final DatapackDataType<HolyShieldSettings> HOLY_SHIELD = new DatapackDataType<>(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "gameplay/item/holy_shield.json"),
            HolyShieldSettings.CODEC, HolyShieldSettings::defaults);
    public static final DatapackDataType<MoneyIsPowerSettings> MONEY_IS_POWER = new DatapackDataType<>(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "gameplay/item/money_is_power.json"),
            MoneyIsPowerSettings.CODEC, MoneyIsPowerSettings::defaults);
    private static final List<DatapackDataType<?>> TYPES = List.of(HOLY_SHIELD, MONEY_IS_POWER);

    private ItemSettings() {}

    public static List<DatapackDataType<?>> types() { return TYPES; }

    public static HolyShieldSettings holyShield() { return IsaacDatapackData.current().get(HOLY_SHIELD); }

    public static MoneyIsPowerSettings moneyIsPower() { return IsaacDatapackData.current().get(MONEY_IS_POWER); }
}
