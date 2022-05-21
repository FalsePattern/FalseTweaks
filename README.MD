A small mixin library that does various low-level tweaks to 1.7.10's rendering system.

Highlights:
- Replaced minecraft's quad-based rendering with triangle-based rendering (the original scope of this project)
- Fixed ambient occlusion (fixes MC-138211)
- A publicly accessible API, which allows other mods to hook into the improved rendering system, allowing for things, such as rendering any arbitrary 3D model into the chunk itself, without the use of a TileEntity renderer
 - Optimized beacon beam rendering

The initial goal of the project was only the modernization of 1.7.10's ancient quad-based rendering, however, as time went on, more and more improvements were included

## Dependencies
- [FalsePatternLib](https://github.com/FalsePattern/FalsePatternLib)
- [SpongeMixins](https://github.com/TimeConqueror/SpongeMixins)

### This project WILL NOT be ported to any version beyond 1.7.10, don't even ask

[![POOP badge](https://raw.githubusercontent.com/gist/poop-person/991e80f390384bbeef09d208bff208f4/raw/a9ef83add84a70f2202896c2d81117ff7b169be1/poop-badge.svg)](https://gist.github.com/poop-person/991e80f390384bbeef09d208bff208f4)
