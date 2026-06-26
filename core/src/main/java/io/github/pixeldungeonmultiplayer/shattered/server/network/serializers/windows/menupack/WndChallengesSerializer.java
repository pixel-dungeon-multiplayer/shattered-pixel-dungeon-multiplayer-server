package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.menupack;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.WindowSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChallenges;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class WndChallengesSerializer extends WindowSerializer<WndChallenges> {

    @Override
    protected @NotNull String type() {
        return "challenges";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndChallenges obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();
        args.put("title", ctx.serialize(Messages.get(WndChallenges.class, "title"), profile));
        args.put("checked", obj.checked());
        args.put("editable", obj.editable());
        JSONArray challenges = new JSONArray();
        for (int i = 0; i < Challenges.NAME_IDS.length; i++) {
            JSONObject challenge = new JSONObject();
            challenge.put("id", Challenges.NAME_IDS[i]);
            challenge.put("mask", Challenges.MASKS[i]);
            challenge.put("checked", (obj.checked() & Challenges.MASKS[i]) != 0);
            challenge.put("title", ctx.serialize(Messages.titleCase(Messages.get(Challenges.class, Challenges.NAME_IDS[i])), profile));
            challenge.put("description", ctx.serialize(Messages.get(Challenges.class, Challenges.NAME_IDS[i] + "_desc"), profile));
            challenges.put(challenge);
        }
        args.put("challenges", challenges);
        return args;
    }
}



