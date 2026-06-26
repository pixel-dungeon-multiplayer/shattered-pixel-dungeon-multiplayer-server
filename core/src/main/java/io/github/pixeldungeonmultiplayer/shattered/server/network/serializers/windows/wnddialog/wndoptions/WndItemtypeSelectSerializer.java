package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.Trinity;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogContract;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemButton;
import org.jetbrains.annotations.NotNull;

public class WndItemtypeSelectSerializer extends WndTitledMessageSerializer<Trinity.WndItemtypeSelect> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull Trinity.WndItemtypeSelect obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndDialogContract contract = super.getContract(obj, ctx, profile);

        for (ItemButton button : obj.buttons) {
            contract.itemSlots.add(new WndDialogContract.ItemSlot(button.item(), true));
        }

        return contract;
    }
}
