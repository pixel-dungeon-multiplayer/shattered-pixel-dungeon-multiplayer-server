package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import io.github.pixeldungeonmultiplayer.shattered.server.utils.Log;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ActorSerializer implements Serializer<Actor> {

    @Override
    public Object serialize(@NotNull Actor actor, @NotNull SerializationContext ctx, @NotNull String profile) {
        Log.w(
                "ActorSerializer",
                "Unexpected ActorSerializer call for type " + actor.getClass().getName()
                        + " with profile '" + profile + "'. Stacktrace:\n" + stackTrace()
        );
        return new JSONObject();
    }

    private String stackTrace() {
        StringWriter stackTrace = new StringWriter();
        new Exception().printStackTrace(new PrintWriter(stackTrace));
        return stackTrace.toString();
    }
}
