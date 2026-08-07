# Porting notes

Working document for the port of **Create: Connected** from NeoForge 1.21.1 to **Fabric / Minecraft 26.2**, targeting [Create Fly](https://github.com/ZurrTum/Create-Fly).

Written to be read cold. If you are picking this up with no context, read *State* and *Traps* first.

---

## State

**It builds and it starts loading.** `gradlew build` is green, down from 7,162 compile errors.

**Launch is now the frontier.** All ~50 mixins apply cleanly, and block and item registration
completes. What remains fails at runtime, and that is a different kind of work from what came
before — see *The launch phase* for what has already been cleared and how, and *What is next* for
where it stands.

Every number in this document was produced by running the tool, not estimated.

| | |
|---|---|
| Repository | https://github.com/GravisLudio/create-connected-fly |
| Local path | `C:\Users\GravisLudio\dev\create-connected-fly` |
| Upstream remote | `upstream` → `hlysine/create_connected` |
| Branch | `main`, 46 commits of port work on top of upstream history |
| Reference clones | `C:\Users\GravisLudio\dev\_reference\{Create-Fly, create-connected-fabric}` |

### Environment

- **JDK 25** required (Create Fly demands it). Installed at `C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot`, pointed at via `org.gradle.java.home` in `gradle.properties` so the system `JAVA_HOME` can stay on 21.
- Gradle 9.4.1, Fabric Loom 1.16.3, MC 26.2-rc-2, fabric-api 0.152.0, Create Fly 6.0.9-1.
- Everything is cached. **Compiling needs no network.** Do not run `--refresh-dependencies` on a flaky connection — that is the one command that revalidates against remote repos. Adding a new `fabricApi.module(...)` line does need the network once, to fetch that module.

```bash
cd C:\Users\GravisLudio\dev\create-connected-fly && .\gradlew.bat build
```

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

That workflow is kept for whenever a Create Fly bump breaks the build again. Right now the build is green, so the useful command is `build` — see *Environment*.

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

**Access wideners are not transitive.** Create Fly opens plenty of vanilla internals in its own classtweaker, and none of that reaches us — every field it reads that we also read needs our own entry. There are five active entries so far. Three were found this way -- `BlockEntityRenderState.blockState`, `BlockModelResolver.modelManager`, and `BlockBehaviour.getCloneItemStack`, the last of which Create Fly does *not* open because it never delegates to another block instance the way the `Linked*` blocks do. Create Fly's classtweaker is inside its jar (`create.classtweaker`) and is worth grepping when something reads as private that Create Fly clearly touches.

Concrete case: `JukeboxBlockEntity.jukeboxSongPlayer` is `net.minecraft.world.item.JukeboxSongPlayer`, not `world.level.block.entity.*`. And it needed no widening at all — 26.2 has a public `getSongPlayer()`. **Check for a getter before adding a classtweaker entry.**

### An unresolvable parameter type reports as an ambiguity somewhere else

`ResourceLocation` was renamed to `Identifier`. The symptom was not "cannot find ResourceLocation" — it was 56 errors reading `reference to registerDependent is ambiguous` on overloads that had nothing to do with it. When overload resolution complains for no visible reason, check whether a parameter type resolves at all.

### `modImplementation` does not exist

In Loom 1.16 / MC 26.2 the production namespace is already mojmap, so there is no remapping step and the `mod*` configurations are gone. Mod dependencies go in with plain `implementation` / `compileOnly`, exactly as Create Fly declares JEI, Sodium and Iris.

### A regex replacement can leave a group reference as literal text

Sweeping `neighborChanged` across six blocks, the replacement string referenced a capture group the pattern did not have, so every one of them ended up with a parameter literally named `$12`. It compiles as far as the *signature* — the error surfaces further down as an unknown variable, pointing at the body rather than the sweep. After any scripted signature change, grep the tree for `\$\d+` before moving on.

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

It is not only recipes. `BoilerData.waterSupplyPerLevel` was `10` and had to become `10 * 81`, because the supply it is compared against is now counted in droplets — Create Fly's own copy says `10 * 81` for the same reason, and its goggle tooltip divides by 81 for display. Any constant that is compared against a fluid amount is suspect, not just the ones in JSON.

### The fabric-api artifact contains no classes

`net.fabricmc.fabric-api:fabric-api` is a container: 53 nested jars and nothing of its own. Declared as plain `implementation` it puts **nothing** on the compile classpath, and every `net.fabricmc.fabric` import fails with *package does not exist* — an error that names no jar and reads like the import is simply wrong. `modImplementation` would have unpacked it, but those configurations are gone (see above), so each module has to be named:

```groovy
implementation fabricApi.module("fabric-creative-tab-api-v1", fabric_version)
```

Eight are declared so far. The most recent, `fabric-object-builder-api-v1`, is there for one method: `FabricBlockEntityType.addValidBlock`.

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

### A block entity that hard-codes its own type, and the three places that read it

Create Fly gave most of its block entities a **two-argument constructor** that hard-codes their type — `CopycatBlockEntity` passes `AllBlockEntityTypes.COPYCAT`, `AnalogLeverBlockEntity` passes `ANALOG_LEVER`, and so on. Upstream Connected subclassed those and passed its own type in, which is no longer expressible.

`BlockEntity.type` is private. Exactly three methods read it, and **all three are overridable and all three matter**:

```java
@Override public BlockEntityType<?> getType()                  { return type; }
@Override public Holder<BlockEntityType<?>> typeHolder()       { return type.builtInRegistryHolder(); }
@Override public boolean isValidBlockState(BlockState state)   { return type.isValid(state); }
```

Overriding only `getType` looks right and is not: `typeHolder` is what writes the saved id, so the block entity would come back from disk as Create's plain one. `isValidBlockState` is checked against the type's valid-blocks set, which will not contain our block. Neither failure appears at build time. `LinkedAnalogLeverBlockEntity` and `ShearPinBlockEntity` do all three.

Two cases did not need it, and are worth copying instead:

- **The subclass adds nothing structural.** The inverted clutch and gearshift are only a `getRotationSpeedModifier` override, so they extend `SplitShaftBlockEntity` — the shared parent, which still takes a type.
- **We do not need our own type at all.** The copycats join *Create's* type through Fabric's `((FabricBlockEntityType) type).addValidBlock(block)`, from `fabric-object-builder-api-v1`. Their block entities now save as `create:copycat`.

### Goggle tooltips are behaviours now, and a block entity that implements the interface shows nothing

`GoggleOverlayRenderer` looks up a `TooltipBehaviour` at the position and **never touches the block entity**. A block entity that merely implements `IHaveGoggleInformation` compiles, keeps its method, and displays nothing — silently. `FluidVesselBlockEntity` was in exactly that state.

The same goes for `addToTooltip` (the no-goggles hover text) and for `tickAudio`, both of which used to sit on the block entity. All of it lives under `client/tooltip/` and `client/` now and is registered in `CCBlockEntityBehaviours`.

Which brings up the thing that made all of the above invisible: **`CCBlockEntityBehaviours.register()` was never called.** The client entrypoint did not list it, so every client behaviour — the three scroll values that were already ported — was dead. It is wired now, but the shape of that bug is worth remembering: a registry class that nothing calls raises nothing anywhere.

### `create_connected.client.mixins.json` named a package that has no classes

It declared `com.hlysine.create_connected.client.mixin`; every client mixin actually lives in `com.hlysine.create_connected.mixin`. All four would have failed to apply at launch. The build has no opinion about it. Worth re-checking after any package move.

### `onItemUseFirst` was NeoForge's, and Create Fly reimplements it by mixin

The hook that lets an item act *before* the block it is pointed at (the crank wheel placing diagonally off a cogwheel, the linked transmitter attaching to a lever) does not exist in vanilla. Create Fly turns it into a static `onItemUseFirst(world, player, stack, hand, ray, pos)` returning null for "not handled", and calls a hardcoded list of them from mixins on `ServerPlayerGameMode.useItemOn` and `MultiPlayerGameMode.performUseItemOn`.

There is no extension point in that list, so Connected has its own pair — `mixin/crankwheel/{Server,Client}CrankWheelPlacementMixin` — injected at the same target. Both halves are needed; the server one alone leaves the client mispredicting the placement.

---

## Reference material

> **`_reference/` is a sibling of this repository, not a directory inside it** — the layout is
> `dev/{create-connected-fly, _reference}`. A tool or agent whose working directory is the repo
> cannot see it. Work from `C:\Users\GravisLudio\dev` instead, or grant access explicitly.

**`_reference/Create-Fly` is the single most useful thing here.** It is the full source of the
target, and nearly every question this port raises — what did this method become, how is this
registered now, does an equivalent even exist — is answered faster by grepping it than by reasoning
from the API. Where Create Fly solved the same problem, copy its answer; it is a native 26.2 mod
and its solutions are known to work. `_reference/create-connected-fabric` is the authorised 1.20.1
Fabric port: useful for seeing *how* a NeoForge-ism was resolved, but it leans on Porting Lib and
Registrate, neither of which exists here, so it is never a source to copy from.

Alongside them, in `dev/_reference/` directly, all reusable:

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

### Block API, all confirmed against the jar

- `updateShape` reordered and grew two parameters: `(state, LevelReader, ScheduledTickAccess, pos, direction, neighborPos, neighborState, RandomSource)`
- `onRemove` → `affectNeighborsAfterRemoval(state, ServerLevel, pos, isMoving)` — the new state is no longer passed, since by the time it runs the block is gone
- `propagatesSkylightDown` and `getOcclusionShape` take the state alone
- `getExplosionResistance` is a no-arg accessor on `Block`
- `neighborChanged` takes a `net.minecraft.world.level.redstone.Orientation` where it took the source position
- `getAnalogOutputSignal` gained the side
- `getCloneItemStack(LevelReader, BlockPos, BlockState, boolean includeData)` — no `HitResult`, no `Player`, reordered, and **protected**
- `getSoundType(state)` only — a block can no longer vary its sound by who is placing it
- `Level.random` is protected behind `getRandom()`; `Level.getPlayerByUUID` is `getPlayerInAnyDimension`
- `ServerPlayer.getServer()` is gone — reach the server through `level()`
- `ValueBoxTransform.rotate` / `shouldRender` / `getLocalOffset` dropped their level and position
- `SmartBlockEntity.addBehaviours` takes `List<BlockEntityBehaviour<?>>` — a raw list silently stops overriding it
- `Item.getDescriptionId` is final; declare a custom key with `Properties.overrideDescription`
- `Direction.getNearest` lost its three-int overload (`getApproximateNearest`), and `getNormal` is `getUnitVec3i`
- `Direction.fromDelta` is gone entirely, and no replacement returns null for a non-unit delta — `foundation/DirectionHelper` reimplements it
- `rotate` and `mirror` take the state alone; `rotate` lost its level and position
- **`getPistonPushReaction` is not an override.** It moved into `Properties.pushReaction(...)`, so it is set at registration
- **`canConnectRedstone` is Create Fly's, not vanilla's** — implement `foundation.block.RedStoneConnectBlock`, signature `(BlockState, @Nullable Direction)`
- `getLightEmission(state, level, pos)` is gone; light is baked per state from `Properties.lightLevel(ToIntFunction<BlockState>)`, so anything dynamic needs a state property to read (see `FluidVesselBlock.LIGHT_LEVEL`)
- `Level.addParticle` grew a second boolean; `Level.markAndNotifyBlock` is gone
- `Level.updateNeighborsAt` / `updateNeighborsAtExceptFromFacing` take a trailing `Orientation`, and the vanilla path passes **null** for it — so a block receiving `neighborChanged` cannot recover the source position from it
- `Player.displayClientMessage` is gone; the action bar is `Minecraft.getInstance().gui.hud.setOverlayMessage`
- `getShapeForEachState` returns a `Function<BlockState, VoxelShape>`, not an `ImmutableMap`
- `SignApplicator.canApplyToSign` / `tryApplyToSign` both take the held `ItemStack` now
- `ItemTags.create` / `FluidTags.create` are gone — `TagKey.create(Registries.ITEM, id)`
- `Item.appendHoverText` takes `(stack, context, TooltipDisplay, Consumer<Component>, flag)` — a consumer, not a list
- The crafting remainder is `Item.getCraftingRemainder()` returning a nullable `ItemStackTemplate`; `stack.create()` makes it a stack
- `ItemStackHandler` is a vanilla `Container`: the change hook is `setChanged()`, with no slot
- `LivingEntity.getSlotForHand` is gone — pick `EquipmentSlot.MAINHAND` / `OFFHAND` off the hand
- `I18n.exists` is gone; `Language.getInstance().has(key)`
- `NbtUtils.writeBlockPos` is gone — `tag.store(key, BlockPos.CODEC, pos)`
- `ResourceKey.location()` is `identifier()`; `RegistryAccess.registryOrThrow` is `lookupOrThrow`; `Registry.getHolder(int)` is `get(int)`
- `PlayerLookup.world` is `PlayerLookup.level` (Fabric API)
- Reading raw NBT into a block entity goes through `TagValueInput.create(problemReporter, registryAccess, tag)`

### Gone with no code equivalent

- **`BlockColor` / `ItemColor`.** Tinting is data-driven: a `BlockTintSource` or `ItemTintSource` declared in the model JSON. Nothing to register from code — see `CCColorHandlers`, now a stub recording which two assets need entries.
- **`BakedModel`, `MultiBufferSource`, `GuiGraphics`.** Replaced by `QuadCollection` / `BlockStateModel`, `SubmitNodeCollector`, and `GuiGraphicsExtractor` respectively.
- **`Item.onItemUseFirst`** — NeoForge's; see *Traps*.
- **`CommonHooks.onNoteChange`** — NeoForge's veto hook for note block changes. Contraption note blocks just cycle the note now.
- **`LevelRenderer.notifyNearbyEntities`** — moved to `LevelEventHandler` and private there. It is a three-block sweep calling `LivingEntity.setRecordPlayingNearby`, inlined in `ContraptionMusicManager`. This is what makes parrots dance.
- **`GuiGraphics.drawString`** → `GuiGraphicsExtractor.text(font, text, x, y, argb, shadow)`. Note the colours are **full ARGB** — `0xFFFFEE` becomes `0xFFFFFFEE`, and dropping the alpha byte draws nothing.
- **`ValueOutput` has no raw tag put** and `ValueInput` no raw list get. Anything still shaped as a `ListTag` goes through `CompoundTag.CODEC.listOf()`.

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
| Lighter-than-air fluids | Gases pool at the bottom of a vessel instead of floating to the top | Create Fly has no fluid-type API; it stubs the same branch out |
| Multiblock placement sound | Placing a vessel or silo plays one metal step per block, not one per structure | `getSoundType(state, level, pos, entity)` is gone from vanilla |
| Feature toggle UI | No in-game config screen; toggles are edited by file | Create Fly has no `catnip.config.ui` |
| Fan washing catalyst tint | Renders grey instead of water-coloured | `CCColorHandlers` (stub) — needs tint entries in two JSONs |
| Creative tab ordering | The tab no longer sits after Create's palettes tab | `withTabsBefore` was removed from the builder |
| Copycats+ migration | Copycat blocks never convert to their Copycats+ equivalents | `CopycatsManager` excluded; the gated branches were collapsed to their fallbacks |
| Crank wheel handle renderer | Without Flywheel (or with it off) the crank wheel draws no handle | `HandCrankRenderer` no longer asks the block entity for its model — it needs its own renderer. Flywheel visuals do draw it |
| Copycat block entity id | Connected's copycat blocks now save as `create:copycat` | They joined Create's block entity type rather than registering a second one — see *Traps*. No 26.2 world predates this, but it is a one-way change |
| Pick-block on a linked transmitter | Picks the module rather than the base when the crosshair target is unavailable | `getCloneItemStack` lost its `HitResult`; `LinkedTransmitterBlock.isHittingBase(state, level, pos)` reads the client target instead |
| Pick-block on an encased cross connector | Always gives the encased block, never the bare connector | Same removal, and here there is no sensible fallback |
| Contraption note blocks | Other mods can no longer veto or rewrite a note change | `CommonHooks.onNoteChange` has no Fabric equivalent |
| Inventory bridge neighbour updates | Notifies every side including the one that notified it, bounded by a re-entrancy guard | `neighborChanged` gets a null `Orientation`, so the source side is unknowable |

The first two are stubs with the full mapping recorded in their class docs — block, sprite shift, predicate, renderer, visual. They are ready to implement, not ready to guess at.

| Legacy contraption storage | Item silos riding a contraption saved in a 1.21.1 world do not come back | `MountedStorageManagerMixin`, excluded |

### Excluded from compilation

Integrations with mods that have no 26.2 release, excluded per-file in `build.gradle` rather than deleted — one line each to re-enable: **Copycats+, Additional Placements, Dye Depot, Simulated, JEI**. `FeatureRefreshEvent` goes with JEI: it exists only to tell JEI to refresh its list when a feature toggles.

Two mixins have no target at all: `ThrottleLeverBlockMixin` (aimed at Simulated, never at Create) and `SubMenuConfigScreenMixin` (Create Fly has no config UI). `ItemUseOverridesMixin` goes with `ItemUseOverrides`, which Create Fly removed outright. `MountedStorageManagerMixin` joins them: `readLegacy` was not renamed, Create Fly deleted legacy contraption storage reading entirely, so there is nowhere to inject and no data to migrate either way.

**Every one of these must also be absent from `create_connected.mixins.json`.** Nothing enforces that — see *The launch phase*.

341 JSONs of compat data for those mods were deleted, along with two recipes upstream had disabled with an always-false condition.

---

## The launch phase

Both predictions above turned out right, so this section records what the first launches cost and
how each class of failure was found. The method matters more than the list: **Mixin stops at the
first hard failure**, so launching repeatedly discovers one bug per run. Auditing statically found
seven in one pass.

### Audit the mixins against the jar rather than launching

For each mixin, resolve its `@Mixin` target from the file's own imports, `javap` that class, and
check every `method = "..."` name exists. Build the classpath as:

```bash
CF=$(ls ~/.gradle/caches/modules-2/files-2.1/maven.modrinth/create-fly/*/*/*.jar | grep -v sources | head -1)
MC=~/.gradle/caches/fabric-loom/26.2-rc-2
javap -p -cp "$CF:$MC/minecraft-common.jar:$MC/minecraft-client.jar:$MC/minecraft-client-only.jar" <class>
```

Skip anything carrying `require = 0` / `expect = 0`, and anything whose target class is absent
because the mod is not installed — both are fine and will otherwise drown the signal. Checking the
*name* is not enough on its own: a stale descriptor fails exactly like a stale name, and
`@Accessor` / `@Invoker` declare no `method =` at all, so they need checking separately from their
own method names.

Seven were broken. `onRemove` → `affectNeighborsAfterRemoval` accounted for three of them.

### A mixin listed in the config but excluded from compilation kills the game

`mixin/compat/**` is excluded in `build.gradle`, but two of its entries were still named in
`create_connected.mixins.json`. The config is `"required": true`, so Mixin fails to load the class
and takes the game with it. The build has no opinion about this at all — the two lists are only
kept in agreement by hand. If you exclude a mixin, remove it from the config in the same change.

### The mass rename never touched annotation strings

Two separate silent failures, both from the `com.simibubi.create` → `com.zurrtum.create` sweep
having only rewritten imports and code:

- `@At(value = "INVOKE", target = "Lcom/simibubi/create/...")` still naming the NeoForge package.
  With `remap = false` these match nothing, and an injection that matches nothing inside an
  otherwise-valid mixin is simply inert. `SteamEngineBlockMixin`'s `onPlace` was in this state and
  would have surfaced only after its sibling `onRemove` was fixed.
- Thirteen descriptors written `Lcom.zurrtum.create...;` **with dots**. A descriptor takes slashes.

Both are invisible to javac and to the launch log. Sweep for them directly:

```bash
grep -rnE 'L(com|net|org|java)\.[A-Za-z0-9_.$]+;' src/main/java/.../mixin/   # dotted descriptors
grep -rn "Lcom/simibubi" src/main/java/.../mixin/                            # stale package
```

### `Item.Properties` needs its registry key before the Item is constructed

26.2 resolves an item's description id inside `Item`'s constructor, so a bare `Properties` throws
`NullPointerException: Item id not set`. The shim now calls `setId(ResourceKey<Item>)` first.

Block items additionally need `useBlockDescriptionPrefix()` — that is what makes the id read
`block.<ns>.<name>`, which is the shape all 172 block keys in the committed lang files use. Without
it there is no error, just every block showing an untranslated name.

Vanilla does both in `Items.registerBlock`, but every overload of it is private and access wideners
are not transitive, so the shim does it by hand — the same reason it avoids the `BlockItemId`
overload of `Blocks.register`.

### Eager registration exposes static-init cycles Registrate hid

Registrate deferred block construction until after `CCBlocks` had finished initialising. The shim
registers eagerly, so a block's `<clinit>` now runs *inside* `CCBlocks.<clinit>`, at the moment the
very field being assigned is still null.

Three `PlacementHelper`s passed a **bound method reference** — `CCBlocks.SHEAR_PIN::has` and the two
copycat equivalents — to their super constructor. A bound reference evaluates its receiver
immediately, so each one NPE'd on the block it belonged to. A lambda defers the read to call time,
which is always after init.

This is worth knowing generally, not just for these three: **anything evaluated from a `static`
field of a block or item class now runs mid-registration.** Reads of `CCBlocks` / `CCItems` from
there have to be lazy. Registries initialised from `onInitialize` (`CCCreativeTabs` and friends) are
fine, because that runs after every `register()` call has returned.

---

## What is next

**Keep launching.** Each run gets further; the remaining failures are runtime-only and the build
says nothing about them.

1. **The table below.** *What is missing on purpose* is the test script for the first run that
   reaches a world.
2. **Inert injections.** A mixin can apply and still do nothing if its inner `@At` target moved.
   The audit above covers the outer method; the inner targets were checked for class resolution but
   not for the member within it.
3. **The sequencer GUI.** Create Fly stripped every field off `SequencerInstructions` and moved the
   display properties to `client...SequencedGearshiftScreen`. The enum side is ported — the three
   added instructions (`TURN_AWAIT`, `TURN_TIME`, `LOOP`) carry name and ordinal only — but the
   screen has **not** been taught about them. Expect the sequenced gearshift UI to be wrong for
   those three. This is the one known-incomplete piece rather than a deliberate omission.

Then the visible gaps, roughly in order of how much they cost: the block entity renderers and Flywheel visuals (`client/CCBlockEntityRenders`), connected textures (`client/CCConnectedTextures`), and server→client config sync (`config/CCommon`). All three are stubs whose class docs record the exact mapping needed.

### Block entity renderers — done, and the shape to follow

**This is a redesign, not a sweep.** 26.2 split block entity rendering into two phases, and `renderSafe(be, partialTicks, poseStack, bufferSource, light, overlay)` no longer exists in any form:

| Phase | Method | Runs |
|---|---|---|
| extract | `extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay)` | reads the block entity, fills a state object |
| submit | `submit(state, poseStack, SubmitNodeCollector, cameraRenderState)` | queues draw calls, never touches the block entity |

So a renderer implements `BlockEntityRenderer<BE, S>` and **needs its own `BlockEntityRenderState` subclass** carrying everything the submit phase reads — the old code's local variables become fields. `Create Fly`'s `SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay)` and `getCardinalLighting(level)` do the common part.

`SuperByteBuffer` gains an `extractRenderState()` that yields a `SuperByteBufferRenderState`, and rotations are precomputed into `Quaternionf` fields rather than applied inline.

Read `client/content/kinetics/gearbox/GearboxRenderer` in Create Fly first: it is the closest analogue and shows the whole shape end to end.

**Done**, all following that shape: the three gearboxes, the kinetic battery, the kinetic bridge, the linked analog lever, the fluid vessel.

Points that only showed up while doing it:

- Rotation is baked into the buffer during extraction with `rotateCentered(radians, direction)`, so submit usually has nothing left to do. Only pull rotations out into `Quaternionf` state fields when submit has to interleave them, as `GearboxRenderer` does.
- `SmartBlockEntityRenderer.extractBase(be, state, crumbling)` returns the `Level` and fills the base fields; `getCardinalLighting(level)` and `getLightCoords(level, pos)` cover the rest. `LevelRenderer.getLightColor` is reached through the latter.
- `RenderType.cutoutMipped` was a chunk layer and has no block entity equivalent — those became `ChunkSectionLayer`. Use the typeless `submit(matrices, queue)`, which takes the buffer's own material; Create Fly does this for all of its own.
- Renderers that add Create behaviour overlays subclass the parent's render state and extract `FilteringRenderer.getFilterRenderState` / `LinkRenderer.getLinkRenderState` into it — see `LinkedAnalogLeverRenderer`.
- `Direction.getNearest` lost its three-int overload; `getApproximateNearest` takes a delta.

**The two entity-model renderers are across too** — `FanCatalystRotatingHeadRenderer` and `DashboardRenderer` draw models rather than `SuperByteBuffer`s, which was a separate sub-problem:

- `SkullBlockRenderer.createModel(EntityModelSet, SkullBlock.Type)` is public and replaces the reflective model construction in `SkullTypes`, which takes `SkullModel` and `DragonHeadModel` with it — neither exists under those names now. `context.getModelSet()` is `context.entityModelSet()`.
- `SkullBlockRenderer.getSkullRenderType(type, identifier)` is public and replaces reaching into the private `SKIN_BY_TYPE`. A null identifier falls back to that map, which is what the old code did.
- **`submitSkull` argument order, previously unresolved and now settled by decompiling `SkullBlockRenderer.submit`:** `submitSkull(animationProgress, poseStack, collector, lightCoords, model, renderType, outlineColor, crumbling)`. The first int is light, the second is the outline colour, and vanilla always passes `0` for it. Guessing would have compiled and rendered wrong in silence.
- The dashboard draws text, so it follows vanilla's `AbstractSignRenderer` instead: `Font.drawInBatch` took a `MultiBufferSource`, so text goes through `SubmitNodeCollector.submitText`. `SignRenderer.getDarkColor` moved to `AbstractSignRenderer`.

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

Note Create Fly's own warning on `CopycatModel`: if FRAPI is loaded, `FabricBlockStateModel#emitQuads` has to be overridden for ambient occlusion and emissive flags to survive.

### Scroll values and value boxes — done

Create Fly split `ScrollValueBehaviour` in two, and this is worth knowing before touching any other Create behaviour:

- **Server half** (`ServerScrollValueBehaviour`, in `foundation/`): the value, its range, `getValueSettings` / `setValueSettings`, and `getClipboardKey` — `ValueSettingsHandleBehaviour` extends `ClipboardCloneable`, so clipboard support rides along on this side. The block entity adds it in `addBehaviours`.
- **Client half** (`ScrollValueBehaviour`, in `client/`): the value box and its board. **Not** added by the block entity — it goes in a type-keyed client registry. Connected's `CCBlockEntityBehaviours` mirrors Create Fly's `AllBlockEntityBehaviours`.

Upstream's three behaviours each overrode both halves, so each became a pair. Upstream also seeded defaults by assigning the `value` field directly; it is protected on the parent and `setValue` clamps against a range that is not set yet, so the server halves carry an explicit `startingValue`.

**`LinkBehaviour` is split the same way** — `ServerLinkBehaviour` carries the transmission and frequency, the client `LinkBehaviour` carries the value box slots. So are **`ScrollOptionBehaviour`** (`ServerScrollOptionBehaviour` holds the value; the client half is abstract, and Create Fly's own `RotationDirectionScrollBehaviour` was directly reusable for the battery and the freewheel clutch) and **`FilteringBehaviour`** / **`SidedFilteringBehaviour`**.

Assume any Create behaviour with both a value and a widget is a pair now, and check which half you actually want before importing: the two classes share a simple name, so picking the wrong one surfaces as *missing methods*, not a missing class.

`ValueBoxTransform.rotate` / `shouldRender` / `getLocalOffset` all dropped their level and position — the block state alone now.

### The missing-classes list, as it ended up

Create classes with no Create Fly equivalent. **Six of the original list turned out not to need reimplementing**, which is worth checking for before writing a replacement:

- **`BakedQuadHelper`** — `BakedModelHelper.cropAndMove` now takes a whole quad, leaving it with no callers.
- **`SmartFluidTank`** — it existed to take a change callback; Create Fly subclasses `FluidTank` and overrides `markDirty` instead. Same for `CreativeSmartFluidTank`, which is `CreativeFluidTankBlockEntity.CreativeFluidTankInventory`.
- **`BlockEntityConfigurationPacket`** — it carried the permission, distance and load checks; Create Fly folded those into one helper, and with a single such packet here they inline.
- **`ClipboardOverrides`** — `ClipboardType` is a top-level type in `infrastructure.component`.
- **`CreateBuiltInRegistries`** — `api.registry.CreateRegistries`.
- **`EncasedCogRenderer`** — the import was left over from the stripped renderer registration; nothing used it.

Still genuinely absent, and none of them currently referenced: `SafeBlockEntityRenderer`, `ItemUseOverrides`, `ItemStackHandlerAccessor`, `VersionedInventoryWrapper`, `ReducedDestroyEffects`, `ICapabilityProvider`, `ChuteGenerator`, `ChainDriveGenerator`.

Capability migrations are done — the pattern is under *Architecture decisions* if another one comes up.

---

## Standing risk

Create Fly is at `26.2-rc-2`. It is a release candidate and its API moves. Some of this will need redoing — and the port now leans on three things that are more fragile than an ordinary API call, so check these first after any bump:

- The **three type-redirect overrides** on `LinkedAnalogLeverBlockEntity` and `ShearPinBlockEntity`. If vanilla adds a fourth reader of `BlockEntity.type`, they break silently.
- The **two placement mixins**, which inject at the same target as Create Fly's own and depend on that target existing.
- **`SkullBlockRenderer.submitSkull`'s argument order**, established by decompiling rather than by any published signature.
