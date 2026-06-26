package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

public interface Serializer<T> {
    @CheckReturnValue
    Object serialize(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile);
}
