/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.input.ControllerHandler;
import com.watabou.noosa.Game;
import com.watabou.plugins.PluginManifest;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Point;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class DesktopPlatformSupport extends PlatformSupport {

	//we recall previous window sizes as a workaround to not save maximized size to settings
	//have to do this as updateDisplaySize is called before maximized is set =S
	protected static Point[] previousSizes = null;

	@Override
	public void updateDisplaySize() {
		if (previousSizes == null) {
			previousSizes = new Point[2];
			previousSizes[1] = SPDSettings.windowResolution();
		} else {
			previousSizes[1] = previousSizes[0];
		}
		previousSizes[0] = new Point(Game.width, Game.height);
		if (!SPDSettings.fullscreen()) {
			SPDSettings.windowResolution(previousSizes[0]);
		}
	}

	private static boolean first = true;

	@Override
	public void updateSystemUI() {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				if (SPDSettings.fullscreen()) {
					int monitorNum = 0;
					if (!first) {
						Graphics.Monitor[] monitors = Gdx.graphics.getMonitors();
						for (int i = 0; i < monitors.length; i++) {
							if (((Lwjgl3Graphics.Lwjgl3Monitor) Gdx.graphics.getMonitor()).getMonitorHandle()
									== ((Lwjgl3Graphics.Lwjgl3Monitor) monitors[i]).getMonitorHandle()) {
								monitorNum = i;
							}
						}
					} else {
						monitorNum = SPDSettings.fulLScreenMonitor();
					}

					Graphics.Monitor[] monitors = Gdx.graphics.getMonitors();
					if (monitors.length <= monitorNum) {
						monitorNum = 0;
					}
					Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(monitors[monitorNum]));
					SPDSettings.fulLScreenMonitor(monitorNum);
				} else {
					Point p = SPDSettings.windowResolution();
					Gdx.graphics.setWindowedMode(p.x, p.y);
				}
				first = false;
			}
		});
	}

	@Override
	public boolean connectedToUnmeteredNetwork() {
		return true; //no easy way to check this in desktop, just assume user doesn't care
	}

	@Override
	public boolean supportsVibration() {
		//only supports vibration via controller
		return ControllerHandler.vibrationSupported();
	}

	/* FONT SUPPORT */

	//custom pixel font, for use with Latin and Cyrillic languages
	private static FreeTypeFontGenerator basicFontGenerator;
	//droid sans fallback, for asian fonts
	private static FreeTypeFontGenerator asianFontGenerator;

	@Override
	public void setupFontGenerators(int pageSize, boolean systemfont) {
		//don't bother doing anything if nothing has changed
		if (fonts != null && this.pageSize == pageSize && this.systemfont == systemfont) {
			return;
		}
		this.pageSize = pageSize;
		this.systemfont = systemfont;

		resetGenerators(false);
		fonts = new HashMap<>();

		if (systemfont) {
			basicFontGenerator = asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		} else {
			basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel_font.ttf"));
			asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		}

		fonts.put(basicFontGenerator, new HashMap<>());
		fonts.put(asianFontGenerator, new HashMap<>());

		packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
	}

	private static Matcher asianMatcher = Pattern.compile("\\p{InHangul_Syllables}|" +
			"\\p{InCJK_Unified_Ideographs}|\\p{InCJK_Symbols_and_Punctuation}|\\p{InHalfwidth_and_Fullwidth_Forms}|" +
			"\\p{InHiragana}|\\p{InKatakana}").matcher("");

	@Override
	protected FreeTypeFontGenerator getGeneratorForString(String input) {
		if (asianMatcher.reset(input).find()) {
			return asianFontGenerator;
		} else {
			return basicFontGenerator;
		}
	}

	//splits on newline (for layout), chinese/japanese (for font choice), and '_'/'**' (for highlighting)
	private Pattern regularsplitter = Pattern.compile(
			"(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");

	//additionally splits on spaces, so that each word can be laid out individually
	private Pattern regularsplitterMultiline = Pattern.compile(
			"(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");

	@Override
	public String[] splitforTextBlock(String text, boolean multiline) {
		if (multiline) {
			return regularsplitterMultiline.split(text);
		} else {
			return regularsplitter.split(text);
		}
	}

	@Override
	public List<PluginManifest> loadPlugins() {
		List<PluginManifest> manifests = new ArrayList<>();
		if (Files.isDirectory(Paths.get("plugins/"))) {
			File[] files;
			try {
				files = new File("plugins/").listFiles();
				if (files == null) {
					return manifests;
				}
				for (File file : files) {
					if (file.getName().endsWith(".jar")) {
						JarFile jar = new JarFile(file.toPath().toAbsolutePath().toFile());
						ZipEntry manifest = jar.getEntry("plugin_manifest.txt");
						if (manifest != null) {
							InputStream input = jar.getInputStream(manifest);
							ByteArrayOutputStream result = new ByteArrayOutputStream();
							//might change buffer, 2kb should be fine. Can a manifest even be that big?
							byte[] buffer = new byte[2048];
							for (int length; (length = input.read(buffer)) != -1; ) {
								result.write(buffer, 0, length);
							}
							Gdx.app.log("PluginLoader", "Found manifest in: " + file.toPath());
							manifests.add(new PluginManifest(result.toString(), file.toPath().toAbsolutePath().toUri().toString()));
						} else {
							Gdx.app.error("PluginLoader", "Failed to find manifest in: " + file.getName());
						}
						jar.close();
					}
				}
			} catch (IOException e) {
				Gdx.app.error("PluginLoader", e.toString());
			}
		} else {
			try {
				Files.createDirectories(Paths.get("plugins"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return manifests;

	}

	private final Object dnsLock = new Object();
	private final ExecutorService dnsExecutor = Executors.newCachedThreadPool(r -> {
		Thread thread = new Thread(r, "SPDMP mDNS");
		thread.setDaemon(true);
		return thread;
	});
	private final Map<InetAddress, JmDNS> dnsByAddress = new LinkedHashMap<>();
	private final Map<InetAddress, ServiceInfo> serviceByAddress = new LinkedHashMap<>();
	private int dnsGeneration = 0;
	private Integer dnsServicePort = null;

	@Override
	public void registerService(int port, Map<String, String> properties) {
		synchronized (dnsLock) {
			dnsServicePort = port;
		}
		scheduleServiceRegistration(port, properties);
	}

	@Override
	public void updateService(Map<String, String> properties) {
		Map<InetAddress, ServiceInfo> servicesSnapshot;
		List<InetAddress> registeredAddresses;
		int generation;
		int port;
		Map<String, String> propertiesSnapshot = new HashMap<>(properties);
		synchronized (dnsLock) {
			if (dnsServicePort == null) {
				throw new IllegalStateException("Cannot update service before it is registered");
			}
			port = dnsServicePort;
			if (dnsByAddress.isEmpty()) {
				scheduleServiceRegistration(port, propertiesSnapshot);
				return;
			}
			generation = ++dnsGeneration;
			servicesSnapshot = new LinkedHashMap<>(serviceByAddress);
			registeredAddresses = new ArrayList<>(dnsByAddress.keySet());
		}
		dnsExecutor.submit(() -> {
			List<Future<?>> futures = new ArrayList<>();
			for (ServiceInfo serviceInfo : servicesSnapshot.values()) {
				futures.add(dnsExecutor.submit(() -> {
					try {
						serviceInfo.setText(propertiesSnapshot);
					} catch (IllegalStateException e) {
						Gdx.app.error("DNS", "Failed to update service TXT", e);
					}
				}));
			}
			for (InetAddress address : serviceAddresses()) {
				if (!registeredAddresses.contains(address)) {
					futures.add(dnsExecutor.submit(() -> registerService(address, port, propertiesSnapshot, generation)));
				}
			}
			waitFor(futures);
		});
	}

	@Override
	public void unregisterService() {
		Map<InetAddress, JmDNS> dnsSnapshot;
		synchronized (dnsLock) {
			dnsGeneration++;
			dnsServicePort = null;
			dnsSnapshot = new LinkedHashMap<>(dnsByAddress);
			dnsByAddress.clear();
			serviceByAddress.clear();
		}
		closeServices(dnsSnapshot);
	}

	private void scheduleServiceRegistration(int port, Map<String, String> properties) {
		Map<InetAddress, JmDNS> oldDns;
		int generation;
		Map<String, String> propertiesSnapshot = new HashMap<>(properties);
		synchronized (dnsLock) {
			generation = ++dnsGeneration;
			oldDns = new LinkedHashMap<>(dnsByAddress);
			dnsByAddress.clear();
			serviceByAddress.clear();
		}
		dnsExecutor.submit(() -> {
			closeServices(oldDns);
			List<InetAddress> addresses = serviceAddresses();
			if (addresses.isEmpty()) {
				registerService(null, port, propertiesSnapshot, generation);
				return;
			}
			List<Future<?>> futures = new ArrayList<>();
			for (InetAddress address : addresses) {
				futures.add(dnsExecutor.submit(() -> registerService(address, port, propertiesSnapshot, generation)));
			}
			waitFor(futures);
		});
	}

	private void registerService(InetAddress bindAddress, int port, Map<String, String> properties, int generation) {
		JmDNS dns = null;
		try {
			dns = bindAddress == null ? JmDNS.create() : JmDNS.create(bindAddress);
			ServiceInfo serviceInfo = ServiceInfo.create("_spdmp._tcp.local.", SPDSettings.serverName(), port, 0, 0, properties);
			dns.registerService(serviceInfo);
			InetAddress registeredAddress = dns.getInetAddress();
			boolean stale;
			synchronized (dnsLock) {
				stale = generation != dnsGeneration;
				if (!stale) {
					dnsByAddress.put(registeredAddress, dns);
					serviceByAddress.put(registeredAddress, serviceInfo);
				}
			}
			if (stale) {
				closeService(dns);
				return;
			}
			String host = registeredAddress == null ? "default interface" : registeredAddress.getHostAddress();
			System.out.println("Service registered: " + serviceInfo.getName() + " on " + host + ":" + serviceInfo.getPort());
		} catch (IOException e) {
			String host = bindAddress == null ? "default interface" : bindAddress.getHostAddress();
			Gdx.app.error("DNS", "Failed to register service on " + host, e);
			if (dns != null) {
				closeService(dns);
			}
		}
	}

	private void closeServices(Map<InetAddress, JmDNS> services) {
		if (services.isEmpty()) {
			return;
		}
		List<Future<?>> futures = new ArrayList<>();
		for (JmDNS dns : services.values()) {
			futures.add(dnsExecutor.submit(() -> closeService(dns)));
		}
		waitFor(futures);
		System.out.println("Service unregistered");
	}

	private void closeService(JmDNS dns) {
		try {
			dns.unregisterAllServices();
		} finally {
			try {
				dns.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void waitFor(List<Future<?>> futures) {
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				Gdx.app.error("DNS", "DNS task failed", e);
			}
		}
	}

	private List<InetAddress> serviceAddresses() {
		List<InetAddress> result = new ArrayList<>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) {
					continue;
				}
				for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
					if ((addr instanceof Inet4Address || addr instanceof Inet6Address) && !addr.isLoopbackAddress() && !addr.isAnyLocalAddress()) {
						result.add(addr);
					}
				}
			}
		} catch (IOException e) {
			Gdx.app.error("DNS", "Failed to enumerate network interfaces", e);
		}
		return result;
	}
}
