# Porting notes

Working document for the port of **Create: Connected** from NeoForge 1.21.1 to **Fabric / Minecraft 26.2**, targeting [Create Fly](https://github.com/ZurrTum/Create-Fly).

Written to be read cold. If you are picking this up with no context, read *State* and *Traps* first.

---

## State

**468 compile errors, down from 7,162.** The mod does not build yet and has never been launched.

Every number in this document was produced by running `gradlew compileJava`, not estimated.

| | |
|---|---|
| Repository | https://github.com/GravisLudio/create-connected-fly |
| Local path | `C:\Users\GravisLudio\dev\create-connected-fly` |
| Upstream remote | `upstream` → `hlysine/create_connected` |
| Branch | `main`, 20 commits of port work on top of upstream history |
| Reference clones | `C:\Users\GravisLudio\dev\_reference\{Create-Fly, create-connected-fabric}` |

### Environment

- **JDK 25** required (Create Fly demands it). Installed at `C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot`, pointed at via `org.gradle.java.home` in `gradle.properties` so the system `JAVA_HOME` can stay on 21.
- Gradle 9.4.1, Fabric Loom 1.16.3, MC 26.2-rc-2, fabric-api 0.152.0, Create Fly 6.0.9-1.
- Everything is cached. **Compiling needs no network.** Do not run `--refresh-dependencies` on a flaky connection — that is the one command that revalidates against remote repos. Adding a new `fabricApi.module(...)` line does need the network once, to fetch that module.

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

`item()` returns a `BlockItemBuilder` rather than the block builder, because `.properties(...)` after `.item(...)` was configuring the *item*, and the two `Properties` types cannot share one method. Chains end either `build().register()` or with a bare `register()` — Registrate allowed both, since the datagen transforms that used to sit between closed the sub-builder themselves. Both return the block entry.

`BlockEntry` and `ItemEntry` implement a small `ItemProvider` interface, standing in for Registrate's `ItemProviderEntry<?, ?>`, so the creative tab and the ponder plugin can hold one list of either.

### Block-keyed behaviours

Create exposed these as static transforms on the registrate chain (`DisplaySource.displaySource(...)`). Create Fly dropped them and fills the block-keyed registries directly after registration instead. `foundation/registrate/CCBehaviours` reproduces the chained spelling over those registries, so the call sites in `CCBlocks` read as they did.

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

### An unregistered resource condition means *true*, not false

Fabric skips condition ids it does not know and treats the resource as loading normally. So deleting a condition that should have been switched off silently **enables** the recipes it guarded. `FeatureEnabledInCopycatsCondition` can only ever return false while Copycats+ is excluded, and it is still registered for exactly this reason.

### The datagen stripper corrupted nested calls

`strip-datagen.pl` removed datagen calls nested inside *other* calls' arguments, leaving dangling receivers like `.onRegister(CCRegistrate)` — broken Java, not merely wrong. Twelve sites. Its drop list also named `blockModel` but not bare `model`, so those chains survived whole. Both fixed, but if the script is ever re-run:

```bash
grep -nE "\.(onRegister|transform)\((CCRegistrate|AssetLookup|BuilderTransformers|ModelGen|BlockStateGen)\)"
```

### Fluid amounts changed by a factor of 81

Fabric counts droplets at 81 per mB. NeoForge's `1000` is `81000`. Nothing fails at load time — the recipe just quietly asks for 81× less fluid. Spotted because a Create Fly recipe used `8100` where the NeoForge one used `100`.

### The fabric-api artifact contains no classes

`net.fabricmc.fabric-api:fabric-api` is a container: 53 nested jars and nothing of its own. Declared as plain `implementation` it puts **nothing** on the compile classpath, and every `net.fabricmc.fabric` import fails with *package does not exist* — an error that names no jar and reads like the import is simply wrong. `modImplementation` would have unpacked it, but those configurations are gone (see above), so each module has to be named:

```groovy
implementation fabricApi.module("fabric-creative-tab-api-v1", fabric_version)
```

**Artifact names do not follow package names.** `net.fabricmc.fabric.api.itemgroup.v1` was renamed to `net.fabricmc.fabric.api.creativetab.v1` and ships in `fabric-creative-tab-api-v1` — guessing `fabric-item-group-api-v1` fails at configuration time. To find the right one, list the nested jars:

```bash
unzip -l ~/.gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/fabric-api/*/*/fabric-api-*.jar
```

Then read the module's own `-sources.jar` before writing against it — several of these APIs were reshaped, not just moved.

### A generic parameter appearing on a supertype reads as a name clash

`SmartBlockEntity.addBehaviours` takes `List<BlockEntityBehaviour<?>>` now. Seventeen block entities kept the raw `List<BlockEntityBehaviour>` and therefore silently stopped overriding it — javac says *name clash ... neither overrides the other*, which does not read like a version change. Worth checking for whenever a Create supertype gains a type parameter.

### Private fields with public getters of the same name

`Level.isClientSide` is a private field now, with a public `isClientSide()`. The error is `isClientSide has private access in Level`, which reads like a missing classtweaker entry — it is not. Same story elsewhere; check for a same-named getter before widening anything.

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
- `Blocks.COPPER_BLOCK` **and `Items.COPPER_BLOCK`** are a `WeatheringCopperCollection`, reach the plain variant with `.weathering().unaffected()`
- NBT getters return `Optional`; use `getXOr(key, default)` / `getXOrEmpty(key)`, and `getList` lost its type argument
- Block entity `read`/`write` take `ValueInput` / `ValueOutput` instead of `(CompoundTag, HolderLookup.Provider)`; positions go through `view.read/store` with `BlockPos.CODEC`, sub-tags through `view.childOrEmpty`
- **`ValueInput` has no `contains()`.** `read()` returns an `Optional`, so an absent key and a null value fall through the same `orElse` — the `if (contains) read` pairs collapse to one line. It also carries its own registry lookup (`view.lookup()`), which removes the `DynamicOps` plumbing and Catnip's `CatnipCodecUtils.encode/decode`, both of which existed only to thread a `HolderLookup.Provider` through
- `ItemStack.saveOptional(provider)` is gone; the codec is the only route (`ItemStack.OPTIONAL_CODEC.encodeStart(ops, stack)`)
- `JukeboxSong.fromStack` dropped its `RegistryAccess` parameter
- `@OnlyIn(Dist.CLIENT)` → `@Environment(EnvType.CLIENT)`
- Create's `AllTags` was flattened: `AllTags.AllBlockTags` → top-level `AllBlockTags`, and the `.tag` accessor is gone
- `ItemInteractionResult` folded back into `InteractionResult`; `PASS_TO_DEFAULT_BLOCK_INTERACTION` → `TRY_WITH_EMPTY_HAND`. `InteractionResultHolder<ItemStack>` is gone — its payload lives on `InteractionResult.Success#heldItemTransformedTo`, which cannot express a stack alongside a *failure*
- `BlockAndTintGetter` **split**: common code takes `net.minecraft.world.level.BlockAndLightGetter`, client code keeps `BlockAndTintGetter` under `net.minecraft.client.renderer.block`
- `BakedQuad` → `net.minecraft.client.resources.model.geometry.BakedQuad`; `SkullModelBase` → `net.minecraft.client.model.object.skull.SkullModelBase`
- `CreativeModeTab.builder()` takes a `(Row, int)` placement; `withTabsBefore` is gone, so a tab's position relative to another mod's is no longer expressible. Create Fly passes `(null, -1)`

### Gone with no code equivalent

- **`BlockColor` / `ItemColor`.** Tinting is data-driven: a `BlockTintSource` or `ItemTintSource` declared in the model JSON. Nothing to register from code — see `CCColorHandlers`, now a stub recording which two assets need entries.
- **`BakedModel`, `MultiBufferSource`, `GuiGraphics`.** Replaced by `QuadCollection` / `BlockStateModel`, `SubmitNodeCollector`, and `GuiGraphicsExtractor` respectively. This is the largest piece of work left; see below.

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
| Fan washing catalyst tint | Renders grey instead of water-coloured | `CCColorHandlers` (stub) — needs tint entries in two JSONs |
| Creative tab ordering | The tab no longer sits after Create's palettes tab | `withTabsBefore` was removed from the builder |
| Copycats+ migration | Copycat blocks never convert to their Copycats+ equivalents | `CopycatsManager` excluded; the gated branches were collapsed to their fallbacks |

The first two are stubs with the full mapping recorded in their class docs — block, sprite shift, predicate, renderer, visual. They are ready to implement, not ready to guess at.

### Excluded from compilation

Integrations with mods that have no 26.2 release, excluded per-file in `build.gradle` rather than deleted — one line each to re-enable: **Copycats+, Additional Placements, Dye Depot, Simulated, JEI**.

Two mixins have no target at all: `ThrottleLeverBlockMixin` (aimed at Simulated, never at Create) and `SubMenuConfigScreenMixin` (Create Fly has no config UI). `ItemUseOverridesMixin` goes with `ItemUseOverrides`, which Create Fly removed outright.

341 JSONs of compat data for those mods were deleted, along with two recipes upstream had disabled with an always-false condition.

---

## What is next

The `registries/` package is essentially done — every one of its files is now under five errors. What is left splits into one large redesign and a long tail.

### Block entity renderers — the biggest piece left

**This is a redesign, not a sweep.** 26.2 split block entity rendering into two phases, and `renderSafe(be, partialTicks, poseStack, bufferSource, light, overlay)` no longer exists in any form:

| Phase | Method | Runs |
|---|---|---|
| extract | `extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay)` | reads the block entity, fills a state object |
| submit | `submit(state, poseStack, SubmitNodeCollector, cameraRenderState)` | queues draw calls, never touches the block entity |

So a renderer implements `BlockEntityRenderer<BE, S>` and **needs its own `BlockEntityRenderState` subclass** carrying everything the submit phase reads — the old code's local variables become fields. `Create Fly`'s `SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay)` and `getCardinalLighting(level)` do the common part.

`SuperByteBuffer` gains an `extractRenderState()` that yields a `SuperByteBufferRenderState`, and rotations are precomputed into `Quaternionf` fields rather than applied inline.

Read `client/content/kinetics/gearbox/GearboxRenderer` in Create Fly first: it is the closest analogue to Connected's gearbox renderers and shows the whole shape end to end.

Affected: `SixWayGearboxRenderer`, `BrassGearboxRenderer`, `KineticBridgeRenderer`, `FanCatalystRotatingHeadRenderer`, `FluidVesselRenderer`, `DashboardRenderer` — roughly 50 errors, plus whatever `CCBlockEntityRenders` needs to register them.

### The model layer — done

The eight copycat models are across, via `CCCopycatModel`. What changed:

| Was | Is now |
|---|---|
| `BakedModel` | `BlockStateModel` / `BlockStateModelPart`, under `client.renderer.block.dispatch` |
| `BakedQuad` list building | `QuadCollection`, under `client.resources.model.geometry` |
| `MultiBufferSource` | `SubmitNodeCollector` / `OrderedSubmitNodeCollector` |
| `GuiGraphics` | `GuiGraphicsExtractor` |
| NeoForge `ModelData` | no equivalent — model state travels differently |

`CopycatModel.addPartsWithInfo` replaced the old `getQuads` / `ModelData` pair: the base resolves the copycat's material from the block entity and hands it in, and the subclass appends parts rather than returning quads.

The per-part scaffolding — walk the material's parts, rebuild a `QuadCollection` for each, carry its ambient-occlusion flag and particle material through — is identical in every model, and Create Fly writes it out in full each time. `CCCopycatModel` holds it once; subclasses implement only:

```java
protected abstract void assembleQuads(
    BlockState state, Direction face,
    List<BakedQuad> source, List<BakedQuad> dest);
```

`BakedQuad` is a record now — `direction()`, and no raw vertices. `BakedModelHelper.cropAndMove(quad, aabb, offset)` takes and returns a whole quad, which is why `BakedQuadHelper` did **not** need reimplementing after all: it has no callers left. Cross it off the missing-classes list.

Still outstanding in this area: `FluidVesselModel`.

Note Create Fly's own warning on `CopycatModel`: if FRAPI is loaded, `FabricBlockStateModel#emitQuads` has to be overridden for ambient occlusion and emissive flags to survive.

### The tail

| File | Errors | What it needs |
|---|---|---|
| `content/overstressclutch/OverstressClutchBlockEntity` | 18 | Scroll-value behaviour API |
| `content/fluidvessel/FluidVesselBlock` | 16 | Last of the capability migration; `Level.random` is protected now |
| `content/inventoryaccessport/InventoryAccessPortBlockEntity` | 14 | Inventory rewrite (see below) |
| `content/fluidvessel/FluidVesselBlockEntity` | 14 | |
| `content/fluidvessel/BoilerData` | 13 | |
| `content/linkedtransmitter/LinkedTransmitterFrequencySlot` | 13 | Value-box rendering |

The `fluidvessel` package is now the largest single cluster at roughly 70 errors across six files, split between the capability migration and the rendering rewrite.

Also outstanding: **16 Create classes with no Create Fly equivalent**, which need reimplementing rather than renaming — `SafeBlockEntityRenderer`, `SmartFluidTank`, `ItemUseOverrides`, `CreateBuiltInRegistries`, `BlockEntityConfigurationPacket`, `ItemStackHandlerAccessor`, `VersionedInventoryWrapper`, `ReducedDestroyEffects`, `CreateAdvancement`, `ICapabilityProvider`, `ChuteGenerator`, `EncasedCogRenderer`, `ChainDriveGenerator`, `ClipboardOverrides`.

Two capability migrations remain: `BrassChute` and `InventoryAccessPort` (`FluidVessel` is partly done). The pattern is under *Architecture decisions*.

After it compiles, the real work starts: mixins that compile but do not apply fail **at launch**, not at build. A mixin whose signature no longer mirrors its target is silently inert.

---

## Standing risk

Create Fly is at `26.2-rc-2`. It is a release candidate and its API moves. Some of this will need redoing.
