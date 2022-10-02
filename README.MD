# FalseTweaks

FalsePattern's flagship tweaking, patching, and optimization mod.

## NOTICE
This mod completely replaces Triangulator and AnimFix. Make sure you delete these two mods before installing FalseTweaks.

## List of major features:
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

And all of these tweaks can be individually toggled on/off in the config!

## Fixing known problems
| **Issue**                                                                                      | **Potential solution**                                                                         |
|------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| Game crashes on startup,<br>forge log says `MixinTargetAlreadyLoadedException`                 | Under the `00_modules` category in the config,<br>set `B:STARTUP_OPTIMIZATIONS` to `false`.    |
| Game crashes on startup,<br>loading screen was already present (or fixing issue 1 didn't help) | Under the `triangulator` category in the config,<br>set `B:RENDER_HOOK_COMPAT_MODE` to `true`. |
| Crash with OptiFine                                                                            | Under the `00_modules` category in the config,<br>set `B:MIPMAP_FIX` to `false`.               |

Any bug reports concerning the known problems listed in this table will be closed without response. Read before you open issues.

## Dependencies
- [FalsePatternLib](https://github.com/FalsePattern/FalsePatternLib)
- [GasStation](https://github.com/FalsePattern/GasStation)
