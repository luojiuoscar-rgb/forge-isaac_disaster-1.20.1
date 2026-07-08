package net.luojiuoscar.isaac_disaster.client.config;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Objects;

/**
 * Describes a single editable Forge config value for the custom Isaac config UI.
 *
 * @param id stable entry id used for translation keys
 * @param category parent category shown in the root config screen
 * @param value Forge config value edited by this entry
 * @param type primitive editor type used by the screen
 * @param defaultValue value restored by the Reset button
 * @param requiresRestart whether a change should be treated as needing a game or world restart
 */
public record IsaacConfigEntry<T>(
        String id,
        IsaacConfigCategory category,
        ForgeConfigSpec.ConfigValue<T> value,
        IsaacConfigEntryType type,
        T defaultValue,
        boolean requiresRestart
) {
    public IsaacConfigEntry {
        Objects.requireNonNull(id);
        Objects.requireNonNull(category);
        Objects.requireNonNull(value);
        Objects.requireNonNull(type);
        Objects.requireNonNull(defaultValue);
    }

    /**
     * Returns the translated display label for this config entry.
     */
    public Component title() {
        return Component.translatable("config.isaac_disaster.entry." + id);
    }

    /**
     * Returns the translated description for this config entry.
     */
    public Component description() {
        return Component.translatable("config.isaac_disaster.entry." + id + ".desc");
    }

    /**
     * Returns the current value formatted for an edit box or button label.
     */
    public String currentAsString() {
        return String.valueOf(value.get());
    }

    /**
     * Parses and stores a text value in the backing Forge config entry.
     */
    @SuppressWarnings("unchecked")
    public void setFromString(String text) {
        Object parsed = switch (type) {
            case BOOLEAN -> parseBooleanStrict(text);
            case INTEGER -> Integer.parseInt(text.trim());
            case DOUBLE -> Double.parseDouble(text.trim());
            case STRING -> text;
        };
        value.set((T) parsed);
    }

    /**
     * Returns whether the supplied text can be parsed for this entry type.
     */
    public boolean isValidText(String text) {
        try {
            switch (type) {
                case BOOLEAN -> parseBooleanStrict(text);
                case INTEGER -> Integer.parseInt(text.trim());
                case DOUBLE -> Double.parseDouble(text.trim());
                case STRING -> {
                    return true;
                }
            }
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /**
     * Restores this entry to its default value.
     */
    public void reset() {
        value.set(defaultValue);
    }

    private static boolean parseBooleanStrict(String text) {
        String normalized = text.trim();
        if ("true".equalsIgnoreCase(normalized)) return true;
        if ("false".equalsIgnoreCase(normalized)) return false;
        throw new IllegalArgumentException("Expected true or false");
    }
}
