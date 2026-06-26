package io.github.pixeldungeonmultiplayer.shattered.server.network;


import com.shatteredpixel.shatteredpixeldungeon.items.Item;

public class SpecialSlot {
    public int id;
    public String sprite = "items.png";
    public int image_id = 127;
    public Item item = null;

    public int path() {
        return -id - 1;
    }

    public SpecialSlot(int id, String sprite, int image_id, Item item) {
        this.id = id;
        this.sprite = sprite;
        this.image_id = image_id;
        this.item = item;
    }
}
