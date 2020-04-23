package tesseract.api;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import tesseract.graph.IConnectable;
import tesseract.graph.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that acts as a wrapper for a node component.
 */
abstract public class NodeWrapper<C extends IConnectable, N extends IConnectable> {

    protected final N node;
    protected final ConnectionType connection;

    protected Long2ObjectMap<C> full;
    protected Long2ObjectMap<C> cross;

    /**
     * Creates instance of the node.
     *
     * @param node The node instance.
     * @param path The path information.
     */
    protected NodeWrapper(@Nonnull N node, @Nullable Path<C> path) {
        this.node = node;

        if (path != null) {
            full = path.getFull();
            cross = path.getCross();
        }

        if (full != null) {
            for (C connector : full.values()) {
                onConnectorCatch(connector);
            }
        }

        if (cross == null || cross.size() == 0) {
            connection = (full == null) ? ConnectionType.ADJACENT : ConnectionType.SINGLE;
        } else {
            connection = ConnectionType.VARIATE;
        }
    }

    /**
     * @return Gets the node ref.
     */
    @Nonnull
    public N getNode() {
        return node;
    }

    /**
     * @return Gets the connection type.
     */
    @Nonnull
    public ConnectionType getConnection() {
        return connection;
    }

    /**
     * @return Gets the cross path of connectors.
     */
    @Nonnull
    public ObjectSet<Long2ObjectMap.Entry<C>> getCross() {
        return cross.long2ObjectEntrySet();
    }

    /**
     * @return Gets the full path of connectors.
     */
    @Nonnull
    public ObjectSet<Long2ObjectMap.Entry<C>> getFull() {
        return full.long2ObjectEntrySet();
    }

    /**
     * Executes when constructor find a connector instance in the provided path.
     *
     * @param connector The connector object.
     */
    protected abstract void onConnectorCatch(@Nonnull C connector);
}
