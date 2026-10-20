# Mom's Knife runtime model

The editable approved model is mom_knife.bbmodel. Geometry and texture design stay in Blockbench.
mom_knife.obj is the native Blockbench OBJ export (46 quads / 92 triangles).
Its coordinates are in blocks (1/16 of Blockbench model units).
The OBJ references materials.mtl; the converter reads those material UUIDs directly from
the embedded PNG composites in the bbmodel, so no external MTL or separate source PNG is required.

To update:
1. Edit and visually approve the model in Blockbench.
2. Export both bbmodel and OBJ from the same project through Blockbench, replacing these two sources.
3. Run python blockbench/familiar/mom_knife/generate_runtime.py (Python 3 + Pillow).
4. Run the same command with --check, then compile and run the MomKnife tests.

The converter preserves native face order, normals and UVs. It packs the approved silver,
dark handle and wood composites into one 64x64 atlas without resampling (other hidden/reference
textures stay only in the editable source).
Runtime assets: MomKnifeMesh.java and textures/entity/familiar/mom_knife.png.
There is no runtime OBJ parsing or added model-loader dependency.

The handle tail at Blockbench (-1, -6.5, 0) becomes the entity origin.
A 180-degree X rotation aligns the tip with the existing negative-Y attack axis.
The 26.5-unit axial length is scaled to the existing 0.84-block blade reach.
The silhouette remains asymmetric around that axis, as in the approved source.
Yaw, pitch, twist and attack timing remain supplied by the existing entity.
