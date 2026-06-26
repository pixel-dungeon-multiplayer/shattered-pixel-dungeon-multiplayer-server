package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.wndoptions;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.ui.ImageIcon;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogContract;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows.wnddialog.WndDialogSerializer;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WndOptionsSerializer<T extends WndOptions> extends WndDialogSerializer<T> {

    @Override
    protected @NotNull WndDialogContract getContract(@NotNull T obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        WndOptions.WndOptionsParams params = Objects.requireNonNull(obj.params());

        WndDialogContract contract = new WndDialogContract();
        contract.titleText = params.title;
        contract.titleColor = params.titleColor;
        contract.message = params.message;
        contract.layout = WndDialogContract.Layout.options();
        contract.titleIcon = titleIcon(params);
        for (int i = 0; i < params.options.size(); i++) {
            LocalizedString option = params.options.get(i);
            contract.actions.add(new WndDialogContract.Action(
                    option,
                    obj.hasInfoForNetwork(i),
                    obj.enabledForNetwork(i),
                    optionIcon(obj, i, ctx, profile)));
        }

        return contract;
    }

    private @NotNull ImageIcon titleIcon(@NotNull WndOptions.WndOptionsParams params) {
        if (params.itemSpriteImage != null) {
            return ImageIcon.itemSprite(params.itemSpriteImage, params.itemSpriteGlowing);
        }
        if (params.charSprite != null) {
            return ImageIcon.charSprite(
                    params.charSprite.getSpriteAsset(),
                    params.charSprite.spriteName());
        }
        return ImageIcon.none();
    }

    private @NotNull ImageIcon optionIcon(
            @NotNull WndOptions obj,
            int index,
            @NotNull SerializationContext ctx,
            @NotNull String profile) {
        Image icon = obj.optionIcon(index);
        return ImageIcon.fromImage(icon, ctx, profile);
    }
}
