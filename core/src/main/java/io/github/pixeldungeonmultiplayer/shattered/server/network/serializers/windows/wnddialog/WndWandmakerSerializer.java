package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndWandmaker;
import org.jetbrains.annotations.NotNull;

public class WndWandmakerSerializer extends WndDialogSerializer<WndWandmaker> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndWandmaker obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.titleText = obj.wandmaker.name();
        contract.message = obj.message;
        contract.titleIcon = ImageIcon.charSprite(obj.wandmaker.getSprite().getClass().getName());
        contract.layout = WndDialogContract.Layout.titledMessage();
        contract.itemSlots.add(new WndDialogContract.ItemSlot(Wandmaker.Quest.wand1, true));
        contract.itemSlots.add(new WndDialogContract.ItemSlot(Wandmaker.Quest.wand2, true));
        return contract;
    }
}
