package tesseract.graph;

import tesseract.util.Dir;

public class Connectivity {

    public static byte with(byte connectivity, Dir side) {
        return (byte) (connectivity | (1 << side.ordinal()));
    }

    public static byte of(IConnectable connectable) {
        byte connectivity = 0;

        for (Dir direction : Dir.VALUES) {
            if (connectable.connects(direction)) {
                connectivity = Connectivity.with(connectivity, direction);
            }
        }

        return connectivity;
    }

    public static boolean has(byte connectivity, Dir side) {
        return (connectivity & (1 << side.ordinal())) > 0;
    }

    public static class Cache<C> {

        byte connectivity;
        C value;

        private Cache() {
        }

        public static <C> Cache<C> ofDelegated(C value, IConnectable delegate) {
            Cache<C> cache = new Cache<>();

            cache.value = value;
            cache.connectivity = Connectivity.of(delegate);

            return cache;
        }

        public static <C extends IConnectable> Cache<C> of(C value) {
            Cache<C> cache = new Cache<>();

            cache.value = value;
            cache.connectivity = Connectivity.of(value);

            return cache;
        }

        public boolean connects(Dir direction) {
            return Connectivity.has(connectivity, direction);
        }

        public C value() {
            return value;
        }
    }
}
