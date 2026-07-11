package io.github.pixeldungeonmultiplayer.shattered.server.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

final class ServerSocketBinder {

	private ServerSocketBinder() {
	}

	static ServerSocket bind(int port) throws IOException {
		ServerSocket socket = new ServerSocket();
		try {
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(port));
			return socket;
		} catch (IOException | RuntimeException e) {
			try {
				socket.close();
			} catch (IOException closeError) {
				e.addSuppressed(closeError);
			}
			throw e;
		}
	}
}
