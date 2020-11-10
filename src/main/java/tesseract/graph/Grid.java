package tesseract.graph;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.IConnectable;
import tesseract.util.*;
import tesseract.graph.traverse.ASFinder;
import tesseract.graph.traverse.BFDivider;

import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * Grid provides the functionality of a set of linked nodes.
 */
public class Grid<C extends IConnectable> implements INode {

    private final Long2ObjectMap<Cache<C>> connectors = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2ByteMap nodes = new Long2ByteLinkedOpenHashMap();
    private final BFDivider divider = new BFDivider(this);
    private final ASFinder finder = new ASFinder(this);

    // Prevent the creation of empty grids externally, a caller needs to use singleConnector.
    private Grid() {
        nodes.defaultReturnValue(Byte.MAX_VALUE);
    }

    /**
     * @param pos The position of the connector.
     * @param connector The given connector.
     * @return Create a instance of a class for a given position and connector.
     */
    protected static <C extends IConnectable> Grid<C> singleConnector(long pos, Cache<C> connector) {
        Grid<C> grid = new Grid<>();
        grid.connectors.put(pos, connector);
        return grid;
    }

    @Override
    public boolean contains(long pos) {
        return connectors.containsKey(pos) || nodes.containsKey(pos);
    }

    @Override
    public boolean linked(long from, Dir towards, long to) {
        assert towards != null;

        Cache<C> cacheFrom = connectors.get(from);
        Cache<C> cacheTo = connectors.get(to);

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

        return validLink && Connectivity.has(connectivityFrom, towards.getIndex()) && Connectivity.has(connectivityTo, towards.getOpposite().getIndex());
    }

    @Override
    public boolean connects(long pos, Dir towards) {
        assert towards != null;

        Cache<C> cache = connectors.get(pos);
        byte connectivity = nodes.get(pos);

        if (cache != null) {
            connectivity = cache.connectivity();
        }

        if (connectivity == Byte.MAX_VALUE) {
            return false;
        }

        return Connectivity.has(connectivity, towards.getIndex());
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
    public Long2ObjectMap<Cache<C>> getConnectors() {
        return connectors;
    }

    /**
     * @return Returns nodes map.
     */
    public Long2ByteMap getNodes() {
        return nodes;
    }

    /**
     * Gets paths from the position to another linked nodes.
     *
     * @param from The position of the linked node.
     * @return Returns paths from the linked node.
     */
    public List<Path<C>> getPaths(long from) {
        List<Path<C>> data = new ObjectArrayList<>();

        for (long to : nodes.keySet()) {
            if (from != to) {
                data.add(new Path<>(connectors, finder.traverse(from, to)));
            }
        }

        return data;
    }

    /**
     * Begins a find operation from the specified start position to the end position.
     *
     * @param origin The start position of the traverse operation.
     * @param target The end position of the traverse operation.
     * @return An set of path points.
     */
    public Deque<Node> getPath(long origin, long target) {
        return finder.traverse(origin, target);
    }

    /**
     * Merges all of the elements from the other provided grid into this grid.
     *
     * @param other The other grid to merge elements from.
     */
    public void mergeWith(Grid<C> other) {
        connectors.putAll(other.connectors);
        nodes.putAll(other.nodes);
    }

    /**
     * Gets a potentially random position from the grid.
     *
     * @return A random position from the grid.
     */
    public long sampleConnector() {
        LongIterator it = connectors.keySet().iterator();
        return it.hasNext() ? it.nextLong() : Long.MAX_VALUE;
    }

    /**
     * Adds a new connector to the grid.
     *
     * @param pos The given position.
     * @param connector The given connector.
     */
    public void addConnector(long pos, Cache<C> connector) {
        connectors.put(pos, connector);
    }

    /**
     * Adds a new node to the grid.
     *
     * @param pos The given position.
     * @param node The given node.
     */
    public void addNode(long pos, Cache<?> node) {
        nodes.put(pos, node.connectivity());
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
     */
    public void removeAt(long pos, Consumer<Grid<C>> split) {

        if (!contains(pos)) {
            throw new IllegalArgumentException("Grid::remove: Tried to call with a position that does not exist within the grid.");
        }

        if (isExternal(pos)) {
            removeFinal(pos);
            return;
        }

        List<LongSet> colored = new ObjectArrayList<>();

        int bestColor = divider.divide(
            removed -> removed.add(pos),
            roots -> {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    long side = position.offset(direction).asLong();

                    if (linked(pos, direction, side)) {
                        roots.add(side);
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
                    newGrid.nodes.put(reached, connectivity);
                } else {
                    newGrid.connectors.put(reached, connectors.remove(reached));
                }
            }
            split.accept(newGrid);
        }

        removeFinal(pos);

        for (long reached : check) {
            if (isExternal(reached)) {
                nodes.remove(reached);
            }
        }
    }

    /**
     * Removes connector by a position.
     *
     * @param pos The given position.
     */
    private void removeFinal(long pos) {
        connectors.remove(pos);

        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long side = position.offset(direction).asLong();

            if (nodes.containsKey(side) && isExternal(side)) {
                nodes.remove(side);
            }
        }
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

        int neighbors = 0;
        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long side = position.offset(direction).asLong();

            if (!nodes.containsKey(side) && linked(pos, direction, side)) {
                neighbors++;
            }
        }

        return neighbors <= 1;
    }
}