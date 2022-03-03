package tesseract.api;

import static java.lang.Integer.compare;

import java.util.Comparator;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import tesseract.api.capability.TesseractBaseCapability;
import tesseract.graph.Path;

/**
 * A class that acts as a wrapper for a node component.
 */
abstract public class Consumer<C extends IConnectable, N> {

    protected final N node;
    protected final ConnectionType connection;

    protected Long2ObjectMap<C> full = Long2ObjectMaps.emptyMap();
    protected Long2ObjectMap<C> cross = Long2ObjectMaps.emptyMap();
    protected int distance;

    // Way of the sorting by the priority level and the distance to the node
    public static final Comparator<Consumer<?, ?>> COMPARATOR = (t1, t2) -> (t1.getPriority() == 0 && t2.getPriority() == 0) ? compare(t1.getDistance(), t2.getDistance()) : compare(t2.getPriority(), t1.getPriority());

    /**
     * Creates instance of the node.
     *
     * @param node The node instance.
     * @param path The path information.
     */
    protected Consumer(N node, N producer, Path<C> path) {
        this.node = node;

        if (path != null) {
            full =  path.getFull();
            cross = path.getCross();
        }
        int fullSize = full.size();
        if (producer instanceof TesseractBaseCapability) {
            TesseractBaseCapability<?> cap = (TesseractBaseCapability<?>) producer;
            long pos = cap.tile.getBlockPos().asLong();
            if (full.size() == 0) {
                full = Long2ObjectMaps.singleton(pos, (C) cap.tile);
            } else {
                full.put(pos, (C) cap.tile);
            }
            if (cross.size() == 0) {
                cross = Long2ObjectMaps.singleton(pos, (C) cap.tile);
            } else {
                cross.put(pos, (C) cap.tile);
            }
        }
        int crossSize = cross.size();

        if (crossSize == 0) {
            connection = (fullSize == 0) ? ConnectionType.ADJACENT : ConnectionType.SINGLE;
        } else {
            connection = ConnectionType.VARIATE;
        }
    }

    /**
     * Initialize all connectors of the full path.
     */
    public void init() {
        if (full != null) {
            distance = full.size();
            for (C connector : full.values()) {
                onConnectorCatch(connector);
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
    public Long2ObjectMap<C> getCross() {
        return cross;
    }
    /**
     * @return Gets the full path of connectors.
     */
    public Long2ObjectMap<C> getFull() {
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
