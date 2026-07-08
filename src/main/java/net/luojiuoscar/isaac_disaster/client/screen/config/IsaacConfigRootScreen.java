package net.luojiuoscar.isaac_disaster.client.screen.config;

import net.luojiuoscar.isaac_disaster.client.config.IsaacConfigCatalog;
import net.luojiuoscar.isaac_disaster.client.config.IsaacConfigCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Root screen shown from Forge's Mods page config button.
 */
public class IsaacConfigRootScreen extends Screen {
    private final Screen parent;

    public IsaacConfigRootScreen(Screen parent) {
        super(Component.translatable("config.isaac_disaster.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();

        int buttonWidth = 220;
        int buttonHeight = 20;
        int startY = 52;
        int centerX = this.width / 2;

        int index = 0;
        for (IsaacConfigCategory category : IsaacConfigCategory.values()) {
            if (IsaacConfigCatalog.entriesFor(category).isEmpty()) continue;

            int y = startY + index * 26;
            addRenderableWidget(Button.builder(category.title(),
                            button -> minecraft.setScreen(new IsaacConfigCategoryScreen(this, category)))
                    .bounds(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight)
                    .build());
            index++;
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        button -> minecraft.setScreen(parent))
                .bounds(centerX - 100, this.height - 28, 200, 20)
                .build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("config.isaac_disaster.subtitle"),
                this.width / 2, 34, 0xA0A0A0);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
