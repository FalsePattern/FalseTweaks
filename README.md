A small mod to remove the stuttering caused by the large amount of animated textures in heavily modded instances.

This is achieved by replacing the vanilla one-by-one texture uploading system with a batch-based system.

Every time an animated texture tries to upload, it gets recorded in a buffer, which is then processed by a second thread
running parallel to the client thread.

The second part of the optimization is deferred texture updates, where every single animated texture is delayed by 1 tick
to achieve double buffering. This way, the worker thread can process the textures while the game is still running,
and the only lag that happens is a *single* texture update per atlas per tick, instead of hundreds or thousands per tick.

## Dependencies
- [FalsePatternLib](https://github.com/FalsePattern/FalsePatternLib)
- [GasStation](https://github.com/FalsePattern/GasStation)
