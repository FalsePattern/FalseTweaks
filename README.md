CurseForge: https://www.curseforge.com/minecraft/mc-mods/animfix

A mixin-based library to improve the horrible stuttering in heavily modded games caused by animated textures (also known as Animated Terrain from OptiFine, but this mod actually optimizes the way said animations are uploaded to the graphics card, instead of just outright turning them off and calling that a "fix").

This happens because each individual animated texture has to separately upload its contents to the GPU. Each of these uploads has a nonzero overhead, which quickly spirals out of control when faced with hundreds or even thousands of animated textures.

This mod fixes that problem by batching those animated textures into a single gigantic texture, which gets uploaded in a single step. This causes a little bit of extra VRAM usage, but with modern hardware it's negligible.

The batching has a user-configurable limit, see the mod's config file for more information.


## Dependencies
- [FalsePatternLib](https://www.curseforge.com/minecraft/mc-mods/falsepatternlib)
- Either [SpongeMixins](https://www.curseforge.com/minecraft/mc-mods/spongemixins) or [Grimoire](https://www.curseforge.com/minecraft/mc-mods/grimoire-api)

### This project WILL NOT be ported to any version beyond 1.7.10, don't even ask.
