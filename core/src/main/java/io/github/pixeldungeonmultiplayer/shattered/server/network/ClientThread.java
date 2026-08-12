package io.github.pixeldungeonmultiplayer.shattered.server.network;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.shattered.server.noosa.audio.Music;
import io.github.pixeldungeonmultiplayer.shattered.server.utils.Log;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.*;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.*;
import com.shatteredpixel.shatteredpixeldungeon.plugins.events.ChatEvent;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.*;
import io.github.pixeldungeonmultiplayer.shattered.server.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.heroes;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.level;
import static com.watabou.utils.PathFinder.NEIGHBOURS8;


public class ClientThread implements Callable<String> {

    public static final String CHARSET = "UTF-8";

    protected OutputStreamWriter writeStream;
    protected BufferedWriter writer;
    protected InputStreamReader readStream;
    private BufferedReader reader;

    protected int threadID;
    protected final int protocolVersion;

    protected final Socket clientSocket;

    protected Hero clientHero;
    protected String clientProtocolName;
    protected int clientProtocolVersion = -1;
    private String pendingJson;

    protected final NetworkPacket packet = new NetworkPacket();
    private final ArrayList<ChatMessageAction> pendingChatMessages = new ArrayList<>();
    boolean needHeroInit = true;

    //todo rewrite it on single thread pool
    @NotNull
    private FutureTask<String> jsonCall;

    public ClientThread(int ThreadID, Socket clientSocket, @Nullable Hero hero, int protocolVersion) {
        this(ThreadID, clientSocket, hero, protocolVersion, null);
    }

    public ClientThread(int ThreadID, Socket clientSocket, @Nullable Hero hero, int protocolVersion, @Nullable String initialJson) {
        clientHero = hero;
        this.clientSocket = clientSocket;
        this.protocolVersion = protocolVersion;
        this.clientProtocolName = Protocol.NAME;
        this.clientProtocolVersion = protocolVersion;
        this.pendingJson = initialJson;
        try {
            this.threadID = ThreadID;
            if (hero != null){
                hero.networkID = threadID;
            }
            writeStream = new OutputStreamWriter(
                    clientSocket.getOutputStream(),
                    Charset.forName(CHARSET).newEncoder()
            );
            readStream = new InputStreamReader(
                    clientSocket.getInputStream(),
                    Charset.forName(CHARSET).newDecoder()
            );
            reader = new BufferedReader(readStream);
            writer = new BufferedWriter(writeStream, 16384);
        } catch (IOException e) {
            GLog.n(e.getMessage());
            disconnect();
            return;
        }
        if (clientHero != null){
            sendInitData();
        }
        if (pendingJson == null) {
            updateTask();
        }
    }

    protected void updateTask() {
        if ((jsonCall == null) || (jsonCall.isDone())) {
            jsonCall = new FutureTask<String>(this);
            new Thread(jsonCall).start();
        }
    }
    @Override
    public String call() {
        if (clientSocket.isClosed()) {
            return null;
        }
        try {
            return reader.readLine();
        } catch (IOException e) {
            Gdx.app.error("ParseThread", e.getMessage());
            return null;
        }
    }

    public void parse(@NotNull String json) throws JSONException {
        JSONObject data = new JSONObject(json);
        Gdx.app.log("ClientThread", data.toString(4));
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String token = it.next();
            try {
                switch (token) {
                    case Protocol.FIELD_PACKET_TYPE: {
                        break;
                    }
                    case Protocol.FIELD_PROTOCOL: {
                        break;
                    }
                    case Protocol.FIELD_VERSION: {
                        break;
                    }
                    //Level block
                    case ("hero_class"): {
                        if (clientHero == null) {
                            InitPlayerHero(data.getString(token), data.getString("uuid"));
                        }
                        break;
                    }
                    case "uuid": {
                        //already parsed
                        break;
                    }
                    case ("talent_upgrade"): {
                        TalentButton.upgradeTalent(clientHero, Talent.valueOf(data.getString("talent_upgrade")));
                        break;
                    }
                    case ("cell_listener"): {
                        Integer cell = data.getInt(token);
                        if (clientHero.cellSelector != null) {
                            if (clientHero.cellSelector.getListener() == null) {
                                clientHero.cellSelector.setListener(clientHero.defaultCellListener);
                            }
                            if (clientHero.cellSelector.getListener() != null) {
                                if (cell != -1) {
                                    clientHero.cellSelector.getListener().onSelect(cell);
                                } else {
                                    GameScene.cancel(clientHero);
                                }
                                GameScene.ready(clientHero);
                            }
                        }
                        break;
                    }
                    case ("action"): {
                        JSONObject actionObj = data.getJSONObject(token);
                        if (actionObj == null) {
                            GLog.n("Empty action object");
                            break;
                        }
                        String action = actionObj.getString("action_name");
                        if ((action == null) || (action.equals(""))) {
                            GLog.n("Empty action");
                            break;
                        }
                        List<Integer> slot = Utils.JsonArrayToListInteger(actionObj.getJSONArray("slot"));
                        if ((slot == null) || slot.isEmpty()) {
                            GLog.n("Empty slot: %s", slot);
                            break;
                        }
                        //FIXME
                        Item item = clientHero.belongings.getItemInSlot(slot);
                        if (item == null) {
                            GLog.n("No item in this slot. Slot: %s", slot);
                            break;
                        }
                        action = action.toLowerCase(Locale.ROOT);
                        boolean did_something = false;
                        for (String item_action : item.actions(clientHero)) {
                            if (item_action.toLowerCase(Locale.ROOT).equals(action)) {
                                did_something = true;
                                item.execute(clientHero, item_action);
                                break;
                            }
                        }
                        if (!did_something) {
                            GLog.n("No such action in actions list. Action: %s", action);
                            break;
                        }
                        break;
                    }
                    case "window": {
                        JSONObject resObj = data.getJSONObject(token);
                        Window.OnButtonPressed(
                                clientHero,
                                resObj.getInt("id"),
                                resObj.getInt("button"),
                                resObj.optJSONObject("result")
                        );
                        break;
                    }
                    case "action_indicator": {
                        JSONObject request = data.optJSONObject(token);
                        ActionIndicator.Action current = clientHero == null ? null : clientHero.actionIndicator.action;
                        LocalizedString currentName = current == null ? null : current.actionName();
                        boolean sameName = request != null && (currentName == null
                                ? request.isNull("displayName")
                                : request.optJSONObject("displayName") != null
                                        && request.optJSONObject("displayName").similar(currentName.toJsonObject()));
                        boolean sameAction = current != null && request != null && sameName
                                && current.getClass().getName().equals(request.optString("actionId", null));
                        if (sameAction && clientHero.isReady()) {
                            current.doAction(clientHero);
                        } else if (clientHero != null) {
                            // The click raced with a server-side indicator update. Re-send authoritative state.
                            clientHero.actionIndicator.refresh();
                        }
                        break;
                    }
                    case "chat": {
                        if (clientHero == null) {
                            break;
                        }
                        String text = data.getJSONObject(token).optString("message", null);
                        if (text == null) {
                            text = data.getJSONObject(token).optString("text", "");
                        }
                        if (text.trim().isEmpty()) {
                            break;
                        }
                        ChatEvent chatEvent = new ChatEvent(text, clientHero);
                        Server.pluginManager.fireEvent(chatEvent);
                        if (!chatEvent.canceled) {
                            SendData.enqueueChatMessageToAll(clientHero.name + ": " + text.trim());
                        }
                        break;
                    }
                    case "toolbar_action": {
                        JSONObject actionObj = data.getJSONObject(token);
                        switch (actionObj.getString("action_name").toUpperCase(Locale.ENGLISH)) {
                            case "SLEEP": {
                                clientHero.rest(true);
                                break;
                            }
                            case "WAIT": {
                                clientHero.rest(false);
                                break;
                            }
                            case "SEARCH": {
                                clientHero.search(true);
                                break;
                            }
                            case "EXAMINE": {
                                GameScene.examineCell(actionObj.getInt("cell"), clientHero);
                                break;
                            }
                        }
                        break;
                    }
                    default: {
                        GLog.n("Server: Bad token: %s", token);
                        break;
                    }
                }
            } catch (JSONException e) {
                assert false;
                GLog.n(String.format("JSONException in ThreadID:%s; Message:%s", threadID, e.getMessage()));
            }
        }
    }

    public void parse() {
        if (pendingJson != null) {
            String json = pendingJson;
            pendingJson = null;
            updateTask();
            try {
                parse(json);
            } catch (JSONException e) {
                ShatteredPixelDungeon.reportException(e);
                GLog.n(e.getStackTrace().toString());
                disconnect();
            }
            return;
        }
        if (!jsonCall.isDone()) {
            return;
        }
        try {
            String json = jsonCall.get();
            if (json == null){
                disconnect();
                return;
            }
            updateTask();
            try {
                parse(json);
            } catch (JSONException e) {
                ShatteredPixelDungeon.reportException(e);
                GLog.n(e.getStackTrace().toString());
                disconnect();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //network functions
    @Deprecated
    protected void flush() {
    }

    protected void forceFlush() {
        try {
            JSONObject json;
            synchronized (packet) {
                packet.compress();
                json = packet.serialize(clientHero);
                packet.clearData();
            }
            if (json.length() <= 1) {
                // packet contains only packet type
                return;
            }
            if (DeviceCompat.isDebug()) {
                try {
                    //Log.i("flush", "clientID: " + threadID + " data:" + json.toString(4));
                } catch (JSONException ignored) {
                }
            }
            synchronized (writer) {
                writer.write(json.toString());
                writer.write('\n');
                writer.flush();
            }
        } catch (IOException e) {
            Log.e(String.format("ClientThread%d", threadID), String.format("IOException in threadID %s. Message: %s", threadID, e.getMessage()));
            disconnect();
        } catch (StackOverflowError e) {
            Log.e("ClientThread", "stack overflow %s", e);
        }
    }

    protected void enqueueChatMessage(@NotNull ChatMessageAction message) {
        synchronized (pendingChatMessages) {
            pendingChatMessages.add(message);
        }
    }

    protected void flushPendingChatMessages() {
        ArrayList<ChatMessageAction> messages;
        synchronized (pendingChatMessages) {
            if (pendingChatMessages.isEmpty()) {
                return;
            }
            messages = new ArrayList<>(pendingChatMessages);
            pendingChatMessages.clear();
        }
        sendImmediate(NetworkPacket.fromChatMessages(messages));
    }

    protected void sendImmediate(@NotNull NetworkPacket networkPacket) {
        try {
            networkPacket.compress();
            JSONObject data = networkPacket.serialize(clientHero);
            if (DeviceCompat.isDebug()) {
                try {
                    Log.i("immediate", "clientID: " + threadID + " data:" + data.toString(4));
                } catch (JSONException ignored) {
                }
            }
            synchronized (writer) {
                writer.write(data.toString());
                writer.write('\n');
                writer.flush();
            }
        } catch (IOException e) {
            Log.e(String.format("ClientThread%d", threadID), String.format("IOException in threadID %s. Message: %s", threadID, e.getMessage()));
            disconnect();
        }
    }

    protected void InitPlayerHero(String className, String uuid) {
        HeroClass curClass;
        try {
            curClass = HeroClass.valueOf(className.toUpperCase());
        } catch (IllegalArgumentException e) {
            if (!className.equals("random")) { //classID==0 is random class, so it  is not error
                GLog.w("Incorrect class:%s; threadID:%s", className, threadID);
            }
            curClass = Random.element(HeroClass.values());
        }
        boolean heroFound = false;
        Hero newHero = new Hero();
        if (uuid != null && !uuid.isEmpty()) {
            for (Hero h : heroes){
                if (h != null && h.uuid.equals(uuid)){
                    disconnect("Hero is already connected");
                }
            }
            Hero hero = Dungeon.loadHero(uuid);
            if (hero != null) {
                newHero = hero;
                heroFound = true;
            }
        }
        clientHero = newHero;
        level.linkHero(newHero);
        if (!heroFound) {
            newHero.live();
            curClass.initHero(newHero);
            for (int i : NEIGHBOURS8) {
                if (Actor.findChar(level.entrance() + i) == null && level.passable[level.entrance() + i]) {
                    newHero.pos = level.entrance() + i;
                    break;
                }
            }
        }
        //newHero.pos = Dungeon.getPosNear(level.entrance);

        if (newHero.pos == -1) {
            newHero.pos = level.entrance(); //todo  FIXME
        }
        newHero.timeToNow(); //todo check this: this may remove "paralytic"
        Actor.addDelayed(newHero, 1f);
        Dungeon.level.occupyCell(newHero);
        synchronized (heroes) { //todo fix it. It is not work
            for (int i = 0; i < heroes.length; i++) {
                if (heroes[i] == null) {
                    heroes[i] = newHero;
                    newHero.networkID = threadID;
                    newHero.name = "Player" + i;
                    break;
                }
            }

            if (newHero.networkID == -1) {
                throw new RuntimeException("Can not find place for hero");
            }
        }
        GameScene.addHeroSprite(newHero);
        newHero.timeToNow();
        newHero.spendAndNext(1f);
        newHero.resendReady();
        sendInitData();
        GameScene.shouldProcess = true;
        Music.INSTANCE.sendLastAction(newHero);
    }

    protected void addCharToSend(@NotNull Char ch) {
        synchronized (packet) {
            if (ch.id() > 0) {
                packet.packAndAdd(new CharUpdateAction(ch), clientHero);
            }
        }
        //todo SEND TEXTURE
    }

    public void addAllActors() {
        for (Actor actor : Actor.all()) {
            if (actor instanceof Char) {
                addCharToSend((Char) actor);
            } else if (actor instanceof Buff) {
                packet.packAndAdd(new BuffUpdateAction((Buff) actor), clientHero);
            } else if (actor instanceof Blob) {
                packet.packAndAdd(new BlobUpdateAction((Blob) actor), clientHero);
            }
        }
    }

    public void addTraps(@NotNull Level level) {
        synchronized (packet) {
            for (int pos = 0; pos < level.length(); pos++) {
                var trap = level.traps.get(pos, null);
                if (trap != null && trap.visible) {
                    packet.addAction(new TrapUpdateAction(pos, trap));
                }
            }
        }
    }

    public void packAndAddLevel(Level level) {
        synchronized (packet) {
            packet.addAction(new ResizeLevelAction(level));
            packet.addAction(new SetLevelVisualsAction(level));
            packet.addAction(new SetLevelEntranceAction(level.entrance()));
            packet.addAction(new SetLevelExitAction(level.exit()));
            packet.addAction(new SetLevelTilesAction(level));
            addCustomTilemaps(level);
            packet.addAction(new SetLevelStatesAction(level));

            level.heaps.values().forEach(heap -> {
                if (!heap.isEmpty()) {
                    packet.packAndAdd(new HeapUpdateAction(heap), clientHero);
                }
            });
            for (int pos = 0; pos < level.length(); pos++) {
                Plant plant = level.plants.get(pos, null);
                if (plant != null) {
                    packet.addLateLiveStateAction(new PlantUpdateAction(pos, plant));
                }
            }
        }
    }

    private void addCustomTilemaps(Level level) {
        for (int i = 0; i < level.customTiles.size(); i++) {
            CustomTilemap tilemap = level.customTiles.get(i);
            if (tilemap.vis != null) {
                packet.addLateLiveStateAction(new CustomTilemapActions.Add(false, i, tilemap));
            }
        }
        for (int i = 0; i < level.customWalls.size(); i++) {
            CustomTilemap tilemap = level.customWalls.get(i);
            if (tilemap.vis != null) {
                packet.addLateLiveStateAction(new CustomTilemapActions.Add(true, i, tilemap));
            }
        }
    }

    //send primitives
    @Deprecated
    public void sendCode(int code) {
        assert false : "removed_code";
        GLog.n("removed code");
    }

    @Deprecated
    public void send(int code, int Data) {
    }

    @Deprecated
    public static void sendAll(int code, int data) {
        for (int i = 0; i < Server.clients.length; i++) {
            if (Server.clients[i] != null) {
                Server.clients[i].send(code, data);
            }
        }
    }

    //hack
    boolean disconnected = false;
    public synchronized void disconnect(String message) {
        if (!disconnected) {
            disconnected = true;
            try {
                Server.sendDisconnect(clientSocket, "disconnect", message);
                clientSocket.close(); //it creates exception when we will wait client data
            } catch (Exception ignore) {
            }
            Server.clients[threadID] = null;
            Server.used[threadID] = false;
            SendData.clearIndicatorCaches(threadID);
            Server.refreshService();
            readStream = null;
            writeStream = null;
            if (jsonCall != null) {
                jsonCall.cancel(true);
            }
            if (clientHero != null) {
                clientHero.next();
                Dungeon.removeHero(clientHero);
                clientHero = null;
                GLog.n("player " + threadID + " disconnected");
                boolean notNullHero = false;
                for (Hero hero: Dungeon.heroes) {
                    if (hero != null) {
                        GameScene.shouldProcess = true;
                        notNullHero = true;
                        break;
                    }
                }
                if (!notNullHero) {
                    GameScene.shouldProcess = false;
                }
            }
        }
    }
    public synchronized void disconnect() {
        disconnect("You was kicked");
    }

    private synchronized void sendInitData() {
        for (String texture : Server.textures) {
            sendTexture(texture);
        }

        packAndAddLevel(level);
        addTraps(level);
        packet.addAction(new HeroActorIdAction(clientHero.id()));
        packet.addAction(new HeroClassAction(clientHero.heroClass));
        packet.addAction(new HeroSubclassAction(clientHero.subClass));
        packet.addAction(new HeroStrengthAction(clientHero.STR()));
        packet.addAction(new HeroExperienceAction(clientHero.lvl, clientHero.exp));
        packet.addAction(new HeroTalentsAction(clientHero.talents));
        packet.addAction(new HeroGoldAction(clientHero.getGold()));
        packet.addAction(new HeroReadyAction(clientHero.isReady()));
        packet.addAction(new HeroUUIDAction(clientHero.uuid));
        packet.addAction(new UpdateFloorInfoAction(Dungeon.depth, Dungeon.branch, Dungeon.level != null? Dungeon.level.feeling: Level.Feeling.NONE));
        packet.addAction(new LockedFloorStateAction(Dungeon.level.locked));
        packet.addAction(new KeysIndicatorAction());
        packet.addAction(new UpdateCounterAction(clientHero.getCounter()));
        packet.addAction(new CellListenerPromptAction(clientHero.cellSelector.getListener()));
        packet.addAction(new AttackIndicatorTargetAction(SendData.getHeroAttackIndicatorTarget(threadID)));
        ActionIndicatorAction cachedAction = SendData.getHeroActionIndicator(threadID);
        packet.addAction(cachedAction != null ? cachedAction : new ActionIndicatorAction(clientHero.actionIndicator.action, clientHero));
        packet.addAction(new ResumeButtonVisibleAction(clientHero.lastAction != null));
        packet.addLateLiveStateAction(new SpecialSlotsDefinitionAction(clientHero));
        packet.addLateLiveStateAction(new InventoryRebuildAction(clientHero));
        Dungeon.observe(clientHero, false);
        packet.addLateLiveStateAction(new UpdateFovAction(clientHero));

        addAllActors();
        forceFlush(); //Let client process previous data while we're creating journal

        packet.packAndAdd(new JournalSnapshotAction(true), clientHero);
        forceFlush();

        packet.addAction(new InterlevelSceneAction("fade_out"));
        forceFlush();

        //these actions use client GameScene
        packet.addAction(BossHealthBar.createAction());
        forceFlush();

        Server.clients[threadID] = this;
        Server.refreshService();
    }
    private void sendTexture(String textureData){
        packet.addAction(new TexturePackAction(textureData));
        forceFlush();
    }
}
