package com.shatteredpixel.shatteredpixeldungeon.texturepack;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Base64Coder;
import io.github.pixeldungeonmultiplayer.shattered.server.network.Server;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TexturePackManager {
    public static HashMap<String, String> animationMap = new HashMap<>();

    public static void addTexturePack(FileHandle path){
        try {
            ZipFile zip = new ZipFile(path.file());
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                if(!entry.isDirectory()){
                    if (entry.getName().contains("animations") && entry.getName().endsWith(".json")){
                        String name = entry.getName();
                        while (name.contains("/")){
                            name = name.substring(name.indexOf('/')+1);
                        }
                        animationMap.put(name.replace(".json",""), name);
                    }
                }
            }
            zip.close();

            Server.textures.add(new String(Base64Coder.encode(path.readBytes())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Nullable
    public static String getMobAnimation(@NotNull Class<? extends CharSprite> mobClass){
        return animationMap.get(mobClass.getSimpleName().replace("Sprite","").toLowerCase(Locale.ROOT));
    }
    public static void loadTextures(String path){
        FileHandle fileHandle = FileUtils.getFileHandle(path);
        if (!fileHandle.exists()){
            fileHandle.mkdirs();
        }
        for (FileHandle handle : fileHandle.list()) {
            if (handle.name().endsWith(".zip")) {
                TexturePackManager.addTexturePack(handle);
                System.out.println("Added texture: "+ handle.nameWithoutExtension());
            }
        }
    }
}
