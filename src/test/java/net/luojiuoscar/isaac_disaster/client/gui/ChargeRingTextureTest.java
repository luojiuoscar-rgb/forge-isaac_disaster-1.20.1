package net.luojiuoscar.isaac_disaster.client.gui;

import javax.imageio.ImageIO;

import net.luojiuoscar.isaac_disaster.client.gui.charge_bar.ChargeRingGeometry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChargeRingTextureTest {
    @Test
    void grayscalePngMatchesTheCodeRingWithoutCoveringItsHollowCenter() throws Exception {
        try (var input = getClass().getResourceAsStream(
                "/assets/isaac_disaster/textures/hud/charge/charge_ring_base.png")) {
            assertNotNull(input, "Registered circular charge base texture must exist");
            var image = ImageIO.read(input);
            assertEquals(ChargeRingGeometry.SIZE, image.getWidth());
            assertEquals(ChargeRingGeometry.SIZE, image.getHeight());
            assertEquals(0, image.getRGB(5, 5) >>> 24);
            assertEquals(0, image.getRGB(0, 0) >>> 24);
            var pixels = ChargeRingGeometry.rasterize(0f);
            int opaqueCount = 0;
            for (int y = 0; y < 12; y++) {
                for (int x = 0; x < 12; x++) {
                    int color = image.getRGB(x, y);
                    if ((color >>> 24) == 0) continue;
                    opaqueCount++;
                    assertEquals(255, color >>> 24);
                    assertEquals((color >> 16) & 255, (color >> 8) & 255, "Base must be neutral grayscale");
                    assertEquals((color >> 8) & 255, color & 255, "Base must be neutral grayscale");
                }
            }
            assertEquals(pixels.size(), opaqueCount);
            for (var pixel : pixels) {
                assertEquals(255, image.getRGB(pixel.x(), pixel.y()) >>> 24,
                        "Code fill and outline must align with the PNG");
            }
        }
    }
}
