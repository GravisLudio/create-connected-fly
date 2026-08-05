# Porting notes

Working document for the port of **Create: Connected** from NeoForge 1.21.1 to **Fabric / Minecraft 26.2**, targeting [Create Fly](https://github.com/ZurrTum/Create-Fly).

Written to be read cold. If you are picking this up with no context, read *State* and *Traps* first.

---

## State

**1,277 compile errors, down from 7,162.** The mod does not build yet and has never been launched.

Every number in this document was produced by running `gradlew compileJava`, not estimated.

| | |
|---|---|
| Repository | https://github.com/GravisLudio/create-connected-fly |
| Local path | `C:\Users\GravisLudio\dev\create-connected-fly` |
| Upstream remote | `upstream` → `hlysine/create_connected` |
| Branch | `main`, 7 commits of port work on top of upstream history |
| Reference clones | `C:\Users\GravisLudio\dev\_reference\{Create-Fly, create-connected-fabric}` |

### Environment

- **JDK 25** required (Create Fly demands it). Installed at `C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot`, pointed at via `org.gradle.java.home` in `gradle.properties` so the system `JAVA_HOME` can stay on 21.
- Gradle 9.4.1, Fabric Loom 1.16.3, MC 26.2-rc-2, fabric-api 0.152.0, Create Fly 6.0.9-1.
- Everything is cached. **Compiling needs no network.** Do not run `--refresh-dependencies` on a flaky connection — that is the one command that revalidates against remote repos.

---

## How to work on this

```bash
cd C:\Users\GravisLudio\dev\create-connected-fly && .\gradlew.bat compileJava --continue
```

`--continue` matters: without it javac stops early and the error count is meaningless as a progress measure. `-Xmaxerrs 10000` is already set in `build.gradle` so the full list is emitted rather than the first 100.

Useful slices of the output:

```bash
grep -oE "^  [0-9,]+ errors" <output> | tail -1          # the authoritative total
grep -oE "create_connected[\\/][a-zA-Z\\/]*\.java" <output> | sort | uniq -c | sort -rn | head
```

Per-file counts are *mentions of the path*, not error counts — good for ranking what to work on, useless for measuring small deltas. Only javac's own total is exact. This tripped me up more than once.

---

## The target: what Create Fly actually is

Not a compatibility layer. A **native Fabric rewrite** of Create 6.0.9 by ZurrTum, repackaged under `com.zurrtum.create`. Licensed CC0.

What that means in practice:

- **Same Create generation.** Connected wants Create 6.0.10, Create Fly is 6.0.9 — the API is the same era, and 19 of the 20 classes Connected's mixins target exist at identical relative paths.
- **Mojmap on both sides.** Connected was NeoForge (mojmap), Create Fly is mojmap. No remapping, only version renames.
- **No Registrate. No Porting Lib. No Fabric Transfer API.** Create Fly registers through vanilla directly and carries its own fluid/item abstractions.
- **Flywheel, Ponder and Catnip are absorbed**, not dependencies — repackaged inside Create Fly.

Compile-time artifact: `maven.modrinth:create-fly:26.2-rc-2-6.0.9-1`. Declared with plain `implementation` — see Traps.

---

## Architecture decisions

### Registrate replacement (`foundation/registrate/`)

Registrate does not exist for 26.2 and Create Fly does not use it. Rather than rewrite all 25 files under `registries/`, there is a small builder that reproduces the slice of Registrate's fluent API that Connected actually calls, backed by vanilla `Blocks.register` and `Registry.register`.

Deliberate omissions, each one there to make javac point at work that has to move:

- **No `blockstate()`, `model()`, `simpleBlock()`.** Their lambda bodies referenced Registrate datagen types; a permissive overload would not have made them compile, only hidden them. Those call sites were stripped instead — the 1,025 JSONs Registrate generated for 1.21.1 are committed under `src/generated/resources` and are migrated by hand.
- **No `renderer()` / `visual()`.** They take client classes while `CCBlockEntityTypes` runs on both sides.

`tag()` takes `TagKey<?>` rather than `TagKey<Block>`: Registrate applied block tags before `item()` and item tags after, on the same chain, and the two erase to the same signature so they cannot be overloads.

### Single source set

`splitEnvironmentSourceSets()` was tried and **abandoned**. It would have required surgery on 49 files that mix registration with rendering. With one source set the whole Create Fly jar — main and client — is on the classpath. Environment isolation stays as it was under NeoForge: `@Environment(EnvType.CLIENT)` plus a separate client mixin config.

### Block entity construction

26.2's `BlockEntitySupplier` passes only `(BlockPos, BlockState)` — the type argument is gone — while Connected's 26 block entities all take three. The builder accepts the three-argument shape and supplies the type through a **deferred reference**: the constructor needs the type and the type needs the constructor, but the supplier only runs when a block entity is created, well after registration returns. Create Fly solves the same circularity by reading an already-assigned `AllBlockEntityTypes` field from a static factory.

### Capabilities

Create Fly has no capability system. A block exposes an inventory by implementing `ItemInventoryProvider` / `FluidInventoryProvider`, which are vanilla's `WorldlyContainerHolder` underneath — so the wiring lives on the **block**, not the block entity. All five `RegisterCapabilitiesEvent` registrations are gone.

Pattern, if you need to do another:

1. Block entity: delete `@EventBusSubscriber` and `registerCapabilities`, leave a public getter.
2. Block: `implements ItemInventoryProvider<YourBE>` plus `getInventory(world, pos, state, be, dir)` returning that getter. **`getBlockEntityClass()` already comes from Create's `IBE`** — do not redeclare it.
3. Neighbour lookups: `ItemHelper.getInventory(world, pos, dir)`, not `level.getCapability(...)`.
4. `invalidateCapabilities()` → `ItemHelper.invalidateInventoryCache(worldPosition)`.

---

## Traps

Things that cost real time. Most produce no error, or an error pointing somewhere else entirely.

### Loom silently ignores classtweaker entries that do not resolve

It does **not** fail the build. It skips them, and what you see is the original `has private access`, exactly as if you had never written the entry. Verify the descriptor against the jar before trusting it:

```bash
javap -p -cp ~/.gradle/caches/fabric-loom/26.2-rc-2/minecraft-common.jar net.minecraft.<class>
```

There are three jars — `minecraft-common`, `minecraft-client`, `minecraft-client-only` — and the class may be in any of them.

Concrete case: `JukeboxBlockEntity.jukeboxSongPlayer` is `net.minecraft.world.item.JukeboxSongPlayer`, not `world.level.block.entity.*`. And it needed no widening at all — 26.2 has a public `getSongPlayer()`. **Check for a getter before adding a classtweaker entry.**

### An unresolvable parameter type reports as an ambiguity somewhere else

`ResourceLocation` was renamed to `Identifier`. The symptom was not "cannot find ResourceLocation" — it was 56 errors reading `reference to registerDependent is ambiguous` on overloads that had nothing to do with it. When overload resolution complains for no visible reason, check whether a parameter type resolves at all.

### `modImplementation` does not exist

In Loom 1.16 / MC 26.2 the production namespace is already mojmap, so there is no remapping step and the `mod*` configurations are gone. Mod dependencies go in with plain `implementation` / `compileOnly`, exactly as Create Fly declares JEI, Sodium and Iris.

### awk turns `"\\b"` into a literal backspace

A generated sed script full of `<BS>com.simibubi...<BS>` matches nothing, and the script cheerfully reports success. The first mass rename did nothing at all and I nearly built on top of it. **Always count before and after.**

### The datagen stripper corrupted nested calls

`strip-datagen.pl` removed datagen calls nested inside *other* calls' arguments, leaving dangling receivers like `.onRegister(CCRegistrate)` — broken Java, not merely wrong. Twelve sites. Its drop list also named `blockModel` but not bare `model`, so those chains survived whole. Both fixed, but if the script is ever re-run:

```bash
grep -nE "\.(onRegister|transform)\((CCRegistrate|AssetLookup|BuilderTransformers|ModelGen|BlockStateGen)\)"
```

### Fluid amounts changed by a factor of 81

Fabric counts droplets at 81 per mB. NeoForge's `1000` is `81000`. Nothing fails at load time — the recipe just quietly asks for 81× less fluid. Spotted because a Create Fly recipe used `8100` where the NeoForge one used `100`.

### `javax.annotation` does not exist here

Use `org.jetbrains.annotations`. And when adding imports with a script, check whether the file already imports a different type with the same simple name — I created a `Nullable` collision that way.

---

## Reference material

In `dev/_reference/`, all reusable:

| File | What it is |
|---|---|
| `createfly-main-classes.txt` | 1,737 classes in Create Fly's main source set |
| `createfly-client-classes.txt` | 1,966 classes in its client source set |
| `import-mapping.tsv` | Deterministic old→new import map with a main/client column |
| `import-unmatched.txt` | What Create Fly does not have |
| `strip-datagen.pl` | Removes datagen calls from builder chains, balanced-paren aware |

The mapping was built by taking, for each import, the first segment starting with a capital letter — which handles `import static X.method` and nested classes — then looking it up in both class lists. **263 of 299 unambiguous (88%).**

Worth knowing: only **17 class names exist in both source sets** out of 3,703, and Connected touches three (`AllConfigs`, `Create`, `VecHelper`). The blind-find-and-replace risk was far smaller than it looked.

Fully-qualified references in code never appear as imports, so sweep for those separately:

```bash
grep -o "com\.simibubi\.create\.[A-Za-z0-9_.]+"
```

Missing that gap left `extends com.simibubi...BoilerData` unmapped, which broke the superclass and produced 240 errors in one file.

---

## Vanilla 1.21.1 → 26.2 renames found so far

- `ResourceLocation` → `Identifier` (`net.minecraft.resources.Identifier`)
- `RenderType` → `net.minecraft.client.renderer.rendertype.RenderType`; chunk layers are `ChunkSectionLayer`
- `net.minecraft.references.BlockItemId` is new — pairs a block key with an item key
- `Blocks.COPPER_BLOCK` is a `WeatheringCopperCollection`, reach the plain variant with `.weathering().unaffected()`
- NBT getters return `Optional`; use `getXOr(key, default)` / `getXOrEmpty(key)`, and `getList` lost its type argument
- Block entity `read`/`write` take `ValueInput` / `ValueOutput` instead of `(CompoundTag, HolderLookup.Provider)`; positions go through `view.read/store` with `BlockPos.CODEC`, sub-tags through `view.childOrEmpty`
- `JukeboxSong.fromStack` dropped its `RegistryAccess` parameter
- `@OnlyIn(Dist.CLIENT)` → `@Environment(EnvType.CLIENT)`
- Create's `AllTags` was flattened: `AllTags.AllBlockTags` → top-level `AllBlockTags`, and the `.tag` accessor is gone

### Data and asset formats

- Item models need a **definition file** at `assets/<ns>/items/<name>.json` alongside `models/item/`
- `overrides` + `predicate` was removed from the model format
- `neoforge:conditions` → `fabric:load_conditions`, and each entry's discriminator goes from `"type"` to **`"condition"`** (confirmed by reading `ResourceCondition.class` in fabric-resource-conditions-api-v1)
- Fluids leave `ingredients` for a separate `fluid_ingredients` array typed `fluid_stack`

---

## What is missing on purpose

**None of these break the build. All of them need checking in game.** This list is the test script for the first successful launch.

| What | Consequence | Where |
|---|---|---|
| Block entity renderers and Flywheel visuals | Cogwheels and clutches do not turn, fluid vessel shows no fluid, dashboard shows no text | `client/CCBlockEntityRenders` |
| Connected textures | Encased gearboxes and the item silo show unconnected casing | `client/CCConnectedTextures` |
| Server→client config sync | Feature toggles can disagree between sides | `config/CCommon` |
| Item-use priority | Right-clicking a linked transmitter holding a placeable item may place it | `registries/PreciseItemUseOverrides` |
| Battery charge level | Kinetic battery renders empty at every charge | `assets/.../items/kinetic_battery.json` |
| Config reload hook | Toggling a feature does not refresh item visibility until restart | `config/CFeatures` |
| Feature toggle UI | No in-game config screen; toggles are edited by file | Create Fly has no `catnip.config.ui` |

The first two are stubs with the full mapping recorded in their class docs — block, sprite shift, predicate, renderer, visual. They are ready to implement, not ready to guess at.

### Excluded from compilation

Integrations with mods that have no 26.2 release, excluded per-file in `build.gradle` rather than deleted — one line each to re-enable: **Copycats+, Additional Placements, Dye Depot, Simulated, JEI**.

Two mixins have no target at all: `ThrottleLeverBlockMixin` (aimed at Simulated, never at Create) and `SubMenuConfigScreenMixin` (Create Fly has no config UI). `ItemUseOverridesMixin` goes with `ItemUseOverrides`, which Create Fly removed outright.

341 JSONs of compat data for those mods were deleted, along with two recipes upstream had disabled with an always-false condition.

---

## What is next

No systemic transformation is left. The first ~5,900 errors fell to six or seven sweeping passes; the remaining 1,277 will fall in tens.

Ranked by size:

| File | Errors | What it needs |
|---|---|---|
| `registries/CCBlocks` | 76 | Leftover datagen references, `Tags`, block API changes |
| `content/kineticbattery/KineticBatteryBlock` | 66 | Vanilla block API |
| `registries/CCPonderPlugin` | 60 | Create Fly's ponder API (`com.zurrtum.create.client.ponder`) |
| `content/fluidvessel/FluidVesselBlock` | 56 | Vanilla block API |
| `content/overstressclutch/OverstressClutchBlockEntity` | 52 | |
| `content/copycat/wall/CopycatWallBlock` | 52 | |

Also outstanding: **16 Create classes with no Create Fly equivalent**, which need reimplementing rather than renaming — `BakedQuadHelper`, `SafeBlockEntityRenderer`, `SmartFluidTank`, `ItemUseOverrides`, `CreateBuiltInRegistries`, `BlockEntityConfigurationPacket`, `ItemStackHandlerAccessor`, `VersionedInventoryWrapper`, `ReducedDestroyEffects`, `CreateAdvancement`, `ICapabilityProvider`, `ChuteGenerator`, `EncasedCogRenderer`, `ChainDriveGenerator`, `ClipboardOverrides`.

After it compiles, the real work starts: mixins that compile but do not apply fail **at launch**, not at build. A mixin whose signature no longer mirrors its target is silently inert.

---

## Standing risk

Create Fly is at `26.2-rc-2`. It is a release candidate and its API moves. Some of this will need redoing.
