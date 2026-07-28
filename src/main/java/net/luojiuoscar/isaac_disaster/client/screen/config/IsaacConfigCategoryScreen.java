package net.luojiuoscar.isaac_disaster.client.screen.config;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.client.config.IsaacConfigCatalog;
import net.luojiuoscar.isaac_disaster.client.config.IsaacConfigCategory;
import net.luojiuoscar.isaac_disaster.client.config.IsaacConfigEntry;
import net.luojiuoscar.isaac_disaster.client.config.IsaacConfigEntryType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Paged editor screen for one Isaac Disaster config category.
 */
public class IsaacConfigCategoryScreen extends Screen {
    private static final int ENTRIES_PER_PAGE = 7;

    private final Screen parent;
    private final IsaacConfigCategory category;
    private final List<IsaacConfigEntry<?>> entries;
    private final List<RowState> rowStates = new ArrayList<>();
    private final Map<IsaacConfigEntry<?>, String> pendingValues = new LinkedHashMap<>();

    private EditBox searchBox;
    private String searchQuery = "";
    private int searchCursorPosition;
    private boolean searchRefreshPending;
    private int page;
    private Component status = Component.empty();

    public IsaacConfigCategoryScreen(Screen parent, IsaacConfigCategory category) {
        super(category.title());
        this.parent = parent;
        this.category = category;
        this.entries = IsaacConfigCatalog.entriesFor(category);
        for (IsaacConfigEntry<?> entry : entries) {
            pendingValues.put(entry, entry.currentAsString());
        }
    }

    @Override
    protected void init() {
        clearWidgets();
        rowStates.clear();

        List<IsaacConfigEntry<?>> visibleEntries = visibleEntries();
        int maxPage = maxPage();
        if (page > maxPage) page = maxPage;

        int left = Math.max(24, this.width / 2 - 180);
        int valueX = this.width / 2 + 30;
        int rowY = 78;
        int rowHeight = 28;

        searchBox = new EditBox(this.font, this.width / 2 - 100, 34, 200, 20,
                Component.translatable("config.isaac_disaster.search"));
        searchBox.setHint(Component.translatable("config.isaac_disaster.search"));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(this::onSearchChanged);
        addRenderableWidget(searchBox);

        int start = page * ENTRIES_PER_PAGE;
        int end = Math.min(visibleEntries.size(), start + ENTRIES_PER_PAGE);
        for (int i = start; i < end; i++) {
            IsaacConfigEntry<?> entry = visibleEntries.get(i);
            int y = rowY + (i - start) * rowHeight;
            addEntryRow(entry, valueX, y);
        }

        addRenderableWidget(Button.builder(Component.translatable("config.isaac_disaster.previous_page"),
                        button -> {
                            page = Math.max(0, page - 1);
                            refreshWidgets();
                        })
                .bounds(this.width / 2 - 154, this.height - 52, 98, 20)
                .build()).active = page > 0;

        addRenderableWidget(Button.builder(Component.translatable("config.isaac_disaster.next_page"),
                        button -> {
                            page = Math.min(maxPage(), page + 1);
                            refreshWidgets();
                        })
                .bounds(this.width / 2 + 56, this.height - 52, 98, 20)
                .build()).active = page < maxPage();

        addRenderableWidget(Button.builder(Component.translatable("config.isaac_disaster.save"),
                        button -> save())
                .bounds(this.width / 2 - 154, this.height - 28, 98, 20)
                .build());

        addRenderableWidget(Button.builder(Component.translatable("config.isaac_disaster.reset_page"),
                        button -> resetVisibleEntries())
                .bounds(this.width / 2 - 49, this.height - 28, 98, 20)
                .build()).active = hasNonDefaultVisibleEntry();

        addRenderableWidget(Button.builder(Component.translatable("gui.back"),
                        button -> minecraft.setScreen(parent))
                .bounds(this.width / 2 + 56, this.height - 28, 98, 20)
                .build());
    }

    /**
     * Adds one editable row for the supplied config entry.
     */
    private void addEntryRow(IsaacConfigEntry<?> entry, int valueX, int y) {
        if (entry.type() == IsaacConfigEntryType.BOOLEAN) {
            addBooleanRow(entry, valueX, y);
            return;
        }

        EditBox box = new EditBox(this.font, valueX, y, 132, 20, entry.title());
        box.setValue(pendingValue(entry));
        box.setTooltip(Tooltip.create(entry.description()));
        addRenderableWidget(box);
        rowStates.add(new RowState(entry, box));

        Button resetButton = Button.builder(Component.translatable("config.isaac_disaster.reset"),
                        button -> {
                            pendingValues.put(entry, String.valueOf(entry.defaultValue()));
                            box.setValue(pendingValue(entry));
                            button.active = false;
                        })
                .bounds(valueX + 138, y, 54, 20)
                .build();
        box.setResponder(value -> {
            pendingValues.put(entry, value);
            resetButton.active = !entry.isDefaultText(value);
        });
        resetButton.active = !entry.isDefaultText(pendingValue(entry));
        addRenderableWidget(resetButton);
    }

    /**
     * Adds a boolean row represented by a two-state button.
     */
    private void addBooleanRow(IsaacConfigEntry<?> entry, int valueX, int y) {
        boolean initialValue = Boolean.parseBoolean(pendingValue(entry));
        Button resetButton = Button.builder(Component.translatable("config.isaac_disaster.reset"),
                        button -> {
                            pendingValues.put(entry, String.valueOf(entry.defaultValue()));
                            refreshWidgets();
                        })
                .bounds(valueX + 138, y, 54, 20)
                .build();
        resetButton.active = !entry.isDefaultText(pendingValue(entry));

        Button button = Button.builder(booleanLabel(initialValue),
                        toggleButton -> {
                            boolean newValue = !Boolean.parseBoolean(pendingValue(entry));
                            pendingValues.put(entry, String.valueOf(newValue));
                            toggleButton.setMessage(booleanLabel(newValue));
                            resetButton.active = !entry.isDefaultText(pendingValue(entry));
                        })
                .bounds(valueX, y, 132, 20)
                .build();
        button.setTooltip(Tooltip.create(entry.description()));
        addRenderableWidget(button);
        rowStates.add(new RowState(entry, button));

        addRenderableWidget(resetButton);
    }

    /**
     * Restores the currently visible page to default values without saving immediately.
     */
    private void resetVisibleEntries() {
        for (RowState state : rowStates) {
            pendingValues.put(state.entry(), String.valueOf(state.entry().defaultValue()));
        }
        status = Component.translatable("config.isaac_disaster.status.reset_page");
        refreshWidgets();
    }

    private boolean hasNonDefaultVisibleEntry() {
        return rowStates.stream()
                .anyMatch(state -> !state.entry().isDefaultText(pendingValue(state.entry())));
    }

    /**
     * Validates all pending values, writes them to the Forge config spec, and saves the file.
     */
    private void save() {
        for (IsaacConfigEntry<?> entry : entries) {
            if (!entry.isValidText(pendingValue(entry))) {
                clearSearch();
                page = entries.indexOf(entry) / ENTRIES_PER_PAGE;
                status = Component.translatable("config.isaac_disaster.status.invalid", entry.title());
                refreshWidgets();
                return;
            }
        }

        Map<IsaacConfigEntry<?>, String> previousValues = new LinkedHashMap<>();
        for (IsaacConfigEntry<?> entry : entries) {
            previousValues.put(entry, entry.currentAsString());
        }

        for (IsaacConfigEntry<?> entry : entries) {
            try {
                entry.setFromString(pendingValue(entry));
            } catch (RuntimeException exception) {
                restorePreviousValues(previousValues);
                clearSearch();
                page = entries.indexOf(entry) / ENTRIES_PER_PAGE;
                status = Component.translatable("config.isaac_disaster.status.invalid_value", entry.title());
                refreshWidgets();
                return;
            }
        }

        Config.save();
        status = Component.translatable("config.isaac_disaster.status.saved");
    }

    private int maxPage() {
        return Math.max(0, (visibleEntries().size() - 1) / ENTRIES_PER_PAGE);
    }

    private void refreshWidgets() {
        init();
    }

    private void onSearchChanged(String value) {
        if (searchQuery.equals(value)) return;

        searchQuery = value;
        searchCursorPosition = searchBox.getCursorPosition();
        page = 0;
        searchRefreshPending = true;
    }

    private void clearSearch() {
        searchQuery = "";
        searchCursorPosition = 0;
        searchRefreshPending = false;
    }

    private List<IsaacConfigEntry<?>> visibleEntries() {
        String query = searchQuery.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return entries;

        return entries.stream()
                .filter(entry -> matchesSearch(entry, query))
                .toList();
    }

    private boolean matchesSearch(IsaacConfigEntry<?> entry, String query) {
        return entry.id().toLowerCase(Locale.ROOT).contains(query)
                || entry.title().getString().toLowerCase(Locale.ROOT).contains(query)
                || entry.description().getString().toLowerCase(Locale.ROOT).contains(query);
    }

    private String pendingValue(IsaacConfigEntry<?> entry) {
        return pendingValues.getOrDefault(entry, entry.currentAsString());
    }

    private Component booleanLabel(boolean value) {
        return Component.translatable("config.isaac_disaster.boolean." + value);
    }

    /**
     * Restores in-memory config values after a failed save while keeping the user's pending input visible.
     */
    private void restorePreviousValues(Map<IsaacConfigEntry<?>, String> previousValues) {
        for (Map.Entry<IsaacConfigEntry<?>, String> previousValue : previousValues.entrySet()) {
            previousValue.getKey().setFromString(previousValue.getValue());
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 18, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("config.isaac_disaster.page", page + 1, maxPage() + 1),
                this.width / 2, 60, 0xA0A0A0);

        List<IsaacConfigEntry<?>> visibleEntries = visibleEntries();
        int start = page * ENTRIES_PER_PAGE;
        int end = Math.min(visibleEntries.size(), start + ENTRIES_PER_PAGE);
        int left = Math.max(24, this.width / 2 - 180);
        int rowY = 84;
        for (int i = start; i < end; i++) {
            IsaacConfigEntry<?> entry = visibleEntries.get(i);
            int y = rowY + (i - start) * 28;
            guiGraphics.drawString(this.font, entry.title(), left, y, 0xFFFFFF, false);
        }

        if (visibleEntries.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("config.isaac_disaster.search.no_results"),
                    this.width / 2, 92, 0xA0A0A0);
        }

        guiGraphics.drawCenteredString(this.font, status, this.width / 2, this.height - 70, 0xE0E0E0);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void tick() {
        super.tick();
        if (!searchRefreshPending) return;

        searchRefreshPending = false;
        refreshWidgets();
        searchBox.setFocused(true);
        searchBox.setCursorPosition(searchCursorPosition);
        setFocused(searchBox);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private record RowState(IsaacConfigEntry<?> entry, GuiEventListener editor) {
    }
}
