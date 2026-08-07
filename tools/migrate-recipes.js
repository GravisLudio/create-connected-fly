// Migrates the committed 1.21.1 recipe JSONs to the 26.2 / Create Fly shapes.
// Every target shape was read off a real recipe: vanilla's from the 26.2 jar,
// Create's from Create Fly's own data/create/recipe/<type>/.
//
// Run with --write to apply; without it, reports only.

const fs = require('fs');
const path = require('path');

const ROOT = process.argv[2];
const WRITE = process.argv.includes('--write');

// An ingredient is a bare string in 26.2: "namespace:id", or "#namespace:tag".
// The {"item": ...} / {"tag": ...} object form is gone.
function ingredient(v) {
    if (typeof v === 'string') return v;
    if (v && typeof v === 'object') {
        if (typeof v.item === 'string') return v.item;
        if (typeof v.tag === 'string') return '#' + v.tag;
    }
    return v; // leave anything unrecognised alone rather than guess
}

const stats = {};
let touched = false;
function bump(k) { stats[k] = (stats[k] || 0) + 1; touched = true; }

function walk(dir, out = []) {
    for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
        const p = path.join(dir, e.name);
        if (e.isDirectory()) walk(p, out);
        else if (e.name.endsWith('.json')) out.push(p);
    }
    return out;
}

let changed = 0;
for (const file of walk(ROOT)) {
    const before = fs.readFileSync(file, 'utf8');
    let r;
    try { r = JSON.parse(before); } catch { console.log('UNPARSEABLE ' + file); continue; }
    if (!r || typeof r !== "object" || typeof r.type !== "string") continue;
    touched = false;

    switch (r.type) {
        // ---- vanilla: ingredients are strings, result already correct ----
        case 'minecraft:crafting_shapeless':
            if (Array.isArray(r.ingredients)) { r.ingredients = r.ingredients.map(ingredient); bump('shapeless'); }
            break;
        case 'minecraft:crafting_shaped':
            if (r.key && typeof r.key === 'object') {
                for (const k of Object.keys(r.key)) r.key[k] = ingredient(r.key[k]);
                bump('shaped');
            }
            break;
        case 'minecraft:stonecutting':
            if (r.ingredient !== undefined) { r.ingredient = ingredient(r.ingredient); bump('stonecutting'); }
            break;

        // ---- Create: singular ingredient, results stays a list ----
        case 'create:cutting':
        case 'create:pressing':
            if (Array.isArray(r.ingredients)) {
                r.ingredient = ingredient(r.ingredients[0]);
                delete r.ingredients;
                bump(r.type);
            }
            break;

        // ---- Create: ingredients[0] is the target, [1] the applied item ----
        case 'create:item_application':
        case 'create:deploying':
            if (Array.isArray(r.ingredients)) {
                r.target = ingredient(r.ingredients[0]);
                r.ingredient = ingredient(r.ingredients[1]);
                delete r.ingredients;
                bump(r.type);
            }
            break;

        // ---- Create: singular everything, including the result ----
        case 'create:filling':
            if (Array.isArray(r.ingredients)) { r.ingredient = ingredient(r.ingredients[0]); delete r.ingredients; }
            if (Array.isArray(r.fluid_ingredients)) { r.fluid_ingredient = r.fluid_ingredients[0]; delete r.fluid_ingredients; }
            if (Array.isArray(r.results)) { r.result = r.results[0]; delete r.results; }
            bump('filling');
            break;

        // sequenced_assembly is a shape change, not a rename -- done by hand
        case 'create:sequenced_assembly':
            bump('sequenced_assembly (skipped, by hand)');
            continue;
    }

    // Only rewrite what was actually transformed. Walking the whole data tree also
    // reaches loot tables and tags, which carry a "type" and would otherwise be
    // reformatted into a diff that says nothing.
    if (!touched) continue;

    const after = JSON.stringify(r, null, 2) + '\n';
    if (after !== before) {
        changed++;
        if (WRITE) fs.writeFileSync(file, after);
    }
}

console.log(WRITE ? 'APPLIED' : 'DRY RUN');
for (const [k, v] of Object.entries(stats).sort((a, b) => b[1] - a[1])) console.log(`  ${String(v).padStart(3)}  ${k}`);
console.log(`  ${changed} files would change`);
