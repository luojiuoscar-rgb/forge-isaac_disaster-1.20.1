package net.luojiuoscar.isaac_disaster.client.config;

import net.luojiuoscar.isaac_disaster.client.screen.config.IsaacConfigRootScreen;
import net.luojiuoscar.isaac_disaster.client.screen.config.IsaacConfigUnavailableScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Registers the Isaac Disaster config screen factory for Forge's Mods page.
 */
public final class IsaacConfigScreenRegistration {
    private IsaacConfigScreenRegistration() {
    }

    /**
     * Adds the custom config screen to the Forge Mods page config button.
     */
    public static void register(ModLoadingContext context) {
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(IsaacConfigScreenRegistration::createConfigScreen)
        );
    }

    /**
     * Opens the editable config screen only before a world is loaded.
     */
    private static Screen createConfigScreen(Minecraft minecraft, Screen parent) {
        if (minecraft.level != null || minecraft.player != null) {
            return new IsaacConfigUnavailableScreen(parent);
        }
        return new IsaacConfigRootScreen(parent);
    }
}
