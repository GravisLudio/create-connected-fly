package com.hlysine.create_connected.compat;

import com.zurrtum.create.api.schematic.requirement.SchematicRequirementRegistries;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Schematic costs Create Fly gets wrong, fixed here because this port is where they were reported.
 * Nothing in this class belongs to Create: Connected itself.
 * <p>
 * <b>Rich Soil Farmland.</b> Farmer's Delight's rich soil farmland extends vanilla farmland, and
 * Create Fly charges every farmland one dirt. Create has a special case making it cost rich soil,
 * but Create Fly ships it commented out behind a {@code //TODO} in {@code ItemRequirement.defaultOf}.
 * So a schematicannon turned plain dirt into rich soil farmland -- rich soil costs compost to make.
 * Registered as a provider so it is looked up lazily: Farmer's Delight may register its blocks
 * after this runs. The registry is consulted before {@code defaultOf}, so this wins.
 */
public final class SchematicRequirementFixes {
    private static final String FARMERS_DELIGHT = "farmersdelight";
    private static final Identifier RICH_SOIL_FARMLAND = Identifier.fromNamespaceAndPath(FARMERS_DELIGHT, "rich_soil_farmland");
    private static final Identifier RICH_SOIL = Identifier.fromNamespaceAndPath(FARMERS_DELIGHT, "rich_soil");

    private SchematicRequirementFixes() {
    }

    public static void register() {
        if (!FabricLoader.getInstance().isModLoaded(FARMERS_DELIGHT))
            return;
        SchematicRequirementRegistries.BLOCKS.registerProvider(SchematicRequirementFixes::richSoilFarmland);
    }

    private static SchematicRequirementRegistries.BlockRequirement richSoilFarmland(Block block) {
        if (!RICH_SOIL_FARMLAND.equals(BuiltInRegistries.BLOCK.getKey(block)))
            return null;
        Item richSoil = BuiltInRegistries.ITEM.getValue(RICH_SOIL);
        if (richSoil == Items.AIR)
            return null;
        ItemRequirement requirement = new ItemRequirement(ItemUseType.CONSUME, richSoil);
        return (state, be) -> requirement;
    }
}
