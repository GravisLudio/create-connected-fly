package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.content.crossconnector.EncasedCrossConnectorBlock;
import com.zurrtum.create.client.content.decoration.encasing.EncasedCTBehaviour;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.hlysine.create_connected.foundation.registrate.CCRegistrate;
import com.hlysine.create_connected.foundation.registrate.SharedProperties;
import com.hlysine.create_connected.foundation.registrate.BlockBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;


public class CCBuilderTransformers {
    public static <B extends EncasedCrossConnectorBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> encasedCrossConnector(String casing,
                                                                                                                           Supplier<CTSpriteShiftEntry> casingShift) {
        return builder -> encasedBase(builder, CCBlocks.CROSS_CONNECTOR::get)
                .onRegister(CCRegistrate.connectedTextures(() -> new EncasedCTBehaviour(casingShift.get())))
                .onRegister(CCRegistrate.casingConnectivity((block, cc) -> cc.make(block, casingShift.get(),
                        (s, f) -> f.getAxis() == s.getValue(EncasedCrossConnectorBlock.AXIS))))

                .item()
                .model(AssetLookup)
                .build();
    }

    private static <B extends Block, P> BlockBuilder<B, P> encasedBase(BlockBuilder<B, P> b,
                                                                       Supplier<ItemLike> drop) {
        return b.initialProperties(SharedProperties::stone)
                .properties(BlockBehaviour.Properties::noOcclusion)
                .loot((p, lb) -> p.dropOther(lb, drop.get()));
    }
}
