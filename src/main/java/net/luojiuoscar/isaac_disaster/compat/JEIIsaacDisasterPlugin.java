package net.luojiuoscar.isaac_disaster.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEIIsaacDisasterPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "jie_plugin");
    }
}
