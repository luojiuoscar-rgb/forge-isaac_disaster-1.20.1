package net.luojiuoscar.isaac_disaster.client.screen.config;

import net.luojiuoscar.isaac_disaster.client.config.IsaacConfigCatalog;
import net.luojiuoscar.isaac_disaster.client.config.IsaacConfigCategory;
import net.luojiuoscar.isaac_disaster.client.config.IsaacConfigDomain;
import net.luojiuoscar.isaac_disaster.client.config.IsaacConfigScreenRegistration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/** Category list for one configuration domain, with a caller-owned return path. */
public class IsaacConfigDomainScreen extends Screen {
    private final Screen parent;
    private final IsaacConfigDomain domain;

    public IsaacConfigDomainScreen(Screen parent, IsaacConfigDomain domain) {
        super(domain.title());
        this.parent = parent;
        this.domain = domain;
    }

    @Override
    protected void init() {
        clearWidgets();
        if (!domain.isAccessible(IsaacConfigScreenRegistration.isWorldLoaded(minecraft))) {
            minecraft.setScreen(new IsaacConfigDomainScreen(parent, IsaacConfigDomain.CLIENT));
            return;
        }
        int index = 0;
        for (IsaacConfigCategory category : IsaacConfigCategory.values()) {
            if (category.domain() != domain || IsaacConfigCatalog.entriesFor(category).isEmpty()) continue;
            addRenderableWidget(Button.builder(category.title(),
                            button -> minecraft.setScreen(new IsaacConfigCategoryScreen(this, category)))
                    .bounds(this.width / 2 - 110, 72 + index++ * 26, 220, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (!domain.isAccessible(IsaacConfigScreenRegistration.isWorldLoaded(minecraft))) init();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        if (domain == IsaacConfigDomain.SERVER) {
            var lines = this.font.split(Component.translatable("config.isaac_disaster.server_notice"),
                    Math.max(100, this.width - 32));
            for (int i = 0; i < lines.size(); i++) {
                graphics.drawCenteredString(this.font, lines.get(i), this.width / 2,
                        36 + i * this.font.lineHeight, 0xA0A0A0);
            }
        } else {
            graphics.drawCenteredString(this.font, Component.translatable("config.isaac_disaster.subtitle"),
                    this.width / 2, 36, 0xA0A0A0);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
