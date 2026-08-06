package com.hlysine.create_connected.content.sequencedpulsegenerator;

import com.hlysine.create_connected.content.sequencedpulsegenerator.instructions.Instruction;
import com.hlysine.create_connected.registries.CCPackets;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Create's {@code BlockEntityConfigurationPacket} base is one of the classes Create Fly removed
 * rather than renamed. It carried the permission, distance and load checks every configuration
 * packet needs; Create Fly folded those into a single {@code onBlockEntityConfiguration} helper in
 * {@code AllHandle}. With only one such packet here, {@link #apply} inlines the same checks.
 */
public record ConfigureSequencedPulseGeneratorPacket(BlockPos pos, ListTag instructions) implements CustomPacketPayload {

    private static final int MAX_RANGE = 16;

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureSequencedPulseGeneratorPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ConfigureSequencedPulseGeneratorPacket::pos,
                    ByteBufCodecs.TAG, ConfigureSequencedPulseGeneratorPacket::instructions,
                    ConfigureSequencedPulseGeneratorPacket::new
            );

    public ConfigureSequencedPulseGeneratorPacket(BlockPos pos, Tag instructions) {
        this(pos, (ListTag) instructions);
    }

    /** Runs on the server thread; the sender is the player whose reach is checked. */
    public void apply(ServerPlayer player) {
        if (player.isSpectator() || !player.mayBuild())
            return;

        Level level = player.level();
        if (!level.isLoaded(pos))
            return;
        if (!pos.closerThan(player.blockPosition(), MAX_RANGE))
            return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SequencedPulseGeneratorBlockEntity be))
            return;

        be.currentInstruction = -1;
        be.instructions = Instruction.deserializeAll(instructions);
        be.reset();
        be.setChanged();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return CCPackets.CONFIGURE_SEQUENCER;
    }
}
