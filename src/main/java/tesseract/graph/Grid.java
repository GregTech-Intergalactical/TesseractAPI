package tesseract.graph;

import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import tesseract.graph.traverse.BFDivider;
import tesseract.graph.traverse.INodeContainer;
import tesseract.graph.visit.VisitableGrid;
import tesseract.util.Dir;
import tesseract.util.Pos;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// default: parameters are nonnull, methods return nonnull
public class Grid<C extends IConnectable> implements INodeContainer, VisitableGrid<C> {

    private HashMap<Pos, Connectivity.Cache<C>> connectors;
    private Object2ByteOpenHashMap<Pos> linkedNodes;
    private BFDivider divider;

    private Grid() {
        connectors = new HashMap<>();
        linkedNodes = new Object2ByteOpenHashMap<>();
        linkedNodes.defaultReturnValue(Byte.MAX_VALUE);
        divider = new BFDivider(this);
    }

    public static <C extends IConnectable> Grid<C> singleConnector(Pos pos, Connectivity.Cache<C> connector) {
        Grid<C> grid = new Grid<>();

        grid.connectors.put(Objects.requireNonNull(pos), Objects.requireNonNull(connector));

        return grid;
    }

    @Override
    public boolean contains(Pos pos) {
        return connectors.containsKey(pos) || linkedNodes.containsKey(pos);
    }

    @Override
    public boolean linked(Pos from, Dir towards, Pos to) {
        Connectivity.Cache<C> cacheFrom = connectors.get(from);
        Connectivity.Cache<C> cacheTo = connectors.get(to);

        byte connectivityFrom = linkedNodes.getByte(from);
        byte connectivityTo = linkedNodes.getByte(to);

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
        byte connectivity = linkedNodes.getByte(position);

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
    public void visitConnectors(BiConsumer<Pos, C> visitor) {
        for (Map.Entry<Pos, Connectivity.Cache<C>> entry : connectors.entrySet()) {
            visitor.accept(entry.getKey(), entry.getValue().value());
        }
    }

    /**
     * Merges all of the elements from the other provided grid into this grid.
     *
     * @param other The other grid to merge elements from
     */
    void mergeWith(Pos at, Grid<C> other) {
        // TODO: Validate that the other grid touches the specified position.

        connectors.putAll(other.connectors);
        linkedNodes.putAll(other.linkedNodes);
    }

    /**
     * Gets a potentially random position from the grid.
     *
     * @return A random position from the grid
     */
    Pos sampleConnector() {
        return connectors.keySet().iterator().next();
    }

    public void addConnector(Pos pos, Connectivity.Cache<C> connector) {
        // TODO: Validate that the other grid touches the specified position.

        connectors.put(Objects.requireNonNull(pos), Objects.requireNonNull(connector));
    }

    public void addLinkedNode(Pos pos, byte connectivity) {
        linkedNodes.put(Objects.requireNonNull(pos), connectivity);
    }

    public C remove(Pos pos, Consumer<Grid<C>> split) {
        Objects.requireNonNull(split);

        if (!contains(pos)) {
            throw new IllegalArgumentException("Tried to call Grid::remove with a position that does not exist within the grid.");
        }

        if (isExternal(pos)) {
            return removeFinal(pos);
        }

        ArrayList<HashSet<Pos>> colored = new ArrayList<>();

        int bestColor = divider.divide(
            removed -> removed.add(pos),
            roots -> {
                for (Dir direction : Dir.VALUES) {
                    Pos side = pos.offset(direction);

                    if (this.linked(pos, direction, side)) {
                        roots.add(side);
                    }
                }
            },
            colored::add
        );

        // TODO: Properly split / remove relevant linkedNodes, verify that this works.
        HashSet<Pos> check = new HashSet<>();

        for (int i = 0; i < colored.size(); i++) {
            if (i == bestColor) {
                // These nodes will be kept.
                continue;
            }

            Grid<C> newGrid = new Grid<>();
            HashSet<Pos> found = colored.get(i);

            for (Pos reached : found) {
                byte connectivity = linkedNodes.getByte(reached);

                if (connectivity != Byte.MAX_VALUE) {
                    check.add(reached);
                    newGrid.linkedNodes.put(reached, connectivity);
                } else {
                    newGrid.connectors.put(reached, this.connectors.remove(reached));
                }
            }

            split.accept(newGrid);
        }

        C connector = removeFinal(pos);

        for (Pos toCheck : check) {
            if (isExternal(toCheck)) {
                linkedNodes.removeByte(toCheck);
            }
        }

        return connector;
    }

    private C removeFinal(Pos pos) {
        C connector = this.connectors.remove(pos).value();

        for (Dir direction : Dir.VALUES) {
            Pos face = pos.offset(direction);

            if (linkedNodes.containsKey(face) && isExternal(face)) {
                linkedNodes.removeByte(face);
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
        if (this.connectors.size() <= 1) {
            return true;
        }

        int neighbors = 0;
        for (Dir direction : Dir.VALUES) {
            Pos face = pos.offset(direction);

            if (!linkedNodes.containsKey(face) && this.linked(pos, direction, face)) {
                neighbors += 1;
            }
        }

        return neighbors <= 1;
    }
}