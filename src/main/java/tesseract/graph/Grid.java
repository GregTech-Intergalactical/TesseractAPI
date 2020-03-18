package tesseract.graph;

import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import tesseract.graph.traverse.ASFinder;
import tesseract.graph.traverse.BFDivider;
import tesseract.util.Dir;
import tesseract.util.Pos;

import java.util.*;
import java.util.function.Consumer;

/**
 * Grid provides the functionality of a set of linked nodes.
 * @apiNote default parameters are nonnull, methods return nonnull.
 */
public class Grid<C extends IConnectable> implements INode, IGrid<C> {

    private HashMap<Pos, Connectivity.Cache<C>> connectors;
    private Object2ByteOpenHashMap<Pos> nodes; // linked nodes
    private BFDivider divider;
    private ASFinder finder;

    // Prevent the creation of empty grids externally, a caller needs to use singleConnector.
    private Grid() {
        connectors = new HashMap<>();
        nodes = new Object2ByteOpenHashMap<>();
        nodes.defaultReturnValue(Byte.MAX_VALUE);
        divider = new BFDivider(this);
        finder = new ASFinder(this);
    }

    /**
     * Create a instance of a class for a given position and connector.
     * @param pos
     * @param connector
     * @return
     */
    public static <C extends IConnectable> Grid<C> singleConnector(Pos pos, Connectivity.Cache<C> connector) {
        Grid<C> grid = new Grid<>();
        grid.connectors.put(Objects.requireNonNull(pos), Objects.requireNonNull(connector));
        return grid;
    }

    @Override
    public boolean contains(Pos pos) {
        return connectors.containsKey(pos) || nodes.containsKey(pos);
    }

    @Override
    public boolean linked(Pos from, Dir towards, Pos to) {
        Connectivity.Cache<C> cacheFrom = connectors.get(from);
        Connectivity.Cache<C> cacheTo = connectors.get(to);

        byte connectivityFrom = nodes.getByte(from);
        byte connectivityTo = nodes.getByte(to);

        boolean validLink = false;

        if (cacheFrom != null) {
            validLink = true;
            connectivityFrom = cacheFrom.connectivity;
        }

        if (cacheTo != null) {
            validLink = true;
            connectivityTo = cacheTo.connectivity;
        }

        if (connectivityFrom == Byte.MAX_VALUE || connectivityTo == Byte.MAX_VALUE) {
            return false;
        }

        return validLink && Connectivity.has(connectivityFrom, towards) && Connectivity.has(connectivityTo, towards.invert());
    }

    @Override
    public boolean connects(Pos position, Dir towards) {
        Connectivity.Cache<C> cache = connectors.get(position);
        byte connectivity = nodes.getByte(position);

        if (cache != null) {
            connectivity = cache.connectivity;
        }

        if (connectivity == Byte.MAX_VALUE) {
            return false;
        }

        return Connectivity.has(connectivity, towards);
    }

    // TODO: Count / visit linked nodes

    @Override
    public int countConnectors() {
        return connectors.size();
    }

    @Override
    public HashMap<Pos, C> getConnectors() {
        HashMap<Pos, C> map = new HashMap<>();
        for (Map.Entry<Pos, Connectivity.Cache<C>> entry : connectors.entrySet()) {
            map.put(entry.getKey(), entry.getValue().value());
        }
        return map;
    }

    @Override
    public LinkedHashSet<Pos> getPath(Pos start, Pos end, boolean crossroad) {
        return finder.find(start, end, crossroad);
    }

    /**
     * Merges all of the elements from the other provided grid into this grid.
     *
     * @param other The other grid to merge elements from
     */
    void mergeWith(Pos at, Grid<C> other) {
        // TODO: Validate that the other grid touches the specified position.
        connectors.putAll(other.connectors);
        nodes.putAll(other.nodes);
    }

    /**
     * Gets a potentially random position from the grid.
     *
     * @return A random position from the grid
     */
    Pos sampleConnector() {
        Iterator<Pos> iterator = connectors.keySet().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     *
     * @param pos
     * @param connector
     */
    public void addConnector(Pos pos, Connectivity.Cache<C> connector) {
        // TODO: Validate that the other grid touches the specified position.
        connectors.put(Objects.requireNonNull(pos), Objects.requireNonNull(connector));
    }

    /**
     *
     * @param pos
     * @param connectivity
     */
    public void addLinkedNode(Pos pos, byte connectivity) {
        nodes.put(Objects.requireNonNull(pos), connectivity);
    }

    /**
     *
     * @param pos
     * @param split
     * @return
     */
    public C remove(Pos pos, Consumer<Grid<C>> split) {
        Objects.requireNonNull(split);

        if (!contains(pos)) {
            throw new IllegalArgumentException("Tried to call Grid::remove with a position that does not exist within the grid.");
        }

        if (isExternal(pos)) {
            return removeFinal(pos);
        }

        ArrayList<LinkedHashSet<Pos>> colored = new ArrayList<>();

        int bestColor = divider.divide(
            removed -> removed.add(pos),
            roots -> {
                for (Dir direction : Dir.VALUES) {
                    Pos face = pos.offset(direction);

                    if (linked(pos, direction, face)) {
                        roots.add(face);
                    }
                }
            },
            colored::add
        );

        // TODO: Properly split / remove relevant nodes, verify that this works.
        LinkedHashSet<Pos> check = new LinkedHashSet<>();

        for (int i = 0; i < colored.size(); i++) {
            if (i == bestColor) {
                // These nodes will be kept.
                continue;
            }

            Grid<C> newGrid = new Grid<>();
            LinkedHashSet<Pos> found = colored.get(i);

            for (Pos reached : found) {
                byte connectivity = nodes.getByte(reached);

                if (connectivity != Byte.MAX_VALUE) {
                    check.add(reached);
                    newGrid.nodes.put(reached, connectivity);
                } else {
                    newGrid.connectors.put(reached, connectors.remove(reached));
                }
            }

            split.accept(newGrid);
        }

        C connector = removeFinal(pos);

        for (Pos toCheck : check) {
            if (isExternal(toCheck)) {
                nodes.removeByte(toCheck);
            }
        }

        return connector;
    }

    /**
     *
     * @param pos
     * @return
     */
    private C removeFinal(Pos pos) {
        C connector = connectors.remove(pos).value();

        for (Dir direction : Dir.VALUES) {
            Pos face = pos.offset(direction);

            if (nodes.containsKey(face) && isExternal(face)) {
                nodes.removeByte(face);
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
    private boolean isExternal(Pos pos) {
        // If the grid contains less than 2 blocks, neighbors cannot exist.
        if (connectors.size() <= 1) {
            return true;
        }

        int neighbors = 0;
        for (Dir direction : Dir.VALUES) {
            Pos face = pos.offset(direction);

            if (!nodes.containsKey(face) && linked(pos, direction, face)) {
                neighbors += 1;
            }
        }

        return neighbors <= 1;
    }
}