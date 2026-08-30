package net.luojiuoscar.isaac_disaster.registries.bullet_visual;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FetusBulletVisualTest {
    @Test
    void usesRottenFleshWhenNoShatterTextureIsConfigured() {
        FetusBulletVisual visual = new FetusBulletVisual(0.0D, null, true, null);

        assertEquals(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/rotten_flesh.png"),
                visual.getShatterTexture());
    }

    @Test
    void preservesAnExplicitShatterTexture() {
        ResourceLocation boneBlock = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/bone_block_side.png");
        FetusBulletVisual visual = new FetusBulletVisual(1.0D, null, false, boneBlock);

        assertEquals(boneBlock, visual.getShatterTexture());
    }
}
