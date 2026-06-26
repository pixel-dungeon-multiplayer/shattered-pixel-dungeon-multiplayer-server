package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.quest.AmbitiousImpRoom;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class CustomTilemapSerializer<T extends CustomTilemap> implements Serializer<T> {

    protected abstract String tilemapType();

    @Override
    public final Object serialize(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject json = new JSONObject();
        json.put("type", tilemapType());
        json.put("x", obj.tileX);
        json.put("y", obj.tileY);
        json.put("w", obj.tileW);
        json.put("h", obj.tileH);
        json.put("texture", obj.texture);

        Tilemap visual = obj.vis;
        if (visual != null) {
            int[] data = visual.data;
            if (data != null) {
                JSONArray dataArr = new JSONArray();
                for (int tile : data) {
                    dataArr.put(tile);
                }
                json.put("cols", visual.mapWidth);
                json.put("data", dataArr);
                json.put("alpha", visual.alpha());
            }
        }
        return json;
    }

    public static class DefaultSerializer extends CustomTilemapSerializer<CustomTilemap> {
        @Override
        protected String tilemapType() {
            return "generic";
        }
    }

    public static class EntranceBarrierSerializer extends CustomTilemapSerializer<AmbitiousImpRoom.EntranceBarrier> {
        @Override
        protected String tilemapType() {
            return "entrance_barrier";
        }
    }

    public static class FadingTrapsSerializer extends CustomTilemapSerializer<PrisonBossLevel.FadingTraps> {
        @Override
        protected String tilemapType() {
            return "prison_fading_traps";
        }
    }
}
