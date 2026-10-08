package net.luojiuoscar.isaac_disaster.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Local presentation settings; no Minecraft client classes are needed to define the spec. */
public final class IsaacClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ATTRIBUTE_INDICATOR_ENABLED;
    public static final ForgeConfigSpec.IntValue ATTRIBUTE_INDICATOR_LEFT_MARGIN;
    public static final ForgeConfigSpec.IntValue ATTRIBUTE_INDICATOR_VERTICAL_OFFSET;
    public static final ForgeConfigSpec.ConfigValue<Number> ATTRIBUTE_INDICATOR_SCALE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("attribute_indicator");
        ATTRIBUTE_INDICATOR_ENABLED = builder.comment("Whether to draw the attribute indicator. Attribute synchronization remains enabled.")
                .define("enabled", true);
        ATTRIBUTE_INDICATOR_LEFT_MARGIN = builder.comment("Left margin in Minecraft GUI coordinate units, including the reserved arrow area.")
                .defineInRange("left_margin", 6, 0, 4096);
        ATTRIBUTE_INDICATOR_VERTICAL_OFFSET = builder.comment("Offset from vertical center in Minecraft GUI coordinate units. Positive values move downward.")
                .defineInRange("vertical_offset", 0, -4096, 4096);
        // Forge's range correction can preserve NaN. A predicate without Range metadata
        // corrects invalid file values to the default, including on file reload.
        ATTRIBUTE_INDICATOR_SCALE = builder.comment("Unitless scale multiplier for icons, text, arrows and spacing. Finite range: 0.5 to 3.0.")
                .define("scale", (Number) 1.0, value -> value instanceof Number number
                        && Double.isFinite(number.doubleValue())
                        && number.doubleValue() >= 0.5 && number.doubleValue() <= 3.0);
        builder.pop();
        SPEC = builder.build();
    }

    private IsaacClientConfig() {
    }
}
