---
name: isaac-disaster-revive-hud-creation
description: Use when creating, updating, or checking revive HUD icons or revive-module HUD textures for the Isaac Disaster Forge 1.20.1 project, including 16x16 PNG source art, 8x8 HUD readability, preview validation, or texture-path issues. Do not use for ordinary item icons.
---

# Isaac Disaster Revive HUD Creation

## Workflow

1. Inspect `src/main/java/net/luojiuoscar/isaac_disaster/registries/revive_module/ReviveModule.java` and `src/main/java/net/luojiuoscar/isaac_disaster/client/hud/ReviveHudOverlay.java`.
2. Confirm the module texture path and icon meaning before editing art.
3. Edit the source as a transparent `16x16` PNG.
4. Run `scripts/validate_revive_hud_icon.py <source.png> --preview <preview.png>`.
5. Check the original `16x16` image and the generated `8x8` preview.
6. Compare the new icon side by side with the existing revive HUD icons, especially `totem_of_undying.png`, to confirm family resemblance and role separation.
7. Check the icon in the health-HUD context.
8. If it still reads poorly at `8x8` or clashes with the revive icon family, fix the PNG. Do not patch the HUD renderer to hide the problem.
9. Finish by checking the resource path, PNG size, and git diff.

## Rules

- Keep the source texture at `16x16`.
- The game still displays the icon at `8x8`.
- Treat revive HUD icons and ordinary item icons as different assets with different readability goals. Redraw for HUD readability instead of cropping, shrinking, or lightly editing the ordinary item icon.
- Use transparent background, integer pixels, and nearest-neighbor scaling only.
- Keep a continuous dark or black outer outline.
- Preserve a clear silhouette after shrinking.
- Favor upper-left lighting.
- Keep the revive icon family visually consistent: match the existing family in outline strength, lighting direction, visual weight, and HUD rhythm, while preserving a distinct silhouette for each revive source.
- Avoid fragmented detail, isolated bright pixels, noisy dark pixels, and thin lines.
- Do not crop detailed item art and reuse it directly as a HUD icon.
- When adapting a familiar item concept such as `1up!`, keep the core semantic cue but reorganize the silhouette and pixel hierarchy so the icon reads as HUD art first, item reference second.
- Do not change HUD code for scaling, outlines, overlays, or animation.
- If the icon needs more visual weight, redraw the PNG. If the `8x8` result looks too thin, noisy, or blurry, thicken the outline, enlarge major masses, and remove interior detail before trying any other trick.
- Use this skill only for revive HUD icons. Use `isaac-disaster-item-creation` for ordinary item icons, and use both skills together when a revive passive item needs both asset types.

## Validation Script

`scripts/validate_revive_hud_icon.py` checks PNG format, exact `16x16` size, transparency, and writes an `8x8` nearest-neighbor preview to a separate path. It exits nonzero on contract errors.
