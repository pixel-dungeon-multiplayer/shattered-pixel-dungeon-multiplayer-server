package io.github.pixeldungeonmultiplayer.shattered.server.network.actions;

import org.jetbrains.annotations.NotNull;

public class JournalSnapshotAction implements LiveStateNetworkAction {

	public final boolean forceFull;

	public JournalSnapshotAction() {
		this(false);
	}

	public JournalSnapshotAction(boolean forceFull) {
		this.forceFull = forceFull;
	}

	@Override
	public @NotNull String actionName() {
		return "journal_snapshot";
	}
}
