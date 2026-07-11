package com.shatteredpixel.shatteredpixeldungeon.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.utils.PlatformSupport;
import java.util.HashMap;
import java.util.Map;

public class AndroidHeadlessPlatformSupport extends PlatformSupport {

    private final Context context;
    private final NsdManager manager;
    private NsdServiceInfo service;
    private Integer servicePort;
    private Map<String, String> serviceProperties = new HashMap<>();
    private boolean serviceRegistered = false;
    private NsdManager.RegistrationListener serviceListener;

    public AndroidHeadlessPlatformSupport(Context context) {
        this.context = context.getApplicationContext();
        this.manager = (NsdManager) this.context.getSystemService(Context.NSD_SERVICE);
    }

    @Override public void updateDisplaySize() { }
    @Override public void updateSystemUI() { }

    @Override
    public boolean connectedToUnmeteredNetwork() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            return !cm.isActiveNetworkMetered();
        }
        return true;
    }

    @Override public boolean supportsVibration() { return false; }
    @Override public boolean supportsFullScreen() { return false; }
    @Override public boolean supportsPlugins() { return false; }
    @Override public void setupFontGenerators(int pageSize, boolean systemFont) { }
    @Override protected FreeTypeFontGenerator getGeneratorForString(String input) { return null; }

    @Override
    public String[] splitforTextBlock(String text, boolean multiline) {
        return new String[]{text};
    }

    @Override
    public synchronized void registerService(int port, Map<String, String> properties) {
        if (manager == null) return;
        servicePort = port;
        serviceProperties = new HashMap<>(properties);
        service = new NsdServiceInfo();
        service.setServiceName(SPDSettings.serverName());
        service.setServiceType("_spdmp._tcp.");
        service.setPort(port);
        for (Map.Entry<String, String> property : properties.entrySet()) {
            service.setAttribute(property.getKey(), property.getValue());
        }
        serviceListener = newRegistrationListener();
        manager.registerService(service, NsdManager.PROTOCOL_DNS_SD, serviceListener);
    }

    @Override
    public synchronized void updateService(Map<String, String> properties) {
        if (manager == null) return;
        if (servicePort == null) {
            throw new IllegalStateException("Cannot update service before it is registered");
        }
        serviceProperties = new HashMap<>(properties);
        NsdManager.RegistrationListener oldListener = serviceListener;
        if (oldListener != null) {
            try {
                manager.unregisterService(oldListener);
            } catch (IllegalArgumentException ignored) {
            }
        }
        serviceRegistered = false;
        serviceListener = null;
        registerService(servicePort, serviceProperties);
    }

    @Override
    public synchronized void unregisterService() {
        if (manager == null) return;
        NsdManager.RegistrationListener oldListener = serviceListener;
        if (oldListener == null) {
            return;
        }
        try {
            manager.unregisterService(oldListener);
        } catch (IllegalArgumentException ignored) {
        }
        serviceRegistered = false;
        serviceListener = null;
        servicePort = null;
    }

    private NsdManager.RegistrationListener newRegistrationListener() {
        return new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                serviceRegistered = false;
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                serviceRegistered = false;
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                serviceRegistered = true;
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                serviceRegistered = false;
            }
        };
    }
}
