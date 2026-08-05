package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Upstream carried a copy of Create's sound registry, complete with the datagen that writes
 * {@code sounds.json} and the lang entries for subtitles. Both of those outputs are committed
 * assets now, and Create Fly registers eagerly against vanilla rather than through an event, so
 * the builder, the {@code RegisterEvent} hook and the data provider are all gone.
 * <p>
 * Registration order matters: this class must be touched before {@link CCJukeboxSongs}, which
 * reads the holders below at class-init time.
 */
public class CCSoundEvents {

    public static final SoundEntry ELEVATOR_MUSIC = register("elevator_music", SoundSource.RECORDS);
    public static final SoundEntry INTERLUDE_MUSIC = register("interlude_music", SoundSource.RECORDS);

    private static SoundEntry register(String name, SoundSource category) {
        Identifier id = CreateConnected.asResource(name);
        Holder.Reference<SoundEvent> holder = Registry.registerForHolder(
                BuiltInRegistries.SOUND_EVENT,
                id,
                SoundEvent.createVariableRangeEvent(id)
        );
        return new SoundEntry(holder, category);
    }

    /**
     * Kept as a no-op entry point so the mod initialiser still has a place to force class loading.
     */
    public static void register() {
    }

    public record SoundEntry(Holder<SoundEvent> holder, SoundSource category) {
        public Holder<SoundEvent> getMainEventHolder() {
            return holder;
        }

        public SoundEvent getMainEvent() {
            return holder.value();
        }

        public void play(Level world, @Nullable Player entity, double x, double y, double z, float volume, float pitch) {
            world.playSound(entity, x, y, z, getMainEvent(), category, volume, pitch);
        }

        public void play(Level world, @Nullable Player entity, Vec3i pos, float volume, float pitch) {
            play(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch);
        }

        public void play(Level world, @Nullable Player entity, Vec3i pos) {
            play(world, entity, pos, 1, 1);
        }

        public void playOnServer(Level world, Vec3i pos) {
            play(world, null, pos, 1, 1);
        }

        public void playOnServer(Level world, Vec3i pos, float volume, float pitch) {
            play(world, null, pos, volume, pitch);
        }

        public void playFrom(Entity entity, float volume, float pitch) {
            if (!entity.isSilent())
                play(entity.level(), null, entity.blockPosition(), volume, pitch);
        }

        public void playFrom(Entity entity) {
            playFrom(entity, 1, 1);
        }

        public void playAt(Level world, double x, double y, double z, float volume, float pitch, boolean fade) {
            world.playLocalSound(x, y, z, getMainEvent(), category, volume, pitch, fade);
        }

        public void playAt(Level world, Vec3i pos, float volume, float pitch, boolean fade) {
            playAt(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch, fade);
        }

        public void playAt(Level world, Vec3 pos, float volume, float pitch, boolean fade) {
            playAt(world, pos.x(), pos.y(), pos.z(), volume, pitch, fade);
        }
    }
}
