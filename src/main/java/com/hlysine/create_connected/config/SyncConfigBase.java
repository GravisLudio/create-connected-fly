package com.hlysine.create_connected.config;

import com.hlysine.create_connected.CreateConnected;
import com.zurrtum.create.catnip.config.ConfigBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * A config section whose values a server can push to clients.
 * <p>
 * The serialisation half below is platform-neutral and kept as-is. The transport half was pure
 * NeoForge -- {@code PacketDistributor}, {@code PayloadRegistrar}, {@code ICustomConfigurationTask},
 * {@code ServerLifecycleHooks} -- and is <b>not ported yet</b>; see the note in {@link CCommon}.
 * <p>
 * The {@link SyncConfig} payload is left in place because it is built entirely from vanilla types
 * and is what the Fabric implementation will send. What is missing is registering it and pushing
 * it: on Fabric that means a vanilla {@code ConfigurationTask} plus a mixin into
 * {@code ServerConfigurationPacketListenerImpl}, the way Create Fly does it.
 * <p>
 * Until then config values stay local to each side.
 */
public abstract class SyncConfigBase extends ConfigBase {

    public final CompoundTag getSyncConfig() {
        CompoundTag nbt = new CompoundTag();
        writeSyncConfig(nbt);
        if (children != null)
            for (ConfigBase child : children) {
                if (child instanceof SyncConfigBase syncChild) {
                    if (nbt.contains(child.getName()))
                        throw new RuntimeException("A sync config key starts with " + child.getName() + " but does not belong to the child");
                    nbt.put(child.getName(), syncChild.getSyncConfig());
                }
            }
        return nbt;
    }

    protected void writeSyncConfig(CompoundTag nbt) {
    }

    public final void setSyncConfig(CompoundTag config) {
        if (children != null)
            for (ConfigBase child : children) {
                if (child instanceof SyncConfigBase syncChild) {
                    CompoundTag nbt = config.getCompoundOrEmpty(child.getName());
                    syncChild.readSyncConfig(nbt);
                }
            }
        readSyncConfig(config);
    }

    protected void readSyncConfig(CompoundTag nbt) {
    }

    /** Applies a received payload. Nothing calls this until the transport is wired up. */
    public void applySyncedConfig(SyncConfig data) {
        setSyncConfig(data.nbt());
        CreateConnected.LOGGER.debug("Sync Config: Received and applied server config {}", data.nbt());
    }

    public record SyncConfig(CompoundTag nbt) implements CustomPacketPayload {
        public static final Type<SyncConfig> TYPE = new Type<>(CreateConnected.asResource("sync_config"));

        public static final StreamCodec<ByteBuf, SyncConfig> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.COMPOUND_TAG,
                SyncConfig::nbt,
                SyncConfig::new
        );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
