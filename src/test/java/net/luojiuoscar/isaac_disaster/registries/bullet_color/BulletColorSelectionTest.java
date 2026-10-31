package net.luojiuoscar.isaac_disaster.registries.bullet_color;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BulletColorSelectionTest {
    private static final ResourceLocation TEAR = ResourceLocation.parse("test:tear");
    private static final ResourceLocation LASER = ResourceLocation.parse("test:laser");
    private static final ResourceLocation BRIMSTONE = ResourceLocation.parse("test:brimstone");

    @Test
    void higherTierWinsBeforePriorityAndEqualRanksUseFullId() {
        var low = ResourceLocation.parse("a:low");
        var first = ResourceLocation.parse("a:same");
        var second = ResourceLocation.parse("b:same");
        var definitions = Map.of(low, BulletColor.builder().color(1).priority(0, 999).build(),
                first, BulletColor.builder().color(2).priority(1, 2).build(),
                second, BulletColor.builder().color(3).priority(1, 2).build());
        for (var order : List.of(List.of(low, first, second), List.of(second, first, low))) {
            var result = new BulletColorSelection(order).resolve(TEAR, TEAR, 0, true, definitions::get);
            assertEquals(first, result.colorId());
            assertEquals(2, result.color());
        }
    }

    @Test
    void alphaUsesAllApplicableCandidatesIndependentlyOfRgb() {
        var tint = ResourceLocation.parse("test:tint");
        var translucent = ResourceLocation.parse("test:translucent");
        var moreTransparent = ResourceLocation.parse("test:more_transparent");
        var definitions = Map.of(tint, BulletColor.builder().color(0x00FF00).priority(3, 100).build(),
                translucent, BulletColor.builder().alpha(0.7F).build(),
                moreTransparent, BulletColor.builder().alpha(0.2F).build());
        var colors = new BulletColorSelection(definitions.keySet());
        colors.offer(translucent);
        var result = colors.resolve(TEAR, TEAR, 0, true, definitions::get);
        assertEquals(tint, result.colorId());
        assertEquals(0x00FF00, result.color());
        assertEquals(0.5F, result.alpha());
        assertEquals(3, colors.ids().size());
        assertEquals(1, colors.resolve(LASER, LASER, 0, false, definitions::get).alpha());
    }

    @Test
    void minimumAlphaIsSelectedRatherThanMultiplied() {
        var a = ResourceLocation.parse("test:a");
        var b = ResourceLocation.parse("test:b");
        var definitions = Map.of(a, BulletColor.builder().alpha(0.8F).build(),
                b, BulletColor.builder().alpha(0.7F).build());
        assertEquals(0.7F, new BulletColorSelection(definitions.keySet())
                .resolve(TEAR, TEAR, 0x123456, true, definitions::get).alpha());
    }

    @Test
    void exactAndRootWhitelistsFormAUnionAndRejectGroundEffects() {
        var id = ResourceLocation.parse("test:restricted");
        var color = BulletColor.builder().color(0x112233).alpha(0.5F)
                .onlyTypes(BRIMSTONE).onlyRoots(TEAR).build();
        var selection = new BulletColorSelection(List.of(id));
        Map<ResourceLocation, BulletColor> definitions = Map.of(id, color);
        assertEquals(id, selection.resolve(BRIMSTONE, LASER, 0, true, definitions::get).colorId());
        assertEquals(id, selection.resolve(ResourceLocation.parse("test:fetus"), TEAR, 0, true, definitions::get).colorId());
        var rejected = selection.resolve(LASER, LASER, 0xFF0000, true, definitions::get);
        assertEquals(0xFF0000, rejected.color());
        assertEquals(1, rejected.alpha());
        assertEquals(0x3C9BE8, selection.resolve(null, null, 0x3C9BE8, false, definitions::get).color());
    }

    @Test
    void filteringBeforeSelectionPreservesLowerPriorityFallback() {
        var restricted = ResourceLocation.parse("test:restricted");
        var ordinary = ResourceLocation.parse("test:ordinary");
        var definitions = Map.of(restricted, BulletColor.builder().color(1).priority(10, 0).onlyTypes(BRIMSTONE).build(),
                ordinary, BulletColor.builder().color(2).priority(0, 1).build());
        var selection = new BulletColorSelection(definitions.keySet());
        assertEquals(2, selection.resolve(TEAR, TEAR, 0, true, definitions::get).color());
        assertEquals(1, selection.resolve(BRIMSTONE, LASER, 0, false, definitions::get).color());
    }

    @Test
    void copyAndCandidateViewsAreIndependent() {
        var first = ResourceLocation.parse("test:first");
        var second = ResourceLocation.parse("test:second");
        var parent = new BulletColorSelection(List.of(first));
        var child = parent.copy();
        child.offer(second);
        assertEquals(1, parent.ids().size());
        assertEquals(2, child.ids().size());
        assertThrows(UnsupportedOperationException.class, () -> parent.ids().clear());
    }

    @Test
    void neutralAndUnknownIdsKeepConsumerDefaultColor() {
        var selection = new BulletColorSelection(List.of(ModBulletColors.BASE.getId(), ResourceLocation.parse("test:missing")));
        var neutral = BulletColor.builder().color(0xE7C5E0).build();
        var result = selection.resolve(LASER, LASER, 0xFF0000, false,
                id -> id.equals(ModBulletColors.BASE.getId()) ? neutral : null);
        assertEquals(ModBulletColors.BASE.getId(), result.colorId());
        assertEquals(0xFF0000, result.color());
        assertEquals(1, result.alpha());
    }

    @Test
    void alphaNormalizationAndOptionalRgbAreExplicit() {
        assertNull(BulletColor.builder().alpha(0.5F).build().color());
        assertEquals(1, BulletColor.builder().alpha(Float.NaN).build().alpha());
        assertEquals(1, BulletColor.builder().alpha(Float.POSITIVE_INFINITY).build().alpha());
        assertEquals(1, BulletColor.builder().alpha(2).build().alpha());
        assertEquals(0.5F, BulletColor.builder().alpha(-2).build().alpha());
        assertEquals(100, BulletColor.builder().priority(BulletColorPrio.URANUS).build().priorityTier());
        assertEquals(30, BulletColorPrio.URANUS.getPriority());
    }
}
