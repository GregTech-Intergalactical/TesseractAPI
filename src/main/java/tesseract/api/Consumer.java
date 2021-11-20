package tesseract.api;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import tesseract.graph.Path;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import static java.lang.Integer.compare;

/**
 * A class that acts as a wrapper for a node component.
 */
abstract public class Consumer<C extends IConnectable, N> {

    protected final N node;
    protected final ConnectionType connection;

    protected Long2ObjectMap<Path.PathHolder<C>> full = Long2ObjectMaps.emptyMap();
    protected Long2ObjectMap<Path.PathHolder<C>> cross = Long2ObjectMaps.emptyMap();
    protected Set<Path.PathHolder<C>> covers;
    protected int distance;

    // Way of the sorting by the priority level and the distance to the node
    public static final Comparator<Consumer<?, ?>> COMPARATOR = (t1, t2) -> (t1.getPriority() == 0 && t2.getPriority() == 0) ? compare(t1.getDistance(), t2.getDistance()) : compare(t2.getPriority(), t1.getPriority());

    /**
     * Creates instance of the node.
     *
     * @param node The node instance.
     * @param path The path information.
     */
    protected Consumer(N node, Path<C> path) {
        this.node = node;

        if (path != null) {
            full = path.getFull();
            cross = path.getCross();
        }

        if (cross.size() == 0) {
            connection = (full.size() == 0) ? ConnectionType.ADJACENT : ConnectionType.SINGLE;
        } else {
            connection = ConnectionType.VARIATE;
        }
        ImmutableSet.Builder<Path.PathHolder<C>> builder = ImmutableSet.builder();
        if (full != null) {
            for (Path.PathHolder<C> value : full.values()) {
                if (value.connector instanceof ITransactionModifier && ((ITransactionModifier) value.connector).canModify(value.from, value.to)) {
                    builder.add(value);
                }
            }
        }
        this.covers = builder.build();
    }

    /**
     * Initialize all connectors of the full path.
     */
    public void init() {
        if (full != null) {
            distance = full.size();
            for (Path.PathHolder<C> connector : full.values()) {
                onConnectorCatch(connector.connector);
            }
        }
    }

    /**
     * @return Gets the node ref.
     */
    public N getNode() {
        return node;
    }


    /**
     * @return Gets the total distance from to the given consumer.
     */
    public int getDistance() {
        return distance;
    }

    /**
     * @return Gets the connection type.
     */
    public ConnectionType getConnection() {
        return connection;
    }

    /**
     * @return Gets the cross path of connectors.
     */
    public Long2ObjectMap<Path.PathHolder<C>> getCross() {
        return cross;
    }

    public Collection<Path.PathHolder<C>> getModifiers() {
        return covers;
    }

    /**
     * @return Gets the full path of connectors.
     */
    public Long2ObjectMap<Path.PathHolder<C>> getFull() {
        return full;
    }

    /**
     * Executes when constructor find a connector instance in the provided path.
     *
     * @param connector The connector object.
     */
    protected abstract void onConnectorCatch(C connector);

    /**
     * @return Returns the priority of this node as a number.
     */
    protected abstract int getPriority();
}
