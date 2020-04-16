package tesseract.graph;

import tesseract.util.Dir;

import javax.annotation.Nonnull;


/**
 * A class that acts as a controller for a connectivity of the connectables.
 */
public final class Connectivity {

    /**
     * @param connectable The given instance.
     * @return Gets the connectivity for instance.
     */
    public static byte of(@Nonnull IConnectable connectable) {
        byte connectivity = 0;

        for (Dir direction : Dir.VALUES) {
            if (connectable.connects(direction)) {
                connectivity = Connectivity.with(connectivity, direction);
            }
        }

        return connectivity;
    }

    /**
     * @param connectivity The provided state.
     * @param side The direction index.
     * @return Connectivity state for a connection.
     */
    public static byte with(byte connectivity, @Nonnull Dir side) {
        return (byte) (connectivity | (1 << side.ordinal()));
    }

    /**
     * @param connectivity The provided state.
     * @param side The direction index.
     * @return True if a connection is exist, false otherwise.
     */
    public static boolean has(byte connectivity, @Nonnull Dir side) {
        return (connectivity & (1 << side.ordinal())) > 0;
    }

    /**
     * The Cache is a class that should work with connections.
     */
    public final static class Cache<C extends IConnectable> {

        private byte connectivity;
        private C value;

        private Cache() {
        }

        /**
         * Creates a cache instance.
         */
        @Nonnull
        public static <C extends IConnectable> Cache<C> of(@Nonnull C value) {
            Cache<C> cache = new Cache<>();
            cache.value = value;
            cache.connectivity = Connectivity.of(value);
            return cache;
        }

        /**
         * @param direction The direction index.
         * @return True when connect, false otherwise.
         */
        public boolean connects(@Nonnull Dir direction) {
            return Connectivity.has(connectivity, direction);
        }

        /**
         * @return Gets the connection state.
         */
        public byte connectivity() {
            return connectivity;
        }

        /**
         * @return Gets the cache object.
         */
        @Nonnull
        public C value() {
            return value;
        }
    }
}
