package tesseract.graph;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.util.listener.IListener;
import tesseract.util.listener.Long2ByteMapListener;
import tesseract.graph.traverse.ASFinder;
import tesseract.graph.traverse.BFDivider;
import tesseract.util.Dir;
import tesseract.util.Pos;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Grid provides the functionality of a set of linked nodes.
 * @apiNote default parameters are nonnull, methods return nonnull.
 */
public class Grid<C extends IConnectable> implements INode, IGrid<C> {

    private Long2ObjectMap<Connectivity.Cache<C>> connectors;
    //private Object2ObjectLinkedOpenHashMap<C, ObjectLinkedOpenHashSet<LongLinkedOpenHashSet>> pathes;
    private Long2ObjectMap<ObjectSet<LongSet>> pathes;
    private Long2ByteMapListener nodes; // linked nodes
    private BFDivider divider;
    private ASFinder finder;

    // Prevent the creation of empty grids externally, a caller needs to use singleConnector.
    private Grid() {
        connectors = new Long2ObjectLinkedOpenHashMap<>();
        pathes = new Long2ObjectLinkedOpenHashMap<>();
        nodes = new Long2ByteMapListener(new Long2ByteLinkedOpenHashMap(), listener);
        divider = new BFDivider(this);
        finder = new ASFinder(this);
    }

    /**
     * Create a instance of a class for a given position and connector.
     *
     * @param pos
     * @param connector
     * @return
     */
    public static <C extends IConnectable> Grid<C> singleConnector(long pos, Connectivity.Cache<C> connector) {
        Grid<C> grid = new Grid<>();
        grid.connectors.put(pos, Objects.requireNonNull(connector));
        return grid;
    }

    /**
     * Executes on any change on linked node list.
     */
    public IListener listener = () -> {

        pathes.clear();

        /*for (long origin : nodes.unwrap().keySet()) {
            ObjectSet<LongSet> path = new ObjectLinkedOpenHashSet<>();
            for (long target : nodes.unwrap().keySet()) {
                if (origin != target) {
                    path.add(finder.find(origin, target, true));
                }
            }
            pathes.put(origin, path);
        }*/
    };

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

    @Override
    public int countConnectors() {
        return connectors.size();
    }

    @Override
    public int countNodes() {
        return nodes.unwrap().size();
    }

    @Override
    public Long2ObjectMap<Connectivity.Cache<C>> getConnectors() {
        return connectors;
    }

    @Override
    public Long2ByteMap getNodes() {
        return nodes.unwrap();
    }

    @Override
    public Long2ObjectMap<ObjectSet<LongSet>> getPathes() {
        return pathes;
    }

    @Override
    public LongSet findPath(long start, long end, boolean crossroad) {
        return finder.find(start, end, crossroad);
    }

    /**
     * Merges all of the elements from the other provided grid into this grid.
     *
     * @param other The other grid to merge elements from
     */
    public void mergeWith(long at, Grid<C> other) {
        // TODO: Validate that the other grid touches the specified position.
        connectors.putAll(other.connectors);
        nodes.putAll(other.nodes);
    }

    /**
     * Gets a potentially random position from the grid.
     *
     * @return A random position from the grid
     */
    public long sampleConnector() {
        LongIterator iterator = connectors.keySet().iterator();
        return iterator.hasNext() ? iterator.nextLong() : Long.MAX_VALUE;
    }

    /**
     *
     * @param pos
     * @param connector
     */
    public void addConnector(long pos, Connectivity.Cache<C> connector) {
        // TODO: Validate that the other grid touches the specified position.
        connectors.put(pos, Objects.requireNonNull(connector));
    }

    /**
     *
     * @param pos
     * @param connectivity
     */
    public void addNode(long pos, byte connectivity) {
        nodes.put(pos, connectivity);
    }

    /**
     *
     * @param key
     */
    public void removeNode(long key) {
        nodes.remove(key);
    }

    /**
     *
     * @param pos
     * @param split
     * @return
     */
    public C remove(long pos, Consumer<Grid<C>> split) {
        Objects.requireNonNull(split);

        if (!contains(pos)) {
            throw new IllegalArgumentException("Tried to call Grid::remove with a position that does not exist within the grid.");
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
     *
     * @param pos
     * @return
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
     * @param pos The position to test
     * @return Whether the position only has a single neighbor in the group, or is the only entry in the group.
     */
    private boolean isExternal(long pos) {
        // If the grid contains less than 2 blocks, neighbors cannot exist.
        if (connectors.size() <= 1) {
            return true;
        }

        int neighbors = 0;
        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long face = position.offset(direction).get();

            if (!nodes.containsKey(face) && linked(pos, direction, face)) {
                neighbors += 1;
            }
        }

        return neighbors <= 1;
    }
}