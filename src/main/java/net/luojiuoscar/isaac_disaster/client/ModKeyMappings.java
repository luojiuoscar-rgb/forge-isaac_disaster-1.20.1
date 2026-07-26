package net.luojiuoscar.isaac_disaster.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {
    public static final String KEY_CATEGORY_ISAAC_DISASTER = "key.category.isaac_disaster";
    public static final String KEY_OPEN_ISAAC_ITEM_SCREEN = "key.isaac_disaster.open_isaac_item_screen";
    public static final String KEY_TOGGLE_ISAAC_FLIGHT = "key.isaac_disaster.toggle_isaac_flight";

    public static final KeyMapping OPEN_ISAAC_ITEM_SCREEN = new KeyMapping(
            KEY_OPEN_ISAAC_ITEM_SCREEN,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            KEY_CATEGORY_ISAAC_DISASTER
    );

    public static final KeyMapping TOGGLE_ISAAC_FLIGHT = new KeyMapping(
            KEY_TOGGLE_ISAAC_FLIGHT,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY_ISAAC_DISASTER
    );
}
