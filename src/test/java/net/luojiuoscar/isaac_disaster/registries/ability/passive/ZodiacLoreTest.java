package net.luojiuoscar.isaac_disaster.registries.ability.passive;

import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class ZodiacLoreTest {
    @Test
    void realTranslationParserExpandsPercentAndPreservesStaticAquariusPercentage() throws Exception {
        try (var input = getClass().getResourceAsStream("/assets/isaac_disaster/lang/en_us.json")) {
            assertNotNull(input);
            var lang = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            var virgo = lang.get("item.isaac_disaster.virgo.lore.1").getAsString();
            assertEquals("受伤时25%概率获得10秒护盾", Component.translatableWithFallback("test.virgo", virgo, 25).getString());
            assertEquals("受伤时100%概率获得10秒护盾", Component.translatableWithFallback("test.virgo.max", virgo, 100).getString());
            var aquarius = lang.get("item.isaac_disaster.aquarius.lore.2").getAsString();
            assertEquals("每0.5秒造成330%面板伤害", Component.translatableWithFallback("test.aquarius", aquarius).getString());
        }
    }
}
