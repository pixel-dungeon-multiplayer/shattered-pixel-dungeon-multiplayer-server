package io.github.pixeldungeonmultiplayer.shattered.server.desktop;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import io.github.pixeldungeonmultiplayer.shattered.headlessclient.HeadlessClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public final class DesktopMiningTransitionSmoke {
    private DesktopMiningTransitionSmoke() { }

    @Test
    @Tag("desktop")
    void leavingMiningLevelFromClientThreadShowsConfirmation() throws Exception {
        DesktopSmoke.launch("miningTransitionSmoke", MiningTransitionSmoke::new);
    }

    private static final class MiningTransitionSmoke extends DesktopSmoke {
        private MiningTransitionSmoke(int port) {
            super(port);
        }

        @Override protected String smokeThreadName() {
            return "SPDMP mining-transition smoke client";
        }

        @Override protected void runTest() throws Exception {
            waitForServer();
            try (HeadlessClient client = connectClient()) {
                joinAndWaitForScene(client);

                onGameThread(() -> new Pickaxe().identify(hero()).collect(hero().belongings.backpack));

                AtomicInteger confirmationWindow = new AtomicInteger(-1);
                client.afterAction("update_window", (ignored, action) ->
                        confirmationWindow.compareAndSet(-1, action.getInt("id")));

                LevelTransition exit = new LevelTransition(
                        Dungeon.level,
                        hero().pos,
                        LevelTransition.Type.BRANCH_ENTRANCE,
                        Dungeon.depth,
                        0,
                        LevelTransition.Type.BRANCH_EXIT
                );

                // Client actions are handled off the render thread. This is the path that crashed
                // before the confirmation window was scheduled with Game.runOnRenderThread.
                require(!new MiningLevel().activateTransition(hero(), exit),
                        "mining exit should wait for confirmation");
                waitForClient(client, () -> confirmationWindow.get() >= 0,
                        "mining exit confirmation window was not received");
            }
        }
    }
}
