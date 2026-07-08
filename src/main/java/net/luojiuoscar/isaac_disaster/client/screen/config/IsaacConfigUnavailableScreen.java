package net.luojiuoscar.isaac_disaster.client.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Empty config screen shown when the player opens the Mods page config button
 * while already inside a world.
 */
public class IsaacConfigUnavailableScreen extends Screen {
    private final Screen parent;

    public IsaacConfigUnavailableScreen(Screen parent) {
        super(Component.translatable("config.isaac_disaster.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();

        addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        button -> minecraft.setScreen(parent))
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20)
                .build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("config.isaac_disaster.unavailable.in_world"),
                this.width / 2, this.height / 2 - 10, 0xA0A0A0);
        guiGraphics.drawCenteredString(this.font,
                Component.translatable("config.isaac_disaster.unavailable.exit_world"),
                this.width / 2, this.height / 2 + 6, 0xA0A0A0);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
