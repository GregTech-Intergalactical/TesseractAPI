package tesseract.graph;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.IConnectable;
import tesseract.util.*;
import tesseract.graph.traverse.ASFinder;
import tesseract.graph.traverse.BFDivider;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Grid provides the functionality of a set of linked nodes.
 * @apiNote default parameters are nonnull, methods return nonnull.
 */
public class Grid<C extends IConnectable> implements INode {

    private Long2ObjectMap<Connectivity.Cache<C>> connectors;
    private BFDivider divider;
    private ASFinder finder;
    private Listener nodes;

    // Prevent the creation of empty grids externally, a caller needs to use singleConnector.
    private Grid() {
        connectors = new Long2ObjectLinkedOpenHashMap<>();

        divider = new BFDivider(this);
        finder = new ASFinder(this);
        nodes = new Listener();
    }

    /**
     * @param pos The position of the connector.
     * @param connector The given connector.
     * @return Create a instance of a class for a given position and connector.
     */
    protected static <C extends IConnectable> Grid<C> singleConnector(long pos, Connectivity.Cache<C> connector) {
        Grid<C> grid = new Grid<>();
        grid.connectors.put(pos, Objects.requireNonNull(connector));
        return grid;
    }

    @Override
    public boolean contains(long pos) {
        return connectors.containsKey(pos) || nodes.containsKey(pos);
    }

    @Override
    public boolean linked(long from, Dir towards, long to) {
        Connectivity.Cache<C> cacheFrom = connectors.get(from);
        Connectivity.Cache<C> cacheTo = connectors.get(to);

        byte connectivityFrom = nodes.get(from);
        byte connectivityTo = nodes.get(to);

        boolean validLink = false;

        if (cacheFrom != null) {
            validLink = true;
            connectivityFrom = cacheFrom.connectivity();
        }

        if (cacheTo != null) {
            validLink = true;
            connectivityTo = cacheTo.connectivity();
        }

        if (connectivityFrom == Byte.MAX_VALUE || connectivityTo == Byte.MAX_VALUE) {
            return false;
        }

        return validLink && Connectivity.has(connectivityFrom, towards) && Connectivity.has(connectivityTo, towards.invert());
    }

    @Override
    public boolean connects(long position, Dir towards) {
        Connectivity.Cache<C> cache = connectors.get(position);
        byte connectivity = nodes.get(position);

        if (cache != null) {
            connectivity = cache.connectivity();
        }

        if (connectivity == Byte.MAX_VALUE) {
            return false;
        }

        return Connectivity.has(connectivity, towards);
    }

    /**
     * @return Gets the number of connectors.
     */
    public int countConnectors() {
        return connectors.size();
    }

    /**
     * @return Gets the number of linked nodes.
     */
    public int countNodes() {
        return nodes.size();
    }

    /**
     * @return Returns connectors map.
     */
    public Long2ObjectMap<Connectivity.Cache<C>> getConnectors() {
        return connectors;
    }

    /**
     * @return Returns nodes map.
     */
    public Long2ByteMap getNodes() {
        return nodes.getMap();
    }

    /**
     * Gets paths from the position to another linked nodes.
     *
     * @param pos The position of the linked node.
     * @return Returns full paths for the linked node.
     */
    public ObjectList<Path<C>> getPaths(long pos) {
        ObjectList<Path<C>> data = new ObjectArrayList<>();

        for (long target : nodes.keySet()) {
            if (pos != target) {
                data.add(new Path<>(connectors, finder.traverse(pos, target)));
            }
        }

        return data;
    }

    /**
     * Begins a find operation from the specified start position to the end position.
     *
     * @param start The start position of the finds operation.
     * @param end The end position of the finds operation.
     * @return An set of path points.
     */
    public ArrayDeque<Node> getPath(long start, long end) {
        return finder.traverse(start, end);
    }

    /**
     * Merges all of the elements from the other provided grid into this grid.
     *
     * @param at unknown.
     * @param other The other grid to merge elements from.
     */
    public void mergeWith(long at, Grid<C> other) {
        // TODO: Validate that the other grid touches the specified position.
        connectors.putAll(other.connectors);
        nodes.putAll(other.nodes);
        nodes.update();
    }

    /**
     * Gets a potentially random position from the grid.
     *
     * @return A random position from the grid.
     */
    public long sampleConnector() {
        LongIterator iterator = connectors.keySet().iterator();
        return iterator.hasNext() ? iterator.nextLong() : Long.MAX_VALUE;
    }

    /**
     * Adds a new connector to the grid.
     *
     * @param pos The given position.
     * @param connector The given connector.
     */
    public void addConnector(long pos, Connectivity.Cache<C> connector) {
        // TODO: Validate that the other grid touches the specified position.
        connectors.put(pos, Objects.requireNonNull(connector));
        nodes.update();
    }

    /**
     * Adds a new node to the grid.
     *
     * @param pos The given position.
     * @param node The given node.
     */
    public void addNode(long pos, Connectivity.Cache<?> node) {
        Objects.requireNonNull(node);
        nodes.put(pos, node.connectivity(), node.listener());
        nodes.update();
    }

    /**
     * Removes the node from the grid.
     *
     * @param pos The given position.
     */
    public void removeNode(long pos) {
        nodes.remove(pos);
        nodes.update();
    }

    /**
     * Removes an entry from the Grid, potentially splitting it if needed. By calling this function, the caller asserts
     * that this group contains the specified position; the function may misbehave if the group does not actually contain
     * the specified position.
     *
     * @param pos The position of the entry to remove.
     * @param split A consumer for the resulting fresh graphs from the split operation.
     * @return The removed entry, guaranteed to not be null.
     */
    public C removeAt(long pos, Consumer<Grid<C>> split) {
        Objects.requireNonNull(split);

        if (!contains(pos)) {
            throw new IllegalArgumentException("Grid::remove: Tried to call with a position that does not exist within the grid.");
        }

        if (isExternal(pos)) {
            return removeFinal(pos, null);
        }

        ObjectList<LongLinkedOpenHashSet> colored = new ObjectArrayList<>();

        int bestColor = divider.divide(
            removed -> removed.add(pos),
            roots -> {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    long face = position.offset(direction).get();

                    if (linked(pos, direction, face)) {
                        roots.add(face);
                    }
                }
            },
            colored::add
        );

        LongSet check = new LongLinkedOpenHashSet();

        for (int i = 0; i < colored.size(); i++) {
            if (i == bestColor) {
                // These nodes will be kept.
                continue;
            }

            Grid<C> newGrid = new Grid<>();
            LongSet found = colored.get(i);

            for (long reached : found) {
                byte connectivity = nodes.get(reached);

                if (connectivity != Byte.MAX_VALUE) {
                    check.add(reached);
                    newGrid.nodes.put(reached, connectivity, nodes.getListeners().get(reached));
                } else {
                    newGrid.connectors.put(reached, connectors.remove(reached));
                }

            }

            newGrid.nodes.update();

            split.accept(newGrid);
        }

        return removeFinal(pos, check);
    }

    /**
     * Removes connector by a position.
     *
     * @param pos The given position.
     * @param found The set with nodes to check.
     * @return The removed connector.
     */
    private C removeFinal(long pos, LongSet found) {
        C connector = connectors.remove(pos).value();

        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long face = position.offset(direction).get();

            if (nodes.containsKey(face) && isExternal(face)) {
                nodes.remove(face);
            }
        }

        if (found != null) {
            for (long reached : found) {
                if (isExternal(reached)) {
                    nodes.remove(reached);
                }
            }
        }

        nodes.update();
        return connector;
    }

    /**
     * Tests if a particular position is only connected to the grid on a single side, or is the only entry in the grid.
     *
     * @param pos The position to test.
     * @return Whether the position only has a single neighbor in the group, or is the only entry in the group.
     */
    private boolean isExternal(long pos) {
        // If the grid contains less than 2 blocks, neighbors cannot exist.
        if (countConnectors() <= 1) {
            return true;
        }

        byte neighbors = 0;
        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long face = position.offset(direction).get();

            if (!nodes.containsKey(face) && linked(pos, direction, face)) {
                neighbors += 1;
            }
        }

        return neighbors <= 1;
    }

    /**
     * @apiNote Wrapper for a Long2ByteMap class and listener for updates.
     */
    private static class Listener {

        private Long2ByteMap map;
        private Long2ObjectMap<IController> listeners;

        /**
         * Constructs a new Long2ByteMap with the same mappings as the specified Map.
         */
        Listener() {
            map = new Long2ByteLinkedOpenHashMap();
            map.defaultReturnValue(Byte.MAX_VALUE);
            listeners = new Long2ObjectLinkedOpenHashMap<>();
        }

        /**
         * Returns true if this map contains a mapping for the specified key.
         *
         * @param key The key value.
         * @return True or false.
         */
        boolean containsKey(long key) {
            return map.containsKey(key);
        }

        /**
         * @return Gets the value to which the specified key is mapped, or null if this map contains no mapping for the key.
         */
        byte get(long key) {
            return map.get(key);
        }

        /**
         * Associates the specified value with the specified key in this map.
         * @param key The key value.
         * @param value The provided value.
         * @param listener The listener function.
         */
        void put(long key, byte value, IController listener) {
            map.put(key, value);
            listeners.put(key, listener);
        }

        /**
         * Copies all of the mappings from the specified map to this map.
         *
         * @param wrapper The other object.
         */
        void putAll(Listener wrapper) {
            map.putAll(wrapper.map);
            listeners.putAll(wrapper.listeners);
        }

        /**
         * @param key The key value.
         */
        void remove(long key) {
            byte value = map.remove(key);
            listeners.remove(key);
        }

        /**
         * @return Gets map size.
         */
        int size() {
            return map.size();
        }

        /**
         * @return Gets keys set.
         */
        LongSet keySet() {
            return map.keySet();
        }

        /**
         * @return Gets original map.
         */
        Long2ByteMap getMap() {
            return map;
        }

        /**
         * @return Gets listeners map.
         */
        Long2ObjectMap<IController> getListeners() {
            return listeners;
        }

        /**
         * Call attached listeners.
         */
        void update() {
            boolean primary = true;
            for (IController listener : listeners.values()) {
                listener.change(primary); primary = false;
            }
        }
    }
}