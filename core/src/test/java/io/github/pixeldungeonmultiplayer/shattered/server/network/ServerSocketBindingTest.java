package io.github.pixeldungeonmultiplayer.shattered.server.network;

import org.junit.jupiter.api.Test;

import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerSocketBindingTest {

	@Test
	void occupiedPortPreventsServerSocketInitialization() throws Exception {
		try (ServerSocket occupied = new ServerSocket(0)) {
			assertThrows(Exception.class, () -> ServerSocketBinder.bind(occupied.getLocalPort()));
		}
	}
}
