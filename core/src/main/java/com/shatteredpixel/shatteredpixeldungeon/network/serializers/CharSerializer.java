package com.shatteredpixel.shatteredpixeldungeon.network.serializers;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ClassSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TieredSprite;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.nikita22007.multiplayer.utils.Utils.putToJSONArray;

public class CharSerializer implements Serializer<Char> {

    @Override
    public Object serialize(@NotNull Char character, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject object = new JSONObject();

        try {
            int id = character.id();
            if (id <= 0) {
                return object;
            }
            object.put("id", id);

            if (character.getSprite() != null) {
                String spriteAsset = character.getSprite().getSpriteAsset();
                if (spriteAsset != null) {
                    object.put("sprite_asset", spriteAsset);
                } else {
                    object.put("sprite_name", character.getSprite().spriteName());
                }
                if (character.getSprite() instanceof TieredSprite) {
                    object.put("tier", ((TieredSprite) character.getSprite()).tier());
                }
                if (character.getSprite() instanceof ClassSprite) {
                    object.put("class", ((ClassSprite) character.getSprite()).heroClass());
                }
            } else if (character.spriteClass != null) {
                object.put("sprite_name", character.spriteClass.getName());
            }

            object.put("hp", character.getHP());
            object.put("max_hp", character.getHT());
            object.put("position", character.pos);
            object.put("name", ctx.serialize(character.name(), profile));

            int shield = character.shielding();
            if (shield > 0 || character.needsShieldUpdate()) {
                object.put("shield", shield);
            }
            CharSprite sprite = character.getSprite();
            if (sprite != null) {
                JSONArray states = putToJSONArray(sprite.states().toArray());
                object.put("states", states);
            }
            if (character instanceof Mob) {
                object.put("description", ctx.serialize(((Mob) character).description(), profile));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }
}
