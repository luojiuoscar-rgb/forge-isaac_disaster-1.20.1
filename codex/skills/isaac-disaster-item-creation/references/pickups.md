# Pickup Constraints

This branch currently records confirmed card-texture and workflow-review constraints. A complete creation procedure for cards, pill effects, pill appearances, and other pickups remains to be specified; do not claim it is already defined here.

## Card Texture Contract

- Obtain the correct Repentance card PNG from the English Wiki. Inspect the downloaded dimensions; do not assume the original is `18x20` or `16x20`.
- Use a `20x20` transparent output canvas for card textures by default. The collectible `32x32` rule does not apply.
- Adapt the canvas dimensions without scaling the card artwork. Center the source at integer pixel coordinates on the transparent canvas and preserve its pixels, aspect ratio, and transparency. For `18x20`, add one transparent column on each side; for `16x20`, add two on each side. An existing `20x20` image needs no padding.
- Do not resize, interpolate, distort, or crop the card to fit. If the source exceeds the canvas or has an unusual layout that cannot be handled by padding, explain the dimensions and request the intended treatment before altering the artwork.
- Verify the output is an actual `20x20` PNG and visually check placement and preserved pixel detail. For padded inputs, verify the placed source region matches the original pixels and the added border is transparent. Report failed downloads or unresolved texture work through the common final-report rules.
- This contract concerns card textures; do not impose `20x20` on pills, horse pills, or other pickup assets.

## Reviewing Existing Use Flows

- Treat current source as a reference for available APIs and call relationships. It may contain historical inconsistencies; its current `onUse` sequence is not automatically the desired contract.
- Before adopting a pickup reference, trace the relevant path through event publication/cancellation, eligibility checks, effect execution, physical consumption, sound playback, use-record updates, and returned interaction result. Include replay paths when relevant; do not assume they have a stack or hand.
- When these stages disagree, report the concrete call path and observed code behavior, propose a consistent semantic contract, and ask about unresolved design choices. Do not silently preserve the inconsistency or make an unrelated shared-system repair.
- A proposed shared-flow correction must identify affected callers and be included in the user-approved implementation plan. Verify the implementation against confirmed use semantics rather than requiring new pickups to reproduce an old sequence.
