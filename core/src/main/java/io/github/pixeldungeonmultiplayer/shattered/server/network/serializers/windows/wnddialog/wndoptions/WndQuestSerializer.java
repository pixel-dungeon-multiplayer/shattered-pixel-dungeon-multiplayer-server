package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions;

import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogContract;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import org.jetbrains.annotations.NotNull;

public class WndQuestSerializer extends WndDialogSerializer<WndQuest> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull WndQuest obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = new WndDialogContract();
        contract.titleText = obj.charName();
        contract.message = obj.text();
        contract.titleIcon = ImageIcon.charSprite(obj.spriteName());
        contract.layout = WndDialogContract.Layout.titledMessage();
        return contract;
    }
}

