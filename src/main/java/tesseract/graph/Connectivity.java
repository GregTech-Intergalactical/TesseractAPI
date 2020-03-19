package tesseract.graph;

import tesseract.util.Dir;

/**
 *
 */
public class Connectivity {

    /**
     *
     * @param connectivity
     * @param side
     * @return
     */
    public static byte with(byte connectivity, Dir side) {
        return (byte) (connectivity | (1 << side.ordinal()));
    }

    /**
     *
     * @param connectable
     * @return
     */
    public static byte of(IConnectable connectable) {
        byte connectivity = 0;

        for (Dir direction : Dir.VALUES) {
            if (connectable.connects(direction)) {
                connectivity = Connectivity.with(connectivity, direction);
            }
        }

        return connectivity;
    }

    /**
     *
     * @param connectivity
     * @param side
     * @return
     */
    public static boolean has(byte connectivity, Dir side) {
        return (connectivity & (1 << side.ordinal())) > 0;
    }

    /**
     * The Cache is a class that should work with connections.
     */
    public static class Cache<C> {

        private byte connectivity;
        private C value;

        private Cache() { }

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

        public byte connectivity() {
            return connectivity;
        }

        public C value() {
            return value;
        }
    }
}
