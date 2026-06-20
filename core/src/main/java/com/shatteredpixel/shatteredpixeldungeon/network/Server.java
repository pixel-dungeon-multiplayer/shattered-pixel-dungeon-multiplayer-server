package com.shatteredpixel.shatteredpixeldungeon.network;
// based on https://developer.android.com/training/connect-devices-wirelessly/nsd.html#java


import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Alchemize;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.network.actions.*;
import com.shatteredpixel.shatteredpixeldungeon.network.actions.serializers.*;
import com.shatteredpixel.shatteredpixeldungeon.network.NetworkPacket.SerializedAction;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.WndInfoTalentSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WndMetamorphChooseSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.WndMetamorphReplaceSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.heropack.WndHeroInfoSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.heropack.WndHeroSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.heropack.WndInfoSubclassSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.journalpack.WndBadgeSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.menupack.WndChallengesSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.*;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.wnddialog.wndoptions.*;
import com.shatteredpixel.shatteredpixeldungeon.plugins.PluginLoader;
import com.shatteredpixel.shatteredpixeldungeon.plugins.PluginManager;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AlchemyScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.texturepack.TexturePackManager;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ActorSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.CharSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.BagSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.HeapSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.HeapRemovalSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ItemSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.MissileAnchorSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SerializerRegistry;

import com.shatteredpixel.shatteredpixeldungeon.network.serializers.ParticleFactorySerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SpeckFactorySerializer;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.BelongingsSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SpecialSlotDefinitionsSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.SplashFactorySerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.KeyIndicatorSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.emitters.EmitterAnchorSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.dtos.emitters.EmitterAnchor;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.dtos.KeyIndicatorDTO;

import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.watabou.noosa.particles.SerializableParticleFactory;
import com.watabou.utils.Rect;
import com.watabou.utils.RectF;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.RectSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.RectFSerializer;
import com.shatteredpixel.shatteredpixeldungeon.network.serializers.windows.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;

public class Server extends Thread {
    public static final SerializerRegistry SERIALIZERS = new SerializerRegistry();

    static {
        SERIALIZERS.register(Item.class, "default", new ItemSerializer());
        SERIALIZERS.register(Heap.class, "default", new HeapSerializer());
        SERIALIZERS.register(Heap.class, "remove", new HeapRemovalSerializer());
        SERIALIZERS.register(Bag.class, "default", new BagSerializer());
        SERIALIZERS.register(Belongings.class, "default", new BelongingsSerializer());
        SERIALIZERS.register(Belongings.class, "special_slot_definitions", new SpecialSlotDefinitionsSerializer());
        SERIALIZERS.register(Char.class, "default", new CharSerializer());
        SERIALIZERS.register(Actor.class, "default", new ActorSerializer());
        SERIALIZERS.register(KeyIndicatorDTO.class, "default", new KeyIndicatorSerializer());
        SERIALIZERS.register(Rect.class, "default", new RectSerializer());
        SERIALIZERS.register(RectF.class, "default", new RectFSerializer());

        SERIALIZERS.register(SerializableParticleFactory.class, "default", new ParticleFactorySerializer());
        SERIALIZERS.register(Speck.SpeckFactory.class, "default", new SpeckFactorySerializer());
        SERIALIZERS.register(Splash.SplashFactory.class, "default", new SplashFactorySerializer());
        SERIALIZERS.register(EmitterAnchor.class, "default", new EmitterAnchorSerializer());
        SERIALIZERS.register(MissileSprite.Anchor.class, "default", new MissileAnchorSerializer());

        //actions
        SERIALIZERS.register(SerializedAction.class, new SerializedActionSerializer());
        SERIALIZERS.register(EmitterStopAction.class, new EmitterStopActionSerializer());
        SERIALIZERS.register(EmitterBurstAction.class, new EmitterBurstActionSerializer());
        SERIALIZERS.register(EmitterStartAction.class, new EmitterStartActionSerializer());
        SERIALIZERS.register(EmitterPourAction.class, new EmitterPourActionSerializer());
        SERIALIZERS.register(BuffUpdateAction.class, new BuffUpdateActionSerializer());
        SERIALIZERS.register(BuffRemoveAction.class, new BuffRemoveActionSerializer());
        SERIALIZERS.register(JournalSnapshotAction.class, new JournalSnapshotActionSerializer());
        SERIALIZERS.register(PlantUpdateAction.class, new PlantUpdateActionSerializer());
        SERIALIZERS.register(PlantRemoveAction.class, new PlantRemoveActionSerializer());
        SERIALIZERS.register(TrapUpdateAction.class, new TrapUpdateActionSerializer());
        SERIALIZERS.register(TrapRemoveAction.class, new TrapRemoveActionSerializer());
        SERIALIZERS.register(RippleVisualAction.class, new RippleVisualActionSerializer());
        SERIALIZERS.register(ActorRemoveAction.class, new ActorRemoveActionSerializer());
        SERIALIZERS.register(ChatMessageAction.class, new ChatMessageActionSerializer());
        SERIALIZERS.register(ChatMessagesAction.class, new ChatMessagesActionSerializer());
        SERIALIZERS.register(GameSceneFlashAction.class, new GameSceneFlashActionSerializer());
        SERIALIZERS.register(SurpriseVisualAction.class, new SurpriseVisualActionSerializer());
        SERIALIZERS.register(FlareVisualAction.class, new FlareVisualActionSerializer());
        SERIALIZERS.register(EnchantingVisualAction.class, new EnchantingVisualActionSerializer());
        SERIALIZERS.register(MagicMissileVisualAction.class, new MagicMissileVisualActionSerializer());
        SERIALIZERS.register(WoundVisualAction.class, new WoundVisualActionSerializer());
        SERIALIZERS.register(DiscoverTileAction.class, new DiscoverTileActionSerializer());
        SERIALIZERS.register(UpdateFovAction.class, new UpdateFovActionSerializer());
        SERIALIZERS.register(SetLevelEntranceAction.class, new SetLevelEntranceActionSerializer());
        SERIALIZERS.register(SetLevelExitAction.class, new SetLevelExitActionSerializer());
        SERIALIZERS.register(CharEmoAction.class, new CharEmoActionSerializer());
        SERIALIZERS.register(CharSpriteStateAction.class, new CharSpriteStateActionSerializer());
        SERIALIZERS.register(HeapRemoveAction.class, new HeapRemoveActionSerializer());
        SERIALIZERS.register(ShowBannerAction.class, new ShowBannerActionSerializer());
        SERIALIZERS.register(TexturePackAction.class, new TexturePackActionSerializer());
        SERIALIZERS.register(CharSpriteAction.class, new CharSpriteActionSerializer());
        SERIALIZERS.register(AlphaTweenerAction.class, new AlphaTweenerActionSerializer());
        SERIALIZERS.register(BossHealthBarAction.class, new BossHealthBarActionSerializer());
        SERIALIZERS.register(CharUpdateAction.class, new CharUpdateActionSerializer());
        SERIALIZERS.register(SpecialSlotsDefinitionAction.class, new SpecialSlotsDefinitionActionSerializer());
        SERIALIZERS.register(InventoryRebuildAction.class, new InventoryRebuildActionSerializer());
        SERIALIZERS.register(SpriteFlashAction.class, new SpriteFlashActionSerializer());
        SERIALIZERS.register(InterlevelSceneAction.class, new InterlevelSceneActionSerializer());
        SERIALIZERS.register(UpdateCellsAction.class, new UpdateCellsActionSerializer());
        SERIALIZERS.register(SetLevelStatesAction.class, new SetLevelStatesActionSerializer());
        SERIALIZERS.register(SetLevelTilesAction.class, new SetLevelTilesActionSerializer());
        SERIALIZERS.register(SetLevelVisualsAction.class, new SetLevelVisualsActionSerializer());
        SERIALIZERS.register(ResizeLevelAction.class, new ResizeLevelActionSerializer());
        SERIALIZERS.register(ShowStatusAction.class, new ShowStatusActionSerializer());
        SERIALIZERS.register(ShakeCameraAction.class, new ShakeCameraActionSerializer());
        SERIALIZERS.register(HeroReadyAction.class, new HeroReadyActionSerializer());
        SERIALIZERS.register(HeroGoldAction.class, new HeroGoldActionSerializer());
        SERIALIZERS.register(HeroUUIDAction.class, new HeroUUIDActionSerializer());
        SERIALIZERS.register(HeroActorIdAction.class, new HeroActorIdActionSerializer());
        SERIALIZERS.register(HeroExperienceAction.class, new HeroExperienceActionSerializer());
        SERIALIZERS.register(HeroStrengthAction.class, new HeroStrengthActionSerializer());
        SERIALIZERS.register(HeroSubclassAction.class, new HeroSubclassActionSerializer());
        SERIALIZERS.register(HeroTalentsAction.class, new HeroTalentsActionSerializer());
        SERIALIZERS.register(HeroClassAction.class, new HeroClassActionSerializer());
        SERIALIZERS.register(UpdateFloorInfoAction.class, new UpdateFloorInfoActionSerializer());
        SERIALIZERS.register(LockedFloorStateAction.class, new LockedFloorStateActionSerializer());
        SERIALIZERS.register(KeysIndicatorAction.class, new KeysIndicatorActionSerializer());
        SERIALIZERS.register(UpdateCounterAction.class, new UpdateCounterActionSerializer());
        SERIALIZERS.register(CellListenerPromptAction.class, new CellListenerPromptActionSerializer());
        SERIALIZERS.register(AttackIndicatorTargetAction.class, new AttackIndicatorTargetActionSerializer());
        SERIALIZERS.register(ResumeButtonVisibleAction.class, new ResumeButtonVisibleActionSerializer());
        SERIALIZERS.register(MusicAction.class, new MusicActionSerializer());
        SERIALIZERS.register(SampleAction.PlayAction.class, new SampleActionSerializers.Play());
        SERIALIZERS.register(SampleAction.LoadAction.class, new SampleActionSerializers.Load());
        SERIALIZERS.register(SampleAction.UnloadAction.class, new SampleActionSerializers.Unload());
        SERIALIZERS.register(SampleAction.ReloadAction.class, new SampleActionSerializers.Reload());
        SERIALIZERS.register(MissileSpriteVisualAction.class, new MissileSpriteVisualActionSerializer());
        SERIALIZERS.register(FadingTrapsAction.Update.class, new FadingTrapsActionSerializers.Update());
        SERIALIZERS.register(FadingTrapsAction.Kill.class, new FadingTrapsActionSerializers.Kill());
        SERIALIZERS.register(BlobUpdateAction.class, new BlobUpdateActionSerializer());
        SERIALIZERS.register(ItemAction.Add.class, new ItemActionSerializers.Add());
        SERIALIZERS.register(ItemAction.Remove.class, new ItemActionSerializers.Remove());
        SERIALIZERS.register(ItemAction.Update.class, new ItemActionSerializers.Update());
        SERIALIZERS.register(ItemAction.Replace.class, new ItemActionSerializers.Replace());
        SERIALIZERS.register(HeapUpdateAction.class, new HeapUpdateActionSerializer());
        SERIALIZERS.register(UpdateWindowAction.class, new UpdateWindowActionSerializer());
        SERIALIZERS.register(AlchemyScene.class, new AlchemySceneSerializer());
        SERIALIZERS.register(WndBadge.class, new WndBadgeSerializer());
        SERIALIZERS.register(WndBag.class, new WndBagSerializer());
        SERIALIZERS.register(WndBlacksmith.class, new WndBlacksmithSerializer());
        SERIALIZERS.register(WndBlacksmith.WndReforge.class, new WndReforgeSerializer());
        SERIALIZERS.register(WndBlacksmith.WndSmith.class, new WndSmithSerializer());
        SERIALIZERS.register(WndChallenges.class, new WndChallengesSerializer());
        SERIALIZERS.register(WndChooseAbility.class, new WndChooseAbilitySerializer());
        SERIALIZERS.register(WndChooseSubclass.class, new WndChooseSubclassSerializer());
        SERIALIZERS.register(WndClericSpells.class, new WndClericSpellsSerializer());
        SERIALIZERS.register(WndCombo.class, new WndComboSerializer());
        SERIALIZERS.register(WndEnergizeItem.class, new WndEnergizeItemSerializer());
        SERIALIZERS.register(WndHero.class, new WndHeroSerializer());
        SERIALIZERS.register(WndHeroInfo.class, new WndHeroInfoSerializer());
        SERIALIZERS.register(WndImp.class, new WndImpSerializer());
        SERIALIZERS.register(WndInfoCell.class, new WndInfoCellSerializer());
        SERIALIZERS.register(WndInfoItem.class, new WndInfoItemSerializer<WndInfoItem>());
        SERIALIZERS.register(WndInfoSubclass.class, new WndInfoSubclassSerializer());
        SERIALIZERS.register(WndInfoTalent.class, new WndInfoTalentSerializer());
        SERIALIZERS.register(WndJournalItem.class, new WndJournalItemSerializer());
        SERIALIZERS.register(WndMessage.class, new WndMessageSerializer());
        SERIALIZERS.register(WndMonkAbilities.class, new WndMonkAbilitiesSerializer());
        SERIALIZERS.register(WndOptions.class, new WndOptionsSerializer());
        SERIALIZERS.register(WndQuest.class, new WndQuestSerializer());
        SERIALIZERS.register(WndResurrect.class, new WndResurrectSerializer());
        SERIALIZERS.register(WndSadGhost.class, new WndSadGhostSerializer());
        SERIALIZERS.register(WndTextInput.class, new WndTextInputSerializer());
        SERIALIZERS.register(WndTitledMessage.class, new WndTitledMessageSerializer<>());
        SERIALIZERS.register(WndUpgrade.class, new WndUpgradeSerializer());
        SERIALIZERS.register(WndUseItem.class, new WndUseItemSerializer());
        SERIALIZERS.register(WndWandmaker.class, new WndWandmakerSerializer());
        SERIALIZERS.register(Alchemize.WndAlchemizeItem.class, new WndAlchemizeItemSerializer());
        SERIALIZERS.register(StoneOfAugmentation.WndAugment.class, new WndAugmentSerializer());
        SERIALIZERS.register(StoneOfIntuition.WndGuess.class, new WndGuessSerializer());
        SERIALIZERS.register(ScrollOfMetamorphosis.WndMetamorphChoose.class, new WndMetamorphChooseSerializer());
        SERIALIZERS.register(ScrollOfMetamorphosis.WndMetamorphReplace.class, new WndMetamorphReplaceSerializer());
        SERIALIZERS.register(TrinketCatalyst.WndTrinket.class, new WndTrinketSerializer());
        SERIALIZERS.register(DriedRose.WndGhostHero.class, new WndGhostHeroSerializer());
        SERIALIZERS.register(RedirectServerAction.class, new RedirectServerActionSerializer());
    }

    public static ArrayList<String> textures = new ArrayList<>();
    // will return in the future
    public static PluginManager pluginManager = new PluginManager(new PluginLoader(ShatteredPixelDungeon.platform.loadPlugins()));

    //primitive vars
    public static String serviceName;
    protected static int localPort;
    public static boolean started = false;

    //network
    protected static ServerSocket serverSocket;
    protected static Server serverThread;
    protected static ClientThread[] clients = new ClientThread[0];
    protected static boolean[] used = new boolean[0];
    protected static RelayThread relay;

    //NSD
    public static volatile RegListenerState regListenerState = RegListenerState.NONE;
    protected static final int TIME_TO_STOP = 3000; //ms
    protected static final int SLEEP_TIME = 100; // ms

    protected static Thread serverStepThread;

    public static boolean startServerStepLoop() {
        if ((serverStepThread != null) && (serverStepThread.isAlive())) {
            return false;
        }
        {
            serverStepThread = new Thread() {
                @Override
                public void run() {
                    //
                    try {
                        while (!interrupted()) {
                            if (Game.instance != null) {
                                if ((Game.scene() instanceof GameScene)) {
                                    Game.instance.server_step();
                                    sleep(0);
                                } else {
                                    sleep(500);
                                }
                            } else {
                                sleep(500);
                            }
                        }
                    } catch (InterruptedException ignored) {

                    }
                }
            };
            serverStepThread.setDaemon(true);
            serverStepThread.setName("SHPD Server Step Thread");
        }
        serverStepThread.start();
        return true;
    }

    public static boolean startServer() {
        if (started) {
            GLog.h("start when started: WTF?! WHO AND WHERE USED THIS?!");
            return false;
        }
        clients = new ClientThread[SPDSettings.maxPlayers()];
        used = new boolean[SPDSettings.maxPlayers()];
        serviceName = SPDSettings.serverName();
        regListenerState = RegListenerState.NONE;
        if (!initializeServerSocket()) {
            return false;
        }
        registerService(localPort);
        started = true;
        serverThread = new Server();
        TexturePackManager.loadTextures("textures/");
        serverThread.start();
        pluginManager.initialize();
        return started;
    }

    public static boolean stopServer() {
        if (!started) {
            return true;
        }
        started = false;
        if (relay != null) {
            relay.interrupt();
            relay = null;
        }
        serverStepThread.interrupt();
        //ClientThread.sendAll(Codes.SERVER_CLOSED); //todo
        unregisterService();

        return true;
    }

    public static void parseActions() {
        for (ClientThread client : clients) {
            if (client == null) {
                continue;
            }
            client.parse();
        }
        if (!Actor.processing() && !Game.switchingScene()) {
            SendData.forceFlushAll();
        }
    }

    public static void startClientThread(Socket client) throws IOException {
        QueryClientThread queryThread = new QueryClientThread(client);
        queryThread.setDaemon(true);
        queryThread.setName("SPDMP Query Client");
        queryThread.start();
    }

    public static void joinClient(Socket client, String heroClass, String uuid, int protocolVersion) throws IOException {
        synchronized (clients) {
            for (int i = 0; i <= clients.length; i++) {   //search not connected
                if (i == clients.length) { //If we test last and it's connected too
                    rejectClient(client, "server_full", "Server is full");
                    client.close();
                } else if ((clients[i] == null) && !used[i])  {
                    client.setSoTimeout(0);
                    ClientThread thread = new ClientThread(i, client, null, protocolVersion);
                    //clients[i] = thread;
                    thread.InitPlayerHero(heroClass, uuid);
                    break;
                }
            }
        }
    }

    static void rejectClient(@NotNull Socket client, String reason, String message) throws IOException {
        sendDisconnect(client, reason, message);
    }

    static void sendDisconnect(@NotNull Socket client, String reason, String message) throws IOException {
        JSONObject packet = new JSONObject();
        packet.put(Protocol.FIELD_PACKET_TYPE, Protocol.PACKET_DISCONNECT);
        packet.put("reason", reason);
        packet.put("message", message);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                client.getOutputStream(),
                Charset.forName(ClientThread.CHARSET).newEncoder()
        ));
        writer.write(packet.toString());
        writer.write('\n');
        writer.flush();
    }

    //Server thread
    public void run() {
        if (SPDSettings.onlineMode()) {
            relay = new RelayThread();
            relay.start();
        }
        while (started) { //clients  listener
            Socket client;
            try {
                client = serverSocket.accept();  //accept connect
                startClientThread(client);
            } catch (IOException e) {
                if (!(e.getMessage().equals("Socket is closed"))) {  //"Socket is closed" means that client disconnected
                    GLog.h("IO exception:".concat(e.getMessage()));
                }
            }
        }
    }
    //DNS-SD
    protected static void registerService(int port) {
        try {
            ShatteredPixelDungeon.platform.registerService(port, serverInfoProperties());
        } catch (Exception e) {
            Gdx.app.error("DNS", "Failed to start dns-sd service", e);
        }
    }

    public static void unregisterService() {
        try {
            ShatteredPixelDungeon.platform.unregisterService();
        } catch (Exception e) {
            Gdx.app.error("DNS", "Failed to stop dns-sd service", e);
        }
    }

    protected static boolean initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            serverSocket = new ServerSocket(SPDSettings.serverPort());
        } catch (Exception e) {
            return false;
        }
        // Store the chosen port.
        localPort = serverSocket.getLocalPort();
        return true;
    }
    public static int onlinePlayers(){
        int onlineCount = 0;
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null){
                onlineCount++;
            }
        }
        return onlineCount;
    }
    public static @Nullable Hero findHeroByUUID(String uuid){
        for (Hero hero: Dungeon.heroes) {
            if (hero != null && hero.uuid.equals(uuid)) {
                return hero;
            }
        }
        return null;
    }
    public static JSONObject serverInfo(){
            JSONObject serverInfo = new JSONObject();
            serverInfo.put("name", SPDSettings.serverName());
            serverInfo.put("players", Server.onlinePlayers());
            serverInfo.put("max_players", Server.clients.length);
            serverInfo.put("challenges", SPDSettings.challenges());
            serverInfo.put("current_floor", Dungeon.depth);
            serverInfo.put("motd", SPDSettings.motd());
            serverInfo.put("server_version", Game.version);
            serverInfo.put("server_version_code", Game.versionCode);
            serverInfo.put("server_protocol_version", 2);
            return serverInfo;
    }
    private static Map<String, String> serverInfoProperties() {
        JSONObject info = serverInfo();
        Map<String, String> properties = new HashMap<>();
        Iterator<String> keys = info.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = info.opt(key);
            if (value != null) {
                properties.put(key, dnsPropertyValue(String.valueOf(value)));
            }
        }
        return properties;
    }
    private static String dnsPropertyValue(String value) {
        if (value.length() <= 200) {
            return value;
        }
        return value.substring(0, 200);
    }
    public static enum RegListenerState {NONE, UNREGISTERED, REGISTERED, REGISTRATION_FAILED, UNREGISTRATION_FAILED}
}
