package tesseract.graph;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.util.*;
import tesseract.graph.traverse.ASFinder;
import tesseract.graph.traverse.BFDivider;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Grid provides the functionality of a set of linked nodes.
 * @apiNote default parameters are nonnull, methods return nonnull.
 */
public class Grid<C extends IConnectable> implements INode {

    private Long2ObjectMap<Connectivity.Cache<C>> connectors;
    private Long2ByteMap nodes; // linked nodes
    private BFDivider divider;
    private ASFinder finder;

    // Prevent the creation of empty grids externally, a caller needs to use singleConnector.
    private Grid() {
        connectors = new Long2ObjectLinkedOpenHashMap<>();
        nodes = new Long2ByteLinkedOpenHashMap();
        nodes.defaultReturnValue(Byte.MAX_VALUE);

        divider = new BFDivider(this);
        finder = new ASFinder(this);
    }

    /**
     * @param pos The position of the connector.
     * @param connector The given connector.
     * @return Create a instance of a class for a given position and connector.
     */
    public static <C extends IConnectable> Grid<C> singleConnector(long pos, Connectivity.Cache<C> connector) {
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

    // TODO: Count/visit linked nodes

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
        return nodes;
    }

    /**
     * Lazily generates full paths from the linked node to another linked nodes.
     *
     * @param pos The position of the linked node.
     * @return Returns full paths for the linked node.
     */
    public ObjectSet<Path<C>> getPath(long pos) {

        ObjectSet<Path<C>> data = new ObjectLinkedOpenHashSet<>();
        for (long target : nodes.keySet()) {
            if (pos != target) {
                data.add(new Path<>(connectors, finder.find(pos, target)));
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
    public ArrayDeque<Node> findPath(long start, long end) {
        return finder.find(start, end);
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
    }

    /**
     * Adds a new node to the grid.
     *
     * @param pos The given position.
     * @param connectivity The connectivity state.
     */
    public void addNode(long pos, byte connectivity) {
        nodes.put(pos, connectivity);
    }

    /**
     * Removes the node from the grid.
     *
     * @param pos The given position.
     */
    public void removeNode(long pos) {
        nodes.remove(pos);
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
            return removeFinal(pos);
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

        // TODO: Properly split / remove relevant nodes, verify that this works.
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
                    newGrid.nodes.put(reached, connectivity);
                } else {
                    newGrid.connectors.put(reached, connectors.remove(reached));
                }
            }

            split.accept(newGrid);
        }

        // TODO: Why is it here?
        C connector = removeFinal(pos);

        for (long toCheck : check) {
            if (isExternal(toCheck)) {
                nodes.remove(toCheck);
            }
        }

        return connector;
    }

    /**
     * Removes connector by a position.
     *
     * @param pos The given position.
     * @return The removed connector.
     */
    private C removeFinal(long pos) {
        C connector = connectors.remove(pos).value();

        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long face = position.offset(direction).get();

            if (nodes.containsKey(face) && isExternal(face)) {
                nodes.remove(face);
            }
        }

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
        if (connectors.size() <= 1) {
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
     * The Path is a class that should work with paths for grids.
     */
    public static class Path<C extends IConnectable> {

        private Pos origin;
        private Pos target;
        private ObjectList<C> full;
        private ObjectList<C> cross;

        /**
         * Create a path instance.
         *
         * @param connectors The connectors array.
         * @param path The path queue.
         */
        private Path(Long2ObjectMap<Connectivity.Cache<C>> connectors, ArrayDeque<Node> path) {
            origin = path.pollLast();
            target = path.pollFirst();

            full = new ObjectArrayList<>();
            cross = new ObjectArrayList<>();

            Iterator<Node> iterator = path.descendingIterator();
            while (iterator.hasNext()) {
                Node node = iterator.next();
                C cable = connectors.get(node.get()).value();
                full.add(cable);
                if (node.isCrossroad()) {
                    cross.add(cable);
                }
            }

            //hash = ID.getNewHash();
        }

        /**
         * @return Gets the origin position.
         */
        public Pos origin() {
            return origin;
        }

        /**
         * @return Gets the target position.
         */
        public Pos target() {
            return target;
        }

        /**
         * @return Gets the full connectors path.
         */
        public ObjectList<C> getFull() {
            return full;
        }

        /**
         * @return Gets the crossroad connectors path.
         */
        public ObjectList<C> getCross() {
            return cross;
        }
    }
}