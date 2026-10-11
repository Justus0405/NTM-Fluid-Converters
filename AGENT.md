## Project Overview

**NTM Fluid Converters** is a addon mod for Minecraft 1.7.10 that bridges HBM's custom fluid system with Forge's standard fluid system. It adds two blocks:

- **HBM to Forge Converter** - accepts HBM fluids, outputs to Forge fluid handlers
- **Forge to HBM Converter** - accepts Forge fluids, outputs to HBM pipes

The mod supports two HBM forks interchangeably:

- Original HBM at `https://github.com/HbmMods/Hbm-s-Nuclear-Tech-GIT`
- JameH2 fork at `https://github.com/JameH2/Hbm-s-Nuclear-Tech-GIT`

Only HBM native fluids need to be supported on the HBM side. Forge side works with any standard Forge fluid. One jar handles both HBM fluid API generations.

## Project Structure

src/main/java/com/justus0405/ntmfluidconverters/
NTMFluidConverters.java Main mod class
CommonProxy.java Server side lifecycle hooks
ClientProxy.java Client side lifecycle hooks, texture stitching
ModBlocks.java Block registration
ModRecipes.java Crafting recipe registration
FluidConverter.java HBM to Forge fluid registration logic
FluidNetworkCompat.java Network subscription for both HBM fluid API generations
block/ Converter block implementations
tile/ Converter tile entities
client/ Texture stitching and runtime generated sprites
src/main/resources/assets/ntmfluidconverters/
lang/en_US.lang Block names and fluid display names
textures/blocks/fluids/ Shipped fluid textures, one still and one flow per fluid
tools/
generate_fluid_textures.py Texture generation script, do not delete

## Build System

- Based on the GTNH example mod template
- HBM jar lives in `libs/`, picked up dynamically via `fileTree`
- Output is always `ntmfluidconverters-<version>.jar`, no commit hash suffix
- `libs/` is gitignored, only `.gitkeep` is tracked

To build: drop any HBM jar into `libs/`, then run `./gradlew build`.

## Conventions

**Always familiarize with the project before making changes.** Read existing files first so new code matches the established style.

**Follow GTNH conventions.** When in doubt, read GT5-Unofficial at `https://github.com/GTNewHorizons/GT5-Unofficial` or other GTNH addons to see how things are done.

**Soft dependencies must not crash the mod.** Optional integrations are guarded so the mod runs cleanly without them.

**No hardcoded versions or jar filenames.** HBM jar is picked up by glob.

**One jar, both HBM fluid APIs.** Newer HBM removed the trySubscribe and tryProvide defaults. FluidNetworkCompat.java reimplements them with UNINOS calls that exist in both generations, so never call those defaults directly and never reference new-only classes like TilePort. Keep shared code on symbols present in every supported HBM jar.

**Data driven over hardcoded.** Fluid registration iterates `Fluids.getAll()` at runtime. Shipped textures and lang entries cover both forks, and any fluid without a shipped texture gets a still and flow sprite generated at runtime from its HBM color. The fallback texture remains only for fluids whose color cannot be resolved. Unknown future fluids still get a capitalized generated name with a startup warning.

**Comments are written by humans.** No em dashes, no decorative semicolons, no machine generated tone. Short plain sentences. Comments explain why, not what.

**No dead code.** Remove failed attempts, unused imports, and commented out blocks as you go.

## Testing Workflow

After every change, no matter how small:

1. Run `./gradlew build` and confirm `BUILD SUCCESSFUL`
2. Confirm the output is `ntmfluidconverters-<version>.jar`
3. For HBM API or tile changes, repeat the build with an old-generation and a new-generation HBM jar in `libs/`
4. If the change affects runtime behavior, launch the game and confirm no new warnings or errors appear
5. If errors occur, research the root cause from source code rather than guessing API names

Never comment out broken code to force a build. Fix the real cause.

## Adding New Fluids

When HBM adds new fluids in a future version:

1. Drop the new HBM jar into `libs/`
2. Run `./gradlew build`
3. Launch and check the log for lang warnings or texture generation errors
4. Fluids without a shipped png get a still and flow texture generated at runtime from their HBM color, so they already look right
5. Optional, for curated assets and nicer names: add the color to `tools/generate_fluid_textures.py` keeping the alphabetical order, run the script to generate textures, and add a `fluid.<name>=Display Name` entry to `en_US.lang`
6. Rebuild and confirm the warnings are gone

## Hard Rules

- Do not change the converter block functionality without explicit request
- Do not bundle HBM into the output jar
- Do not hardcode HBM item registry names, look them up via `GameRegistry.findItemStack`
- Do not delete `tools/generate_fluid_textures.py`
- Do not invent fluid colors or display names, always source from HBM directly

## Credits

Author: Justus0405 (`https://github.com/Justus0405`)
