package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.dtos;

import org.json.JSONArray;

public class KeyIndicatorDTO {
    public final int[] keys;

    public KeyIndicatorDTO(int[] keys) {
        this.keys = keys;
    }
}
