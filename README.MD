A small mixin library that does various low-level tweaks to 1.7.10's rendering system.

CurseForge: https://www.curseforge.com/minecraft/mc-mods/triangulator

Highlights:
- Replaced minecraft's quad-based rendering with triangle-based rendering (the original scope of this project)
- Fixed ambient occlusion (fixes MC-138211)
- A publicly accessible API, which allows other mods to hook into the improved rendering system, allowing for things, such as rendering any arbitrary 3D model into the chunk itself, without the use of a TileEntity renderer
 - Optimized beacon beam rendering

The initial goal of the project was only the modernization of 1.7.10's ancient quad-based rendering, however, as time went on, more and more improvements were included

Dependencies:

- SpongeMixins https://www.curseforge.com/minecraft/mc-mods/spongemixins

### This project WILL NOT be ported to any version beyond 1.7.10, don't even ask
