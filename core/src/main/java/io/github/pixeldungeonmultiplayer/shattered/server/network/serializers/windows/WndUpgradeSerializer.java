package io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.windows;

import com.shatteredpixel.shatteredpixeldungeon.windows.WndUpgrade;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public class WndUpgradeSerializer extends WindowSerializer<WndUpgrade> {

    @Override
    protected @NotNull String type() {
        return "upgrade";
    }

    @Override
    protected @Nullable JSONObject args(@NotNull WndUpgrade obj, @NotNull SerializationContext ctx, @NotNull String profile) {
        JSONObject args = new JSONObject();

        // 1. Core items (for backwards compatibility and client actions)
        args.put("upgrader", ctx.serialize(obj.upgrader(), "inventory"));
        args.put("item", ctx.serialize(obj.toUpgrade(), "inventory"));
        args.put("force", obj.force());
        args.put("item_info", ctx.serialize(obj.toUpgrade().info(), profile));
        args.put("enabled", obj.getOwnerHero().isReady());

        // 2. Title & Description
        args.put("title", ctx.serialize(obj.getTitleText(), profile));
        args.put("desc", ctx.serialize(obj.getDescText(), profile));

        // 3. Left Slot (Current State)
        WndUpgrade.SlotState left = obj.getLeftSlot();
        if (left != null) {
            JSONObject leftSlot = new JSONObject();
            leftSlot.put("item", ctx.serialize(obj.toUpgrade(), "inventory"));
            leftSlot.put("level_text", left.levelText);
            leftSlot.put("level_color", left.levelColor);
            leftSlot.put("bg_color", left.bgColor);
            args.put("left_slot", leftSlot);
        }

        // 4. Right Slot (Future State)
        WndUpgrade.SlotState right = obj.getRightSlot();
        if (right != null) {
            JSONObject rightSlot = new JSONObject();
            rightSlot.put("item", ctx.serialize(obj.toUpgrade(), "inventory"));
            rightSlot.put("level_text", right.levelText);
            rightSlot.put("level_color", right.levelColor);
            rightSlot.put("bg_color", right.bgColor);
            args.put("right_slot", rightSlot);
        }

        // 5. Stats Table
        JSONArray stats = new JSONArray();
        for (WndUpgrade.VisualStat stat : obj.getVisualStats()) {
            JSONObject statObj = new JSONObject();
            statObj.put("title", ctx.serialize(stat.title, profile));
            statObj.put("val_from", ctx.serialize(stat.valFrom, profile));
            statObj.put("val_to", ctx.serialize(stat.valTo, profile));
            stats.put(statObj);
        }
        args.put("stats", stats);

        // 6. Messages / Warnings
        JSONArray messages = new JSONArray();
        for (WndUpgrade.VisualMessage message : obj.getVisualMessages()) {
            JSONObject msgObj = new JSONObject();
            msgObj.put("text", ctx.serialize(message.text, profile));
            msgObj.put("color", message.color);
            messages.put(msgObj);
        }
        args.put("messages", messages);

        // 7. Buttons
        JSONObject buttons = new JSONObject();

        JSONObject upgradeBtn = new JSONObject();
        upgradeBtn.put("text", ctx.serialize(obj.btnUpgrade().LocalizedStringText(), profile));
        upgradeBtn.put("enabled", obj.getOwnerHero().isReady());
        upgradeBtn.put("upgrader_item", ctx.serialize(obj.upgrader(), "inventory"));
        buttons.put("upgrade", upgradeBtn);

        JSONObject cancelBtn = new JSONObject();
        cancelBtn.put("text", ctx.serialize(obj.btnCancel().LocalizedStringText(), profile));
        buttons.put("cancel", cancelBtn);

        args.put("buttons", buttons);

        return args;
    }
}
