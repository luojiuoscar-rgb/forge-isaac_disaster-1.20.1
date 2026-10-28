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
5. Check the original `16x16` image, the generated `8x8` preview, and the icon in the HUD context.
6. If it still reads poorly at `8x8`, fix the PNG. Do not patch the HUD renderer to hide the problem.
7. Finish by checking the resource path, PNG size, and git diff.

## Rules

- Keep the source texture at `16x16`.
- The game still displays the icon at `8x8`.
- Use transparent background, integer pixels, and nearest-neighbor scaling only.
- Keep a continuous dark or black outer outline.
- Preserve a clear silhouette after shrinking.
- Favor upper-left lighting.
- Avoid fragmented detail, isolated bright pixels, noisy dark pixels, and thin lines.
- Do not crop detailed item art and reuse it directly as a HUD icon.
- Do not change HUD code for scaling, outlines, overlays, or animation.
- If the icon needs more visual weight, redraw the PNG.
- Use this skill only for revive HUD icons. Use `isaac-disaster-item-creation` for ordinary item icons.

## Validation Script

`scripts/validate_revive_hud_icon.py` checks PNG format, exact `16x16` size, transparency, and writes an `8x8` nearest-neighbor preview to a separate path. It exits nonzero on contract errors.
