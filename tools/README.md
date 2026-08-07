# Porting tools

Artefacts from the 26.2 port. See `../PORTING.md` for how they were used.

| File | What it is |
|---|---|
| `createfly-main-classes.txt` | Every class in Create Fly's **main** source set (1,737) |
| `createfly-client-classes.txt` | Every class in its **client** source set (1,966) |
| `import-mapping.tsv` | Deterministic old→new import map, with a main/client column |
| `import-unmatched.txt` | Imports Create Fly has no equivalent for |
| `strip-datagen.pl` | Removes datagen calls from Registrate-style builder chains |
| `migrate-recipes.js` | Rewrites recipe JSONs from the 1.21.1 shapes to the 26.2 / Create Fly ones |

Regenerate the class lists after updating Create Fly:

```bash
r=path/to/Create-Fly/src
find "$r/main/java"   -name '*.java' | sed "s|$r/main/java/||;s|\.java$||"   | tr / . > createfly-main-classes.txt
find "$r/client/java" -name '*.java' | sed "s|$r/client/java/||;s|\.java$||" | tr / . > createfly-client-classes.txt
```

`strip-datagen.pl` is balanced-paren aware because the chains are multi-line with nested
lambdas. It has one known flaw, fixed by hand rather than in the script: it also removes datagen
calls nested inside *other* calls' arguments, which leaves a dangling receiver such as
`.onRegister(CCRegistrate)`. After running it, check with:

```bash
grep -nE "\.(onRegister|transform)\((CCRegistrate|AssetLookup|BuilderTransformers|ModelGen|BlockStateGen)\)"
```

`migrate-recipes.js` reports before it writes; pass `--write` to apply.

```bash
node tools/migrate-recipes.js src/generated/resources/data          # dry run
node tools/migrate-recipes.js src/generated/resources/data --write
```

Every shape it targets was read off a real file — vanilla's from the 26.2 jar, Create's from Create
Fly's own `data/create/recipe/<type>/` — rather than inferred. It deliberately rewrites only what it
transformed: the walk also reaches loot tables and tags, which carry a `type` and would otherwise
come back reformatted with no change of meaning. `create:sequenced_assembly` changed shape rather
than names and is skipped by design.
