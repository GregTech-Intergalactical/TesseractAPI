package tesseract.graph;

import net.minecraft.core.Direction;
import tesseract.api.IConnectable;

/**
 * The Cache is a class that should work with connections.
 */
public class Cache<T extends IConnectable> {

    private final byte connectivity;
    private final boolean pathing;
    private final T value;
    private NodeCache<?> cache;

    /**
     * Creates a cache instance.
     */
    public Cache(T value, boolean pathing) {
        this.value = value;
        this.connectivity = Connectivity.of(value);
        this.pathing = pathing;
    }

    /**
     * @param direction The direction index.
     * @return True when connect, false otherwise.
     */
    public boolean connects(Direction direction) {
        return Connectivity.has(connectivity, direction.get3DDataValue());
    }

    /**
     * @return Gets the connection state.
     */
    public byte connectivity() {
        return connectivity;
    }

    /**
     * @return Gets the cache.
     */
    public T value() {
        return value;
    }

    public boolean pathing() {
        return pathing;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Cache && ((Cache<?>)obj).value == this.value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @SuppressWarnings("unchecked")
    public <N> NodeCache<N> resolveCaps(long thisPos, Graph.INodeGetter<N> getter) {
        if (cache != null) return (NodeCache<N>) cache;
        NodeCache<N> cache = new NodeCache<N>(thisPos, this.value, getter);
        this.cache = cache;
        return cache;
    }
}