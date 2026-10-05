# Create: Connected (Fly Port)

**An unofficial Fabric / Minecraft 26.2 port of [Create: Connected](https://modrinth.com/mod/create-connected) by Lysine, built on [Create Fly](https://modrinth.com/mod/create-fly) by ZurrTum.**

Posted with the original author's permission. It is still not Lysine's release and he does not maintain it, so please report issues on [this port's tracker](https://github.com/GravisLudio/create-connected-fly/issues), not on the original mod's.

## What's in this port

This port carries two kinds of content:

- **Create: Connected, ported with Lysine's permission.** Every block, item and feature of the original mod, and each new upstream release as it comes out, is brought to Fabric 26.2 here with the permission of Lysine, its author. The design, code and assets are Lysine's and the Create: Connected contributors'.
- **Extras designed by GravisLudio, only in this port.** The original mod does not have these:
  - **A goggle slot:** wear Engineer's Goggles in a slot of their own, just above the offhand slot, so they no longer cost you your helmet. Right-click goggles in the air or shift-click them in the inventory. It can be switched off with the `goggle_slot` feature toggle, and steps aside when Trinkets Updated is installed.
  - **Fixes for Create Fly bugs that players run into**, active whenever this mod is installed: Mechanical Arms stalling on the first stack and losing their targets in schematics made with Create, the Schematicannon eating a whole stack of books for one checklist, rich soil farmland costing plain dirt, and a crash report on every game exit.

Extras are marked *original to this port* in the changelog. Report problems with them here, never to Lysine.

## Requirements

- Minecraft 26.2 and Fabric Loader
- Fabric API
- Create Fly 26.2-rc-2-6.0.9-1
- Java 25

Create Fly replaces Create, Flywheel, Ponder and Catnip, since it embeds all of them. Do not install Create alongside it.

## What it adds

Everything Create: Connected 1.3.3 adds, running on Fabric: parallel, six-way and brass gearboxes, the crank wheel, kinetic batteries and bridges, chain cogwheels, the inverted, centrifugal, freewheel and overstress clutches, brakes, shear pins, cross connectors, item silos and fluid vessels, linked transmitters and analog levers, inventory bridges and access ports, brass chutes, the dashboard, the sequenced pulse generator, fan catalysts (including the Create: Dragons Plus ones when that mod is installed), and nine copycat shapes. JEI shows all of it.

Full feature list on the [original mod's wiki](https://github.com/hlysine/create_connected/wiki/New-Features).

## Status

Release. All 24 block entity renderers and Flywheel visuals are registered, connected textures are wired, block tints are registered, and kinetics propagate. Both rendering paths have been walked: with Flywheel on (the default) and with the Flywheel backend forced off, which is what runs if you use Sodium. A dedicated server loads cleanly, and automated game tests cover the Mechanical Arm fixes.

## Not implemented

None of these break anything; they are known gaps:

- No in-game config screen: feature toggles are edited in the config file, and changes need a restart
- Config is not synced server to client in multiplayer, so toggles can disagree between sides
- Integrations with Copycats+, Additional Placements, Dye Depot and Simulated are off until those mods reach 26.2
- The kinetic battery item looks empty at every charge level
- Gases pool at the bottom of a fluid vessel instead of floating
- Placing a multiblock plays one metal step per block rather than one per structure
- The Create: Connected creative tab no longer sits next to Create's
- Pick-block gives the wrong item on linked transmitters and encased cross connectors
- Contraption note blocks can no longer be intercepted by other mods
- The sequenced gearshift screen does not know this mod's three added instructions
- The exploding and dragon head fan catalysts show a bare frame as their inventory icon
- Copycats shade slightly differently from Create Fly's own: a lighting difference only, the blocks are solid

## World compatibility

- Do not load a 1.21.1 world with this. Item silos riding a contraption will not come back, because Create Fly removed legacy contraption storage reading.
- Copycat blocks save as `create:copycat`; they joined Create's block entity type instead of registering a second one. One-way, but no 26.2 world predates it.

## On the version number

`1.3.3` is upstream's version: this port matches Create: Connected 1.3.3. The trailing `-mc26.2-N` counts builds of this port against that upstream version.

## Credits and licence

- **Lysine** and the Create: Connected contributors: the mod itself. [Original repository](https://github.com/hlysine/create_connected)
- **ZurrTum**: [Create Fly](https://github.com/ZurrTum/Create-Fly), the Fabric Create port this is built on
- **sashafiesta**: the earlier Fabric 1.20.1 port

Licensed AGPL-3.0, plus the additional terms at the end of the original LICENSE: distinct name and icon, links back to the original, issues handled here.

Source: [GravisLudio/create-connected-fly](https://github.com/GravisLudio/create-connected-fly)
