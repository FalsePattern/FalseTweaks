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

- **Issue**: Game crashes on startup, forge log says `MixinTargetAlreadyLoadedException` 
  **Known causes**: CoFH Core

  **Potential solution**: Open `falsetweaks.cfg`, and under the `modules` category, set `B:STARTUP_OPTIMIZATIONS` to `false`.

- **Issue**: Game crashes on startup, loading screen was already present (or fixing issue 1 didn't help)
  **Known causes**: Apparatus

  **Potential solution**: Open `falsetweaks.cfg`, and under the `general` category, set `B:RENDER_HOOK_COMPAT_MODE` to `true`.

## Dependencies
- [FalsePatternLib](https://github.com/FalsePattern/FalsePatternLib)
- [GasStation](https://github.com/FalsePattern/GasStation)
