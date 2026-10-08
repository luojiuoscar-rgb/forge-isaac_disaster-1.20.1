package net.luojiuoscar.isaac_disaster.client.config;

import java.util.LinkedHashMap;
import java.util.Map;

/** Validates an entire edit before writing, and restores every value if saving fails. */
public final class IsaacConfigSave {
    private IsaacConfigSave() {
    }

    public static void save(IsaacConfigCategory category, Map<IsaacConfigEntry<?>, String> pending,
                            boolean worldLoaded) {
        if (!category.domain().isAccessible(worldLoaded)) {
            throw new IllegalStateException("Gameplay settings cannot be edited while connected to a world");
        }
        for (var edit : pending.entrySet()) {
            if (edit.getKey().category() != category || !edit.getKey().isValidText(edit.getValue())) {
                throw new IllegalArgumentException("Invalid config edit: " + edit.getKey().id());
            }
        }

        Map<IsaacConfigEntry<?>, Object> previous = new LinkedHashMap<>();
        pending.keySet().forEach(entry -> previous.put(entry, entry.value().get()));
        try {
            pending.forEach(IsaacConfigEntry::setFromString);
            category.domain().spec().save();
        } catch (RuntimeException failure) {
            // Forge uses an autosaving file config. A failed set can already have changed
            // the backing data, so restore ALL values and keep going if disk writes still fail.
            previous.forEach((entry, value) -> {
                try {
                    restore(entry, value);
                } catch (RuntimeException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
            });
            category.domain().spec().afterReload();
            throw failure;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void restore(IsaacConfigEntry<T> entry, Object value) {
        entry.value().set((T) value);
    }
}
