package com.watabou.gltextures;

public interface TextureSource {

    class File implements TextureSource {
        public final String path;

        public File(String path) {
            this.path = path;
        }
    }

    class Solid implements TextureSource {
        public final int color;

        public Solid(int color) {
            this.color = color;
        }
    }

    class Gradient implements TextureSource {
        public final int[] colors;

        public Gradient(int[] colors) {
            this.colors = colors;
        }
    }
}
