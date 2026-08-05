package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

/**
 * {@code JukeboxSong} is a datapack registry, so the songs themselves live in
 * {@code data/create_connected/jukebox_song/*.json} — already committed. Code only needs the keys,
 * which is why the NeoForge {@code JsonCodecProvider} that used to build those files is gone along
 * with the {@code JukeboxSong} instances it fed.
 */
public class CCJukeboxSongs {
    public static final ResourceKey<JukeboxSong> INTERLUDE = key("interlude");
    public static final ResourceKey<JukeboxSong> ELEVATOR = key("elevator");

    private static ResourceKey<JukeboxSong> key(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, CreateConnected.asResource(name));
    }
}
