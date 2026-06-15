package com.shatteredpixel.shatteredpixeldungeon.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class SerializationContext {
    @NotNull
    private final SerializerRegistry registry;
    @Nullable
    public final Hero observer;

    public SerializationContext(@NotNull SerializerRegistry registry, @Nullable Hero observer) {
        this.registry = registry;
        this.observer = observer;
    }

    @CheckReturnValue
    public Object serialize(@Nullable Object obj) {
        return serialize(obj, "default");
    }

    @CheckReturnValue
    @Nullable
    public Object serialize(@Nullable Object obj, @NotNull String profile) {
        if (obj == null) return JSONObject.NULL;
        return serializeAs(obj, obj.getClass(), profile);
    }

    @CheckReturnValue
    public Object serializeAs(@Nullable Object obj, @NotNull Class<?> asClass) {
        return serializeAs(obj, asClass, "default");
    }

    @CheckReturnValue
    public Object serializeAs(@Nullable Object obj, @NotNull Class<?> asClass, @NotNull String profile) {
        if (obj == null) return JSONObject.NULL;

        @SuppressWarnings("unchecked")
        Serializer<Object> serializer = (Serializer<Object>) registry.get(asClass, profile);

        if (serializer == null) {
            throw new IllegalArgumentException("No serializer found for " + asClass + " with profile '" + profile + "'");
        }

        return serializer.serialize(obj, this, profile);
    }
}
