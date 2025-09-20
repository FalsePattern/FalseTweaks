# FalseTweaks

FalsePattern's flagship tweaking, patching, and optimization mod.

Nightly (most likely broken) builds available [here](https://nightly.link/FalsePattern/FalseTweaks/workflows/build-nightly/master/nightly.zip)

## NOTICE
This mod completely replaces Triangulator and AnimFix. Make sure you delete these two mods before installing FalseTweaks.

## List of major features:
- Multithreaded chunk rendering (requires Beddium)
- Dynamic Lights
- Replaced minecraft's quad-based rendering with triangle-based rendering
- Fixed ambient occlusion and smooth lighting (vanilla bugs MC-138211 and MC-43968)
- Optimized beacon beam rendering
- Voxel-based item rendering that replaces the ugly vanilla renderer
- Fixed light leaking between blocks on chunk boundaries
- Tile entity render order fixes
- Optimized animated textures
- 3D rails
- Faster texture stitching, with an algorithm based on [Packing2D](https://github.com/Sciss/Packing2D)
- Fixed block light leaks
- Multithreaded mipmap generation (way faster resource pack switching)
- Multilayer items (see below)
- An improved version of the Shift+F3 profiler

And all of these tweaks can be individually toggled on/off in the config!

## Using multilayer items
Multilayer items are defined using mcmeta files, and require the VOXELIZER module to be enabled for them to function properly.

Example mcmeta format for a 3-layer texture:
```json
{
  "voxelLayers": {
    "layers": [1.0, 1.0, 0.5]
  }
}
```

The mcmeta should be attached to the base texture without any layering, and the layers should have the name of the base
texture with _0, _1, _2, ... numeric suffixes for each layer.

Example file layout:
```
myItem.png
myItem.png.mcmeta
myItem_0.png
myItem_1.png
myItem_2.png
```

## Fixing known problems
| **Issue**                                                                                      | **Potential solution**                                                                                                                                |
|------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| Game crashes on startup,<br>forge log says `MixinTargetAlreadyLoadedException`                 | Under the `00_modules` category in the config,<br>set `B:STARTUP_OPTIMIZATIONS` to `false`.                                                           |
| Game crashes on startup,<br>loading screen was already present (or fixing issue 1 didn't help) | Under the `triangulator` category in the config,<br>set `B:RENDER_HOOK_COMPAT_MODE` to `true`.                                                        |
| Crash with OptiFine                                                                            | Under the `00_modules` category in the config,<br>set `B:MIPMAP_FIX` to `false`.                                                                      |
| Crash with ShadersModCore                                                                      | Standalone shadersmod is unsupported, use optifine instead.                                                                                           |
| Crash with "IllegalArgumentException: Comparison method violates its general contract!"        | Under the `00_modules` category in the config,<br>set `B:TE_TRANSPARENCY_FIX` to `false`.                                                             |
| Flickering blocks when using optifine shaders and PBR resource packs                           | Under the `00_modules` category in the config,<br>set `B:TEXTURE_OPTIMIZATIONS` to `false`.                                                           |
| Crash with ArchaicFix                                                                          | Disable the occlusion culling feature in ArchaicFix if you enable the one in FalseTweaks. (The FalseTweaks one is compatible with FastCraft/OptiFine) |
| Buggy/invisible world with LWJGL3ify                                                           | Under the `00_modules` category in the config,<br>set `B:TEXTURE_OPTIMIZATIONS` to `false`.                                                           |

Any bug reports concerning the known problems listed in this table will be closed without response. Read before you open issues.

## Dependencies
- [FalsePatternLib](https://github.com/FalsePattern/FalsePatternLib)
- [UniMixins](https://github.com/LegacyModdingMC/UniMixins)

### Special thanks to [JProfiler](https://www.ej-technologies.com/jprofiler) ![JProfiler](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png) for their free OpenSource license!
