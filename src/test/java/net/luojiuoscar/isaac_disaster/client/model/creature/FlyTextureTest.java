package net.luojiuoscar.isaac_disaster.client.model.creature;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FlyTextureTest {
    @Test
    void bodyIsWhiteForTintingAndWingsKeepApprovedColorsAndTransparentHoles() throws Exception {
        BufferedImage texture = texture();
        assertEquals(16, texture.getWidth());
        assertEquals(16, texture.getHeight());
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int pixel = texture.getRGB(x, y);
                int alpha = pixel >>> 24;
                assertTrue(alpha == 0 || alpha == 255, "No translucent membrane pixels");
                if (alpha != 0) colors.add(pixel);
                if (y < 8) assertEquals(0xFFFFFFFF, pixel, "White body accepts any RGB tint without bias");
            }
        }
        assertEquals(Set.of(0xFFFFFFFF, 0xFFDFEFF8), colors);
    }

    @Test
    void wingRootHasAnOpeningAndBothSidesHaveTheSameSilhouette() throws Exception {
        BufferedImage texture = texture();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 6; x++) {
                assertEquals(texture.getRGB(10 + x, 8 + y), texture.getRGB(4 + x, 8 + y),
                        "Up/down UVs must agree at the same point on the wing");
            }
        }
        assertEquals(0, texture.getRGB(10, 9) >>> 24, "Root center is transparent");
        assertEquals(0, texture.getRGB(10, 10) >>> 24, "Root center is transparent");
        assertEquals(255, texture.getRGB(10, 8) >>> 24, "Keep upper connection edge");
        assertEquals(255, texture.getRGB(10, 11) >>> 24, "Keep lower connection edge");
    }

    private static BufferedImage texture() throws Exception {
        try (InputStream source = FlyTextureTest.class.getResourceAsStream(
                "/assets/isaac_disaster/textures/entity/creature/fly.png")) {
            assertNotNull(source, "Shared runtime fly texture must be packaged");
            return ImageIO.read(source);
        }
    }
}
