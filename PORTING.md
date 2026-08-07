# Porting notes

Working document for the port of **Create: Connected** from NeoForge 1.21.1 to **Fabric / Minecraft 26.2**, targeting [Create Fly](https://github.com/ZurrTum/Create-Fly).

Written to be read cold. If you are picking this up with no context, read *State* and *Traps* first.

---

## State

**Released.** Published on Modrinth as a beta on 2026-08-07, from 7,162 compile errors five days
earlier. Renderers and Flywheel visuals registered, connected textures wired, block tints
registered, kinetics propagating. Verified in game: item silo, fluid vessel, copycats, shear pin
through a cross connector, linked transmitters. The dedicated server loads all 44 mods, applies its
mixins in `Env=SERVER` and reaches `Done` with no `create_connected` errors.

What is left is genuinely small, and all of it is listed in *What is missing on purpose*. One `TODO`
remains in the whole mod: `config/CCommon.java:21`, the server→client config sync, which only
matters in multiplayer.

**Two habits this port was repeatedly punished for not having.** They are worth more than any
individual fix below:

1. **A class that compiles is not a class that runs.** Five separate bugs this session were code
   that had been written, compiled, and never called — Registrate used to chain the registration
   onto the block, the port had to strip those calls because they name client classes, and stripping
   them leaves no compiler error behind. If something "doesn't work but doesn't error", check it is
   registered before you read it:
   ```bash
   grep -rn "ClassName" --include=*.java src/ | grep -v "/ClassName.java:"
   ```
2. **Verify an API against the jar before writing against it.** Two crashes came from registering a
   Create Fly class without checking which block state properties it reads. `javap` and `grep` are
   both cheaper than a crash report.

A warning about this document, earned the hard way: several claims in it turned out to be reasoned
from what the code looked like rather than from what it did, and every one was wrong. Where an entry
records a *consequence* without recording how it was observed, treat it as a hypothesis and audit it
before acting on it. The corrected entries say what was actually measured.

*The launch phase* records what the first runs cost and — more usefully — how each class of failure
was found, because the finding technique transfers and the individual bugs do not.

Every number in this document was produced by running the tool, not estimated.

| | |
|---|---|
| Repository | https://github.com/GravisLudio/create-connected-fly |
| Modrinth | https://modrinth.com/mod/create-connected-fly-port |
| Local path | `C:\Users\GravisLudio\dev\create-connected-fly` |
| Upstream remote | `upstream` → `hlysine/create_connected` |
| Branch | `main` |
| Reference clones | `C:\Users\GravisLudio\dev\_reference\{Create-Fly, create-connected-fabric}` |
| Licence | AGPL-3.0 **plus additional terms** at the end of `LICENSE` — distinct name and icon, link back to the original, handle your own issues. All met; re-check after any rename or icon change. |

### Testing, and the half that was nearly missed

Everything renders through one of two paths and they are not both exercised by playing normally:

- **Flywheel on** (default) — the visual runs and the block entity renderer is skipped entirely,
  because `skipVanillaRender` is true for most registrations.
- **Flywheel off, or Sodium installed** — the block entity renderer runs instead.

Four days of testing happened on the first path only, and the encased chain cogwheel crash arrived
from a modpack running the second. **Force the Flywheel backend off in Create Fly's config and walk
the blocks again** — that is the only way the 24 registered renderers actually execute.

Same for sides: `runServer` is a thirty-second test that no amount of singleplayer covers.

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
@Override public BlockEntityType<?> getType()                  { return CCBlockEntityTypes.SHEAR_PIN.get(); }
@Override public Holder<BlockEntityType<?>> typeHolder()       { return getType().builtInRegistryHolder(); }
@Override public boolean isValidBlockState(BlockState state)   { return getType().isValid(state); }
```

Overriding only `getType` looks right and is not: `typeHolder` is what writes the saved id, so the block entity would come back from disk as Create's plain one. `isValidBlockState` is checked against the type's valid-blocks set, which will not contain our block. Neither failure appears at build time. `LinkedAnalogLeverBlockEntity` and `ShearPinBlockEntity` do all three.

**The type must come from the registry entry, not from a field.** The obvious version — take the type as a constructor argument, store it, return the field from all three — compiles and then crashes on the first block placement with `NullPointerException: ... because "this.type" is null`. `BlockEntity`'s own constructor calls `validateBlockState`, which calls the `isValidBlockState` override *while `super()` is still running*, before any field declared in the subclass has been assigned. The three-argument constructor stays, because `BlockEntityBuilder`'s factory demands that shape, but its `type` argument is deliberately ignored.

That is the general hazard, not a quirk of these two classes: **anything reachable from a Create Fly superclass constructor sees a subclass whose fields are all still default.** `SmartBlockEntity`'s constructor also calls `addBehaviours`, so a behaviour that reads a constructor-assigned field has the same problem, and a field with a declaration-site initialiser assigned inside `addBehaviours` gets silently overwritten a moment later. Both were audited across the mod on 2026-08-06 and only the two type fields were affected.

Two cases did not need it, and are worth copying instead:

- **The subclass adds nothing structural.** The inverted clutch and gearshift are only a `getRotationSpeedModifier` override, so they extend `SplitShaftBlockEntity` — the shared parent, which still takes a type.
- **We do not need our own type at all.** The copycats join *Create's* type through Fabric's `((FabricBlockEntityType) type).addValidBlock(block)`, from `fabric-object-builder-api-v1`. Their block entities now save as `create:copycat`.

### Block tints: the API survived, the event did not

`CCColorHandlers` was a stub whose doc claimed 26.2 had made tinting purely data-driven with nothing
left to register from code. That was inferred, and it was wrong.
`BlockColors.register(List<BlockTintSource>, Block...)` is still there, and
`BlockTintSources.water()` is the old `BiomeColors.getAverageWaterColor` under a new name. What
actually went away is NeoForge's `RegisterColorHandlersEvent` — Fabric has no hook into
`BlockColors.createDefault()`, so registration goes through a mixin, which is how Create Fly reaches
its own `AllBlockTints`. Inject at `RETURN` rather than `TAIL` and the return value is populated, so
no MixinExtras `@Local` is needed to get at the instance being built.

Two things were missing, and the second is the one worth remembering:

- **`BlockTintSources.water()` on the fan washing catalyst.** Its water rendered white. Needs
  `tintindex: 0` on the faces it should colour, which the model carries.
- **`AllBlockTints.WrappedBlockColor` on all nine copycats.** A copycat draws the material's quads
  but not the material's *tint*, so every biome-tinted material came out with its raw texture — grass
  is grey in the file and gets its green entirely from the tint. `WrappedBlockColor` forwards the
  lookup to whatever block is being copied. Create Fly registers three of them, one per tint index,
  for `COPYCAT_STEP` and `COPYCAT_PANEL`; Connected's nine were simply never added to that list.

The item side is still open and is genuinely data-driven: an `ItemTintSource` in
`assets/create_connected/items/<id>.json`, with no code-side registration at all. Until it is
written the washing catalyst's water is still white **in the inventory**, correct in the world.

### A no-op builder transformer cost three wrong hypotheses in the model layer

**Copycats rendered see-through, with or without a material applied, and light leaked through them.**
The cause was `CCBuilderTransformers.copycat()` returning its builder untouched. Create's version
sets the block's base properties, and the one that mattered is `noOcclusion()`. Without it a copycat
claims to be a solid full cube: its neighbours cull the faces touching it, while its own model is a
slab, a wall or a fence. What you see through the holes is the neighbours' missing faces.

**The symptom was in rendering; the bug was in registration.** Three hypotheses were tried in the
model layer first and all three are wrong — recorded so nobody pays for them twice:

1. *The port bucketed quads by the wrong face.* `CCCopycatModel` puts quads assembled from source
   face `D` into `D`'s culled bucket, which would delete a wall's interior pole when a solid
   neighbour sits to the north. Plausible, and it even matched the "wall looks like several
   overlapping" symptom. Emitting everything unculled instead **changed nothing**. Reverted.
2. *`assemblePiece` was mistranslated.* It is not — a line-by-line diff against upstream shows the
   same cull mask, the same crop, the same offset. Only `BakedQuadHelper.cloneWithCustomGeometry` →
   `BakedModelHelper.cropAndMove` differs, which is the documented API change.
3. *The blockstate points at the wrong model.* It points at `minecraft:block/air` — and so does
   upstream's. The copycat model is generated entirely in code; the blockstate is a placeholder.

Two process lessons, both expensive:

- **A transformer that returns its argument unchanged is a stub, and it does not look like one.**
  Grep for them: `grep -n "return builder;\|return b;" registries/CCBuilderTransformers.java`. This
  one even carried a comment confidently explaining why it had nothing to do.
- **When a rendering symptom does not reproduce in Create Fly alone, the difference is in this mod —
  and it need not be in a renderer.** Registration is the other half. The instinct to keep digging
  in the model layer, three times, was the wrong instinct.

Create Fly's property set for its own copycats, which this now mirrors:

```java
Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().mapColor(MapColor.NONE)
    .isValidSpawn(Blocks::never).emissiveRendering(CopycatPanelBlock::hasEmissiveLighting)
```

`SharedProperties.softMetal()` is already `GOLD_BLOCK`, so the base copy matches. Per-shape extras
compose after the transform — two call sites add `forceSolidOn()`, exactly as Create Fly does for
`COPYCAT_STEP`.

**`emissiveRendering` is deliberately left off, and there is a real bug behind that.** Adding it made
every copycat glow in the dark. The predicate reads `CopycatBlock.EMISSIVE`, and nothing in this mod
ever sets it: `BooleanProperty.create` lists `true` before `false`, `StateDefinition` takes the first
value of each property for the default state, and `CopycatBlock`'s constructor does not override it —
so the property defaults to `true` on all nine blocks. Their own `registerDefaultState` calls chain
off `defaultBlockState()` and preserve it.

Fixing it properly means adding `.setValue(CopycatBlock.EMISSIVE, false)` to `registerDefaultState`
in all nine block classes; then the predicate can go back on. Leaving it off costs only the feature —
a copycat of glowstone will not glow — and that never worked here anyway, because before this nothing
read the property. Note the signature also narrowed to `Predicate<BlockState>` in 26.2; it took
`(state, level, pos)` before.

The lesson that generalises: **before fixing a rendering oddity in a block this mod inherits from
Create, reproduce it on Create Fly alone.** It costs one launch and it has now twice pointed the
work somewhere other than where the symptom appeared.

### Registrate registered things for you; porting the class is only half of it

Three times now the same shape: a class was ported, compiled, and never called, and the symptom was
silence rather than an error.

| What was written | What was never called | Symptom |
|---|---|---|
| Every block entity renderer and Flywheel visual | `CCBlockEntityRenders.register()` | Nothing turned |
| All nine copycat models, plus the fluid vessel's | `CCModels.register()` | Copycats **invisible**, in world and in inventory |
| `ItemSiloCTBehaviour`, `FluidVesselCTBehaviour` | `CCConnectedTextures.register()` | A stack of silos read as separate blocks instead of one vessel |
| The tint sources | nothing at all — the class was a stub | Catalyst water white, copycat grass grey |
| The client `LinkBehaviour` | `CCBlockEntityBehaviours.register()` | Linked levers and buttons had no frequency slots — no way to tune them, so they read as dead blocks |

Five for five. Every one compiled, and every one was silent.

The last is the sharpest illustration of the split this port keeps running into: a behaviour with a
value **and** a widget is two classes now. `ServerLinkBehaviour` is added by the block entity in
`addBehaviours` and carries the frequency and the transmitting; the client `LinkBehaviour` carries
the value box slots and goes in the type-keyed client registry. Register one and the block half
works, invisibly. **Any Create behaviour with both a value and a widget needs both halves —
`ScrollValueBehaviour`, `LinkBehaviour`, `ScrollOptionBehaviour`, `FilteringBehaviour`.**

The cause is structural, not carelessness. Registrate chained these onto block registration —
`.onRegister(CreateRegistrate.blockModel(() -> CopycatSlabModel::new))` — and the port had to strip
those calls because they name client classes from a class that runs on both sides. Stripping them
left no compiler error behind, so nothing pointed at the hole.

The copycat one is the nastiest to diagnose from in-game symptoms: **a model that bakes to nothing
is not a missing model.** No warning is logged, because the file resolved fine — it simply had no
material and therefore no quads. Grepping the log finds nothing at all.

So: **if a class in this port is referenced only by its own file, suspect it is dead.**

```bash
grep -rn "ClassName" --include=*.java src/ | grep -v "/ClassName.java:"
```

Create Fly's side is `AllModels.ALL`, a public `Map<Block, BiFunction<BlockState, UnbakedRoot,
UnbakedRoot>>` that its `BlockStateModelLoaderMixin` consults while baking, so registering into it
from the client entrypoint is all that is required. The authoritative list of what to register is
upstream's, not memory:

```bash
git show b5e21592:src/main/java/com/hlysine/create_connected/registries/CCBlocks.java | grep -B6 blockModel
```

That turns up twelve bindings, and only nine of them are copycats — the fluid vessel binds two
variants through static factories rather than a constructor, and the shear pin borrows Create's own
`BracketedKineticBlockModel`.

### Borrowing a Create Fly renderer means inheriting the block state it reads

This one bit twice, in the same session, and both times the class compiled and crashed at runtime:

| Registered | Reads | On a block that is |
|---|---|---|
| `LinkBehaviour` (its default `RedstoneLinkFrequencySlot`) | a six-valued `FACING` | a lever |
| `EncasedSmallCogRenderer` | `EncasedCogwheelBlock.TOP_SHAFT`, `BOTTOM_SHAFT` | a `ChainDriveBlock` |

Create Fly's renderers and behaviours are written against Create's *own* blocks and reach straight
into their properties. `StateHolder.getValue` throws `IllegalArgumentException` on a property the
block does not have — not a warning, not a fallback. Nothing catches it, so it takes the client down
on the first frame the block is visible.

Both were invisible in the dev instance, for different reasons: the frequency slot needs the block
in view, and the cog renderer only runs when Flywheel is off — with a visual registered and
`skipVanillaRender` true, the BER never fires. The chain cogwheel crash arrived from a modpack with
Sodium, which extracts block entities down its own path. **Testing with Flywheel on tests only half
of what you registered.**

The check is one grep per borrowed class, and it is cheap:

```bash
grep -oE "getValue\([A-Za-z]+\.[A-Z_]+\)" <TheCreateFlyClass>.java | sort -u
```

Then confirm our block extends the class those properties belong to. Run over everything currently
borrowed, only the two above failed: `BrassChuteBlock extends ChuteBlock` and
`LinkedAnalogLeverBlock extends AnalogLeverBlock`, so `ChuteRenderer` and `AnalogLeverVisual` are
safe by inheritance, and `SplitShaftRenderer`, `SplitShaftVisual`,
`BracketedKineticBlockEntityRenderer`, `SmartBlockEntityRenderer` and `BracketedKineticBlockModel`
name no properties at all.

When a borrowed class does not fit, copy the branch that applies and drop the query. For the chain
cogwheel only the shaftless branch was ever reachable, so `ChainCogwheelRenderer` is that branch
alone — which is also exactly what `EncasedCogVisual.small` draws on the Flywheel path.

### A block drawn by its renderer needs a particle-only blockstate model, and `Models.chunkPartial`

Symptom: the crank wheels rendered as two wheels Z-fighting, one static and one turning. It appeared
the moment the renderers were registered, and nothing in the port had changed — upstream's
blockstate, partial models and visual are byte-for-byte what we carried over.

What changed is 1.21.1 → 26.2. **`RenderShape.ENTITYBLOCK_ANIMATED` no longer exists**; the enum is
down to `INVISIBLE` and `MODEL`. That constant was how a block said "do not bake me into the chunk
mesh, my renderer draws me". Create Fly replaced it with a convention instead: nine of its blocks —
`hand_crank`, `flywheel`, `belt`, `crushing_wheel`, the doors — point their blockstate at a
**particle-only model**, a file whose entire content is one `particle` texture:

```json
{ "textures": { "particle": "create:block/axis" } }
```

Connected's crank wheel is a subclass of Create's hand crank and its visual draws the same partial
model the blockstate was baking, so the geometry landed twice. Fixed by mirroring `hand_crank`
exactly: `crank_wheel/particle.json` and `large_crank_wheel/particle.json`, both blockstates
repointed, and a `CrankWheelRenderer` for the no-Flywheel path.

**That renderer is not optional.** Once the blockstate is particle-only, nothing else draws the
block when Flywheel is off. Create Fly's `HandCrankRenderer` hard-codes Create's own partial models
— it will not draw a Connected block — so reusing it leaves the wheel invisible or wearing Create's
hand crank. Any block given a particle-only model needs its own renderer in the same change.

**Second, related: `Models.partial` versus `Models.chunkPartial`.** Flywheel gained the split in
this version, so every visual carried over from upstream uses `partial` — all nine of ours did.
Create Fly's own split is by context, 128 `chunkPartial` against 30 `partial`:

| Use | Which |
|---|---|
| A visual for a block sitting in the world | `chunkPartial` — applies world-space normal darkening |
| A contraption actor (`*ActorVisual`, `StabilizedBearingVisual`) | `partial` — there is no chunk to light against |

All nine were moved to `chunkPartial`. Wrong here is not a crash or a missing model; it is lighting
that looks subtly flat, which is exactly the kind of thing that gets blamed on a shader pack.

### `detachKinetics` runs after the block is already air

The cross connector forwarded rotation when a source was **attached** and not when one was
**removed**: everything past the connector kept spinning at its old speed, and the goggles agreed —
16 RPM with nothing driving it. So the block entity was genuinely stale, server side, not just its
visual.

`RotationPropagatorMixin.forwardConnection` walks the chain by asking each block to forward the
connection, and hands `CrossConnectorBlock.forwardConnection` the state of the block the hop starts
from. It read that state out of the world. But the mixin sits on
`RotationPropagator.getPotentialNeighbourLocations`, and one of that method's callers is
`handleRemoved` ← `KineticBlockEntity.detachKinetics` ← the block entity's `remove()` — which the
chunk calls **after** it has already replaced the block with air. `forwardConnection` opens with an
`instanceof IRotate` test, air fails it, forwarding stops at the connector, and nothing beyond it is
ever told its source is gone.

Fix: for the first hop use `be.getBlockState()`, the block entity's cached state, which is still the
real one. `CrossConnectorBlock.updateConnections` already guarded this way — upstream knew the
hazard in the block and missed it in the mixin.

**The general shape is worth carrying:** any code reached from a block entity's removal path must
not read that block entity's own block out of the world. It is gone. This bites harder in 26.2
because `onRemove(state, level, pos, newState, isMoving)` — which handed you the outgoing state —
became `affectNeighborsAfterRemoval(state, level, pos, movedByPiston)`, and the name is the warning.

**A second bug lived in the same helper: detaching without re-attaching.** Placing or breaking a
cross connector stopped whatever sat past it, permanently — and breaking that block and putting it
back started it again, which is a workaround that makes a logic bug look like a rendering one.

`KineticHelper.updateKineticBlock` calls `detachKinetics()` and `removeSource()`, leaving the block
entity with no source and no reason to look for one. `KineticBlockEntity.tick` only calls
`attachKinetics()` when `needsSpeedUpdate()` is true, and `updateSpeed` is otherwise set in exactly
one place: the constructor. So replacing the block was the *only* thing that could revive it.

Upstream never had to think about it because `markAndNotifyBlock(pos, chunk, state, state, 3, 512)`
carried a flag-1 neighbour update and a 512-deep shape-update recursion, and the two-call replacement
(`sendBlockUpdated` + `updateNeighborsAt`) reproduces neither. Setting `kineticTE.updateSpeed = true`
is the direct expression of what was lost. When a vanilla helper is replaced by "the parts of it we
appeared to need", write down which parts were dropped — this is the second bug to come out of that
one substitution.

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
- **Recipe ingredients are bare strings.** `{"item": "create:shaft"}` is `"create:shaft"`, and `{"tag": "c:ingots/zinc"}` is `"#c:ingots/zinc"`. The object form is gone from every ingredient position — `ingredients`, `ingredient`, and a shaped recipe's `key` values. `result` is unaffected and keeps `{"count": n, "id": ...}`
- **Create's processing recipes went singular**, which corrects an earlier note here claiming fluids move to a `fluid_ingredients` array. Read off Create Fly's own `data/create/recipe/<type>/`:

| type | ingredient side | result side |
|---|---|---|
| `create:cutting`, `create:pressing` | `ingredient` | `results` (list) |
| `create:item_application`, `create:deploying` | `target` **and** `ingredient` — the old `ingredients` list was `[target, applied]` | `results` (list) |
| `create:filling` | `ingredient` and `fluid_ingredient` (one object) | `result` (single) |

- **`create:sequenced_assembly` changed shape, not names.** `results` was a weight table whose first entry was the real product; it splits into `result` — carrying a *probability* — and `junks`, keeping the weights. Divide the old first weight by the total to get it: control chip's `120` of `150` is `0.8`, exactly as Create's own precision mechanism converts. Sequence steps use the placeholders `"$ingredient"` and `"$result"`

---

## What is missing on purpose

**None of these break the build. All of them need checking in game.** This list is the test script for the first successful launch.

| What | Consequence | Where |
|---|---|---|
| ~~Block entity renderers and Flywheel visuals~~ | **Done.** All 24 are registered — see *Block entity renderers* below | `client/CCBlockEntityRenders` |
| ~~Connected textures~~ | **Done.** Both halves — the CT model and the casing connectivity | `client/CCConnectedTextures` |
| Server→client config sync | Feature toggles can disagree between sides | `config/CCommon` |
| Item-use priority | Right-clicking a linked transmitter holding a placeable item may place it | `registries/PreciseItemUseOverrides` |
| Battery charge level | Kinetic battery renders empty at every charge | `assets/.../items/kinetic_battery.json` |
| Config reload hook | Toggling a feature does not refresh item visibility until restart | `config/CFeatures` |
| Lighter-than-air fluids | Gases pool at the bottom of a vessel instead of floating to the top | Create Fly has no fluid-type API; it stubs the same branch out |
| Multiblock placement sound | Placing a vessel or silo plays one metal step per block, not one per structure | `getSoundType(state, level, pos, entity)` is gone from vanilla |
| Feature toggle UI | No in-game config screen; toggles are edited by file | Create Fly has no `catnip.config.ui` |
| ~~Fan washing catalyst tint~~ | **Done.** See *Block tints* below — the same change also fixed biome tinting on the copycats | `client/CCBlockTints` |
| Creative tab ordering | The tab no longer sits after Create's palettes tab | `withTabsBefore` was removed from the builder |
| Copycats+ migration | Copycat blocks never convert to their Copycats+ equivalents | `CopycatsManager` excluded; the gated branches were collapsed to their fallbacks |
| ~~Crank wheel handle renderer~~ | **Done.** `CrankWheelRenderer` covers the no-Flywheel path | `content/crankwheel/CrankWheelRenderer` |
| **Never launched a dedicated server** | Unknown. `run/` has no server files at all | Several block entities import client-only classes — `CrankWheelBlockEntity` pulls in `CachedBuffers` and `SuperByteBuffer` — with no `@Environment(EnvType.CLIENT)` guard |
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

341 JSONs of compat data for those mods were deleted, along with **three** recipes upstream had disabled with an always-false condition. The third, `item_application/withering_catalyst_from_empty`, was missed the first time: its `neoforge:false` had been rewritten to a `fabric:not` carrying no `value` rather than deleted, which is a parse error rather than a disabled recipe. Fabric has no direct spelling of "always false", so deleting is the honest translation.

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

The deepest instance crossed two classes: `CCBlocks` reached a block whose factory named a
`CCBlockEntityTypes` field, which triggered *that* class's initialiser, which read a `CCBlocks`
entry declared two hundred lines further down and still null. Java does not re-enter an initialiser
already running on the same thread — it hands back the partially-built class. The cycle had a narrow
waist worth looking for: `CCBlockEntityTypes` referenced `CCBlocks` thirty-seven times, but only two
blocks referenced back.

### Then the data layer, which fails quietly

Reaching a world turned up 108 recipe parse errors and 37 loot table errors. **Almost none of these
crash** — the file is logged and dropped, so the recipe simply is not in the game and nothing tells
you again. One did throw hard (`SequencedAssemblyRecipe`'s codec) and hung world creation, which is
the only reason the rest were found at all.

Read the target shape off a real file rather than inferring it: vanilla's from the 26.2 jar,
Create's from Create Fly's own `data/create/recipe/<type>/`. Every fix below came from doing that,
and one of them corrected an earlier claim in this document.

The formats are catalogued under *Data and asset formats*. The pattern worth carrying forward:

- **A whole class of file can be silently wrong.** 63 recipes died on ingredients still written as
  `{"item": ...}` objects. Nothing warns; they just do not exist.
- **A validator that is new in 26.2 rejects what used to pass.** Loot tables meaning "drops nothing"
  spelled it with an `air` item entry, which is now an error that voids the whole table. Create Fly
  ships no `pools` key at all instead — see its `blocks/clipboard.json`.
- **Deleting compat assets can leave the blocks behind.** Sixteen Dye Depot loot tables failed
  because their blocks do not exist; the sweep that removed 341 compat JSONs had missed 64 more.

`tools/migrate-recipes.js` does the mechanical part. It reports before it writes and only rewrites
what it actually transformed — walking the data tree also reaches loot tables and tags, which carry
a `type` and would otherwise be reformatted into a diff that says nothing.

---

## What is next

It is released, so the question is no longer "what is wrong" but "what is worth doing".

In rough order of value:

1. **Walk the blocks with the Flywheel backend forced off.** The 24 registered block entity
   renderers have barely run — see *Testing* above. This is where the next crash lives, and it costs
   ten minutes.
2. **The sequenced gearshift screen.** The one thing marked incomplete rather than deliberately
   omitted: Connected's three added instructions (`TURN_AWAIT`, `TURN_TIME`, `LOOP`) carry name and
   ordinal only, and the screen was never taught about them. There is also a `Shift.BY=2` mixin
   warning in the log pointing at the same place.
3. **Server→client config sync** (`config/CCommon.java:21`), the last `TODO` in the mod.
4. **Small, cheap parity items**: `.setValue(CopycatBlock.EMISSIVE, false)` in the nine copycat
   blocks so `emissiveRendering` can go back on; the `ItemTintSource` for the washing catalyst so
   its water is not white in the inventory; separate `item.json` files for the exploding and dragon
   head catalysts so their icons are not bare frames.
5. **Re-enable the compat integrations** as those mods reach 26.2 — one `exclude` line each in
   `build.gradle` for Copycats+, Additional Placements, Dye Depot, Simulated and JEI.

**If a 1.21.11 port comes up**, Create Fly already publishes for it —
`maven.modrinth:create-fly:1.21.11-6.0.9-5`, the same Create 6.0.9. That means starting from this
branch rather than from upstream: same `com.zurrtum.create` API, same renderers, same models, same
mixins, and only the vanilla deltas between 1.21.11 and 26.2 to fix. Measure it before promising
anything — branch, swap the dependency and `minecraft_version`, then
`.\gradlew.bat compileJava --continue` and read the error count. That number is the estimate.

### Known cosmetic gaps — both were resolved, and neither was what it looked like

- **`linked_pale_oak_button` had no model** — **done.** Pale oak is vanilla wood added *after*
  1.21.1, and the buttons are registered by iterating `BlockSetType.values()`, so 26.2 handed the
  mod a fourteenth button that the committed JSONs knew nothing about. Purely additive, and the
  only wood-specific content in the blockstate is two references to `minecraft:block/oak_button`.
  Added: blockstate, loot table, `create:safe_nbt` tag entry, `en_us` and `en_ud`. Take `en_ud`
  glyphs from entries already in the file rather than typing codepoints — `l` is `ן` (U+05DF, final
  nun) and `B` is `ᗺ` (U+15FA), and both are easy to get subtly wrong.
- **"Seven fan catalysts render untextured"** — **the diagnosis was wrong on every count.** What an
  audit actually found:
  - They are *not* registered unconditionally. All seven carry
    `FeatureToggle.addCondition(Mods.X::isLoaded)`, and `CCCreativeTabs` filters both the tab and
    the search list on `FeatureToggle.isEnabled`. They never appear in the creative tab.
  - Their models and textures were *not* deleted. Every blockstate resolves to a model that exists,
    and of 1,414 texture references in the mod's models, every `create_connected:` one resolves.
  - Five of them reference textures belonging to mods with no 26.2 release (`createnuclear`,
    `create_shimmer`, `create_dragons_plus`, `createnetherindustry`, `twilightforest`). That is
    inherent to gated compat content, not a porting gap. Nothing to fix.
  - **Two were a real bug, now fixed**: chocolate and honey coating pointed at
    `create:fluid/chocolate_still` / `honey_still`. **Create Fly moved fluid textures from
    `textures/fluid/` to `textures/block/`.** Of 833 `create:` texture references in the mod those
    two were the only broken ones — worth re-running that check after any Create Fly bump.

The audit that produced this is worth repeating rather than describing: parse every model's
`textures` node (parse the JSON — a regex for `"…": "create:…"` also matches `parent` and reports
nonsense), resolve each id against `src/{main,generated}/resources` and against the Create Fly jar's
`assets/<ns>/textures/<path>.png` entries, and group what is left by namespace. Missing entries in
another mod's namespace are gated content; missing entries in `create:` are the port's own bugs.

**Run the same audit over `parent` as well as `textures`, and check `create:` ids against the jar
rather than assuming a foreign namespace is fine.** Skipping that hid a live bug for a whole
session: `block/inverted_clutch/item` and `block/inverted_gearshift/item` inherited
`create:item/clutch` and `create:item/gearshift`, which Create Fly moved to `create:block/clutch/item`
and `create:block/gearshift/item`. Both items rendered as missing-texture cubes in the hand and the
hotbar. Of eight `create:` parent references in the mod, those were the only two broken.

The log had been saying so all along — `Missing block model: create:item/clutch` — which is the
better lesson: **grep `run/logs/latest.log` for `Missing`, `Unable to load` and `Couldn't parse`
before reasoning about a resource problem.** After these fixes the only such warnings left are the
eleven belonging to the five mod-gated catalysts above.

### Five models still carry NeoForge's composite loader

Found by the same audit and **not previously recorded**. These five block models are
`"loader": "neoforge:composite"` with named `children`, each child carrying its own `render_type`:

```
fan_splashing_catalyst  fan_purifying_catalyst  fan_sculking_catalyst
fan_exploding_catalyst  fan_ending_catalyst_dragon_head
```

Fabric has no such loader. Vanilla's model parser ignores the unknown `loader` and `children` keys,
finds no `elements`, and inherits `minecraft:block/block` — so these blocks build an **empty** model
rather than a wrong one. It raises no error and no missing-model warning, which is why it survived
this long.

`net.minecraft.client.renderer.block.model.CompositeBlockModel` exists in 26.2 but is **not** a
drop-in: its `Unbaked` is `(normal, custom, transformation)`, a pairing rather than a list of named
children with independent render types.

**Fixed by flattening, and the split is worth knowing.** All five are `frame` — which is always
`fan_catalyst/empty` — plus one extra child:

- **Splashing, purifying, sculking** became one model each: the frame's seven elements followed by
  the child's, under the frame's single `render_type`. Merge the `textures` maps carefully — the
  child's keys can collide with the frame's `0` and `1`, and a collision silently repaints the
  frame. Check `texture_size` before merging too; it is per-model, so a child on a different one
  cannot be merged at all without rescaling every UV. These three were all on the default 16.
  `fan_splashing_catalyst` keeps `tintindex: 0` on its six water faces so the water tint above has
  something to colour.
- **Exploding and the dragon head** became **frame only**. Their heads are drawn by
  `FanCatalystRotatingHeadRenderer`, so baking the head into the block model would draw it twice —
  the crank wheel bug again. `dragon_head` is also `texture_size: [256, 256]` against the frame's
  16, which rules out merging regardless. The cost is that their inventory icons show a bare frame,
  since block entity renderers do not run for items; `item.json` inherits `block.json` today, so
  giving them a proper icon means splitting the two.

### Still open from before

- **Inert injections.** A mixin can apply and still do nothing if its inner `@At` target moved. The
  audit under *The launch phase* covers the outer method; inner targets were checked for class
  resolution but not for the member within the class.
- **The sequencer GUI.** Create Fly stripped every field off `SequencerInstructions` and moved the
  display properties to `client...SequencedGearshiftScreen`. The enum side is ported — the three
  added instructions (`TURN_AWAIT`, `TURN_TIME`, `LOOP`) carry name and ordinal only — but the
  screen has **not** been taught about them, and its `updateParamsOfRow` injection additionally
  warns about a `Shift.BY=2` that exceeds `maxShiftBy`. Two signals pointing at the same place.
  This is the one known-incomplete piece rather than a deliberate omission.

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

**And they are registered now.** Writing the renderer classes was only half of it — `CCBlockEntityRenders.register()` sat empty for a while afterwards, so every one of them was dead code and the cogwheels still did not turn. All 24 entries are wired, through Create Fly's `AllBlockEntityRenders.visual` / `.render`. Three things that only became clear while doing it:

- **Take the list from `git show b5e21592:…/CCBlockEntityTypes.java`**, not from notes. The table this class used to carry in its own doc comment was missing `brake` and `kinetic_battery` outright.
- **`visual()` versus `normal()`.** Upstream's flag was Registrate's `renderNormally`; Create Fly's `visual(...)` sets `skipVanillaRender = true` and `normal(...)` sets it false. Two of Connected's calls omitted the flag, and rather than guess Registrate's default, note that their Create counterparts (`HAND_CRANK`, `ANALOG_LEVER`) used the identical spelling and Create Fly registers both with `visual(...)`. Nothing in this mod needs `normal`.
- **`EncasedCogRenderer::small` does not exist** — Create Fly split it into `EncasedSmallCogRenderer` and `EncasedLargeCogRenderer`. The visual side kept the `EncasedCogVisual::small` factory, so only the renderer half changes.

`CCPartialModels.register()` must run before this, because the visuals resolve partial models during construction. `CreateConnectedClient` already had the order right.

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
