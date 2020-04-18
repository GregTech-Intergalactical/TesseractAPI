package tesseract.graph;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.util.*;
import tesseract.graph.traverse.ASFinder;
import tesseract.graph.traverse.BFDivider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Grid provides the functionality of a set of linked nodes.
 */
public class Grid<C extends IConnectable> implements INode {

    private final Long2ObjectMap<Cache<C>> connectors = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2ByteLinkedOpenHashMap nodes = new Long2ByteLinkedOpenHashMap();
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
    @Nonnull
    protected static <C extends IConnectable> Grid<C> singleConnector(long pos, @Nonnull Cache<C> connector) {
        Grid<C> grid = new Grid<>();
        grid.connectors.put(pos, connector);
        return grid;
    }

    @Override
    public boolean contains(long pos) {
        return connectors.containsKey(pos) || nodes.containsKey(pos);
    }

    @Override
    public boolean linked(long from, @Nullable Dir towards, long to) {
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

        return validLink && Connectivity.has(connectivityFrom, Objects.requireNonNull(towards)) && Connectivity.has(connectivityTo, towards.invert());
    }

    @Override
    public boolean connects(long pos, @Nullable Dir towards) {
        Cache<C> cache = connectors.get(pos);
        byte connectivity = nodes.get(pos);

        if (cache != null) {
            connectivity = cache.connectivity();
        }

        if (connectivity == Byte.MAX_VALUE) {
            return false;
        }

        return Connectivity.has(connectivity, Objects.requireNonNull(towards));
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
    @Nonnull
    public Long2ObjectMap<Cache<C>> getConnectors() {
        return Long2ObjectMaps.unmodifiable(connectors);
    }

    /**
     * @return Returns nodes map.
     */
    @Nonnull
    public Long2ByteMap getNodes() {
        return Long2ByteMaps.unmodifiable(nodes);
    }

    /**
     * Gets paths from the position to another linked nodes.
     *
     * @param from The position of the linked node.
     * @return Returns paths from the linked node.
     */
    @Nonnull
    public ObjectList<Path<C>> getPaths(long from) {
        ObjectList<Path<C>> data = new ObjectArrayList<>();

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
    @Nonnull
    public ArrayDeque<Node> getPath(long origin, long target) {
        return finder.traverse(origin, target);
    }

    /**
     * Merges all of the elements from the other provided grid into this grid.
     *
     * @param other The other grid to merge elements from.
     */
    public void mergeWith(@Nonnull Grid<C> other) {
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
    public void addConnector(long pos, @Nonnull Cache<C> connector) {
        connectors.put(pos, connector);
    }

    /**
     * Adds a new node to the grid.
     *
     * @param pos The given position.
     * @param node The given node.
     */
    public void addNode(long pos, @Nonnull Cache<?> node) {
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
    public void removeAt(final long pos, @Nonnull Consumer<Grid<C>> split) {

        if (!contains(pos)) {
            throw new IllegalArgumentException("Grid::remove: Tried to call with a position that does not exist within the grid.");
        }

        if (isExternal(pos)) {
            removeFinal(pos, null);
            return;
        }

        ObjectList<LongLinkedOpenHashSet> colored = new ObjectArrayList<>();

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

        removeFinal(pos, check);
    }

    /**
     * Removes connector by a position.
     *
     * @param pos The given position.
     * @param found The set with nodes to check.
     */
    private void removeFinal(long pos, @Nullable LongSet found) {
        connectors.remove(pos);

        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long side = position.offset(direction).asLong();

            if (nodes.containsKey(side) && isExternal(side)) {
                nodes.remove(side);
            }
        }

        if (found == null) {
            return;
        }

        for (long reached : found) {
            if (isExternal(reached)) {
                nodes.remove(reached);
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

        byte neighbors = 0;
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