package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.content.contraption.jukebox.PlayContraptionJukeboxPacket;
import com.hlysine.create_connected.content.sequencedpulsegenerator.ConfigureSequencedPulseGeneratorPacket;
import net.fabricmc.api.EnvType;
import com.hlysine.create_connected.content.goggleslot.GoggleSlotClickPacket;
import com.hlysine.create_connected.content.goggleslot.GoggleSlotQuickMovePacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Catnip's {@code CatnipPacketRegistry} and its {@code BasePacketPayload} hierarchy went with the
 * platform layer. Create Fly moved to vanilla {@code Packet}s registered into the protocol by
 * mixin; Connected does not need that reach, so this uses Fabric's networking API, which takes the
 * {@link CustomPacketPayload} and {@code StreamCodec} shapes the packets already had.
 * <p>
 * The direction a payload travels is declared at registration now, rather than implied by which
 * Catnip interface it implemented.
 */
public class CCPackets {
    public static final CustomPacketPayload.Type<ConfigureSequencedPulseGeneratorPacket> CONFIGURE_SEQUENCER =
            new CustomPacketPayload.Type<>(CreateConnected.asResource("configure_sequencer"));

    public static final CustomPacketPayload.Type<PlayContraptionJukeboxPacket> PLAY_CONTRAPTION_JUKEBOX =
            new CustomPacketPayload.Type<>(CreateConnected.asResource("play_contraption_jukebox"));

    public static final CustomPacketPayload.Type<GoggleSlotClickPacket> GOGGLE_SLOT_CLICK =
            new CustomPacketPayload.Type<>(CreateConnected.asResource("goggle_slot_click"));

    public static final CustomPacketPayload.Type<GoggleSlotQuickMovePacket> GOGGLE_SLOT_QUICK_MOVE =
            new CustomPacketPayload.Type<>(CreateConnected.asResource("goggle_slot_quick_move"));

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(CONFIGURE_SEQUENCER, ConfigureSequencedPulseGeneratorPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PLAY_CONTRAPTION_JUKEBOX, PlayContraptionJukeboxPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GOGGLE_SLOT_CLICK, GoggleSlotClickPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GOGGLE_SLOT_QUICK_MOVE, GoggleSlotQuickMovePacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                CONFIGURE_SEQUENCER,
                (payload, context) -> context.server().execute(() -> payload.apply(context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(
                GOGGLE_SLOT_CLICK,
                (payload, context) -> context.server().execute(() -> payload.apply(context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(
                GOGGLE_SLOT_QUICK_MOVE,
                (payload, context) -> context.server().execute(() -> payload.apply(context.player()))
        );

        // The client receiver sits behind an environment check rather than in a client entrypoint,
        // because Connected still builds as a single source set.
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            CCClientPackets.register();
    }
}
