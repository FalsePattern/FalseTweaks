# FalseTweaks

FalsePattern's flagship tweaking, patching, and optimization mod.

Highlights:
- Replaced minecraft's quad-based rendering with triangle-based rendering
- Fixed ambient occlusion and smooth lighting (fixes MC-138211 and MC-43968)
- Optimized beacon beam rendering
- Optimized item rendering
- Tile entity render order fixes
- Optimized animated textures
- Made rails 3D
- Sped up texture packing

## Fixing known problems
| **Issue**                                                                                   | **Known causes** | **Potential solution**                                                                                    |
|---------------------------------------------------------------------------------------------|------------------|-----------------------------------------------------------------------------------------------------------|
| Game crashes on startup, forge log says `MixinTargetAlreadyLoadedException`                 | [unknown]        | Open `falsetweaks.cfg`, and under the `00_modules` category, set `B:STARTUP_OPTIMIZATIONS` to `false`.    |
| Game crashes on startup, loading screen was already present (or fixing issue 1 didn't help) | Apparatus        | Open `falsetweaks.cfg`, and under the `triangulator` category, set `B:RENDER_HOOK_COMPAT_MODE` to `true`. |

Any bugs concerning the known problems listed in this table will be closed without response. Read before you open issues.

## Dependencies
- [FalsePatternLib](https://github.com/FalsePattern/FalsePatternLib)
- [GasStation](https://github.com/FalsePattern/GasStation)
