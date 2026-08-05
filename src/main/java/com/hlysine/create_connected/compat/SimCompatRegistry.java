package com.hlysine.create_connected.compat;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.registries.PreciseItemUseOverrides;
import com.hlysine.create_connected.content.linkedtransmitter.*;
import com.hlysine.create_connected.datagen.CCBlockStateGen;
import com.zurrtum.create.AllBlockTags;
import com.hlysine.create_connected.foundation.registrate.CCRegistrate;
import com.hlysine.create_connected.foundation.registrate.BlockEntityEntry;
import com.hlysine.create_connected.foundation.registrate.BlockEntry;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverVisual;
import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.Blocks;

public class SimCompatRegistry {
    private static final CCRegistrate REGISTRATE = CreateConnected.getRegistrate();

    public static final BlockEntry<LinkedThrottleLeverBlock> LINKED_THROTTLE_LEVER = REGISTRATE
            .block("linked_throttle_lever", properties -> new LinkedThrottleLeverBlock(properties, SimBlocks.THROTTLE_LEVER))
            .initialProperties(() -> Blocks.LEVER)
            .tag(AllBlockTags.SAFE_NBT)
            .addLayer(() -> RenderType::cutoutMipped)
            .transform(LinkedTransmitterItem.register())
            .onRegister(PreciseItemUseOverrides::addBlock)

            .asOptional()
            .register();

    public static final BlockEntityEntry<LinkedThrottleLeverBlockEntity> LINKED_THROTTLE_LEVER_ENTITY = REGISTRATE
            .blockEntity("linked_throttle_lever", LinkedThrottleLeverBlockEntity::new)
            .visual(() -> ThrottleLeverVisual::new)
            .validBlocks(SimCompatRegistry.LINKED_THROTTLE_LEVER)
            .renderer(() -> LinkedThrottleLeverRenderer::new)
            .register();

    public static void register() {
    }
}
