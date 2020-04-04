package tesseract.graph;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.IConnectable;
import tesseract.graph.traverse.BFDivider;
import tesseract.util.Dir;
import tesseract.util.Pos;
import tesseract.util.Utils;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Group provides the functionality of a set of adjacent nodes that may or may not be linked.
 * @apiNote default parameters are nonnull, methods return nonnull.
 */
public class Group<C extends IConnectable, N extends IConnectable> implements INode {

    private Long2ObjectMap<Connectivity.Cache<N>> nodes;
    private Int2ObjectMap<Grid<C>> grids;
    private Long2IntMap connectors; // connectors pairing

    private BFDivider divider;

    // Prevent the creation of empty groups externally, a caller needs to use singleNode/singleConnector.
    private Group() {
        nodes = new Long2ObjectLinkedOpenHashMap<>();
        grids = new Int2ObjectLinkedOpenHashMap<>();
        connectors = new Long2IntLinkedOpenHashMap();
        connectors.defaultReturnValue(Utils.INVALID);

        divider = new BFDivider(this);
    }

    /**
     * @param at The position of the node.
     * @param node The given node.
     * @return Create a instance of a class for a given position and node.
     */
    protected static <C extends IConnectable, N extends IConnectable> Group<C, N> singleNode(long at, Connectivity.Cache<N> node) {
        Group<C, N> group = new Group<>();
        group.addNode(at, node);
        return group;
    }

    /**
     * @param at The position of the connector.
     * @param connector The given connector.
     * @return Create a instance of a class for a given position and connector.
     */
    protected static <C extends IConnectable, N extends IConnectable> Group<C, N> singleConnector(long at, Connectivity.Cache<C> connector) {
        Group<C, N> group = new Group<>();
        int id = Utils.getNewId();
        group.connectors.put(at, id);
        group.grids.put(id, Grid.singleConnector(at, connector));
        return group;
    }

    @Override
    public boolean contains(long at) {
        return nodes.containsKey(at) || connectors.containsKey(at);
    }

    @Override
    public boolean linked(long from, Dir towards, long to) {
        return contains(from) && contains(to);
    }

    @Override
    public boolean connects(long position, Dir towards) {
        return contains(position);
    }

    /**
     * @return Gets the number of blocks.
     */
    public int countBlocks() {
        return nodes.size() + connectors.size();
    }

    /**
     * @return Returns blocks set.
     */
    public LongList getBlocks() { // LongSet before, to check
        LongList merge = new LongArrayList();
        merge.addAll(nodes.keySet());
        merge.addAll(connectors.keySet());
        return merge;
    }

    /**
     * @return Returns nodes map.
     */
    public Long2ObjectMap<Connectivity.Cache<N>> getNodes() {
        return nodes;
    }

    /**
     * @return Returns grids set.
     */
    public Int2ObjectMap<Grid<C>> getGrids() {
        return grids;
    }

    /**
     * Adds a new node to the group.
     *
     * @param at The given position.
     * @param node The given node.
     */
    public void addNode(long at, Connectivity.Cache<N> node) {
        nodes.put(at, Objects.requireNonNull(node));

        Pos position = new Pos(at);
        for (Dir direction : Dir.VALUES) {
            if (!node.connects(direction)) {
                continue;
            }

            long side = position.offset(direction).get();
            int id = connectors.get(side);

            if (id != Utils.INVALID) {
                Grid<C> grid = grids.get(id);
                side = position.offset(direction).get();

                if (grid.connects(side, direction.invert())) {
                    grid.addNode(at, node);
                }
            } else {

                if (nodes.containsKey(side)) {
                    boolean found = false;
                    for (Grid<C> grid : grids.values()) {
                        if (grid.contains(side)) {
                            grid.update();
                            found = true;
                        }
                    }

                    /*IListener listener = nodes.get(side).listener();
                    if (listener != null) {
                        listener.change(!found);
                    }*/
                }
            }
        }
    }

    /**
     * Adds a new connector to the group.
     *
     * @param at The given position.
     * @param connector The given connector.
     */
    public void addConnector(long at, Connectivity.Cache<C> connector) {
        Objects.requireNonNull(connector);

        Int2ObjectMap<Grid<C>> linked = new Int2ObjectLinkedOpenHashMap<>();
        Long2ObjectMap<Dir> joined = new Long2ObjectLinkedOpenHashMap<>();
        Grid<C> bestGrid = null;
        int bestCount = 0;
        int bestId = Utils.INVALID;

        byte neighbors = 0;
        Pos position = new Pos(at);
        for (Dir direction : Dir.VALUES) {
            if (!connector.connects(direction)) {
                continue;
            }

            long side = position.offset(direction).get();
            int id = connectors.get(side);

            if (id == Utils.INVALID) {
                // Collect joining nodes
                if (nodes.containsKey(side)) {
                    neighbors += 1;
                    joined.put(side, direction);
                }
                continue;
            }

            neighbors += 1;

            Grid<C> grid = grids.get(id);

            if (grid.connects(side, direction.invert())) {
                linked.put(id, grid);

                if (grid.countConnectors() > bestCount) {
                    bestCount = grid.countConnectors();
                    bestGrid = grid;
                    bestId = id;
                }
            }
        }

        if (neighbors == 0) {
            throw new IllegalStateException("Group::addConnector: Attempted to add a position that would not be touching the group!");
        }

        if (linked.isEmpty()) {
            // Single connector grid
            bestId = Utils.getNewId();
            bestGrid = Grid.singleConnector(at, connector);

            connectors.put(at, bestId);
            grids.put(bestId, bestGrid);
            bestCount = -1; // For exit
        }

        if (bestGrid == null) {
            throw new IllegalStateException();
        }

        // Add neighbours nodes to the grid
        for (Long2ObjectMap.Entry<Dir> entry : joined.long2ObjectEntrySet()) {
            long pos = entry.getLongKey();
            Dir direction = entry.getValue();

            Connectivity.Cache<N> node = nodes.get(pos);

            if (node.connects(direction.invert())) {
                bestGrid.addNode(pos, node);
            }
        }

        if (bestCount == -1) {
            // Grid was just initialized
            return;
        }

        // Add to the best grid
        connectors.put(at, bestId);
        bestGrid.addConnector(at, connector);

        if (linked.size() == 1) {
            // No other grids to merge with
            return;
        }

        for (Int2ObjectMap.Entry<Grid<C>> entry : linked.int2ObjectEntrySet()) {
            int id = entry.getIntKey();
            Grid<C> grid = entry.getValue();

            if (id == bestId) {
                continue;
            }

            bestGrid.mergeWith(grid);
            for (long item : grid.getConnectors().keySet()) {
                connectors.put(item, bestId);
            }
            grids.remove(id);
        }
    }

    /**
     * Removes an entry from the Group, potentially splitting it if needed. By calling this function, the caller asserts
     * that this group contains the specified position; the function may misbehave if the group does not actually contain
     * the specified position.
     *
     * @param posToRemove The position of the entry to remove.
     * @param split A consumer for the resulting fresh graphs from the split operation.
     * @return True on success, false otherwise.
     */
    public boolean remove(long posToRemove, Consumer<Group<C, N>> split) {
        Objects.requireNonNull(split);
        // The contains() check can be skipped here, because Graph will only call remove() if it knows that the group contains the entry.
        // For now, it is retained for completeness and debugging purposes.
        if (!contains(posToRemove)) {
            throw new IllegalArgumentException("Group::remove: Tried to call with a position that does not exist within the group.");
        }

        // If removing the entry would not cause a group split, then it is safe to remove the entry directly.
        if (isExternal(posToRemove)) {
            Connectivity.Cache<N> node = nodes.remove(posToRemove);
            int pairing = connectors.remove(posToRemove);

            if (node != null) {
                // Clear removing node from nearest grid
                for (int id : getNeighboringGrids(posToRemove)) {
                    grids.get(id).removeNode(posToRemove);
                }

                return true;
            }

            Grid<C> grid = grids.get(pairing);

            // No check is needed here, because the caller already asserts that the Group contains the specified position.
            // Thus, if this is not a node, then it is guaranteed to be a connector.
            boolean removed = grid.remove(
                posToRemove,
                newGrid -> {
                    int newId = Utils.getNewId();
                    grids.put(newId, newGrid);

                    for (long pos : newGrid.getConnectors().keySet()) {
                        connectors.put(pos, newId);
                    }
                }
            );

            // Avoid leaving empty grids within the grid list.
            if (grid.countConnectors() == 0) {
                grids.remove(pairing);
            }

            return removed;
        }

        // If none of the fast routes work, we need to due a full group-traversal to figure out how the graph will be split.
        // The algorithm works by "coloring" each fragment of the group based on what it is connected to, and then from this,
        // splitting each colored portion into its own separate group.

        // For optimization purposes, the largest colored fragment remains resident within its original group.
        // Note: we don't remove the node yet, but instead just tell the Searcher to exclude it.
        // This is so that we can handle the grid splits ourselves at the end.
        ObjectList<LongLinkedOpenHashSet> colored = new ObjectArrayList<>();

        int bestColor = divider.divide(
            removed -> removed.add(posToRemove),
            roots -> {
                Pos position = new Pos(posToRemove);
                for (Dir direction : Dir.VALUES) {
                    long side = position.offset(direction).get();

                    if (linked(posToRemove, direction, side)) {
                        roots.add(side);
                    }
                }
            },
            colored::add
        );

        ObjectList<Grid<C>> splitGrids = null;
        LongSet excluded = new LongLinkedOpenHashSet();

        int centerGridId = connectors.get(posToRemove);
        if (centerGridId != Utils.INVALID) {
            Grid<C> centerGrid = grids.remove(centerGridId);
            splitGrids = new ObjectArrayList<>();

            for (long move : centerGrid.getConnectors().keySet()) {
                connectors.remove(move);
                excluded.add(move);
            }

            centerGrid.remove(posToRemove, splitGrids::add);
            splitGrids.add(centerGrid);
        } else {
            // Clear removing node from nearest grid
            for (int id : getNeighboringGrids(posToRemove)) {
                grids.get(id).removeNode(posToRemove);
            }

            nodes.remove(posToRemove);
        }

        for (int i = 0; i < colored.size(); i++) {
            LongSet found = colored.get(i);
            Group<C, N> newGroup;

            if (i != bestColor) {
                newGroup = new Group<>();

                for (long reached : found) {
                    if (newGroup.connectors.containsKey(reached) || excluded.contains(reached)) {
                        continue;
                    }

                    int id = connectors.get(reached);

                    // Just a node then, simply add it to the new group.
                    // The maps are mutated directly here in order to retain the cached connectivity.
                    if (id == Utils.INVALID) {
                        newGroup.nodes.put(reached, Objects.requireNonNull(nodes.remove(reached)));
                        continue;
                    }

                    Grid<C> grid = grids.get(id);
                    if (grid.contains(posToRemove)) {
                        // This should be unreachable
                        throw new IllegalStateException("Group::remove: Searchable grid contains the removed position, the grid should have been removed already?!?");
                    }

                    // Move grid to new group
                    grids.remove(id);
                    newGroup.grids.put(id, grid);

                    for (long moved : grid.getConnectors().keySet()) {
                        connectors.remove(moved);
                        newGroup.connectors.put(moved, id);
                    }
                }
            } else {
                newGroup = this;
            }

            // Add the fragments of the center grid, if present, to each group
            if (splitGrids != null) {
                ObjectIterator<Grid<C>> iterator = splitGrids.iterator();

                while (iterator.hasNext()) {
                    Grid<C> grid = iterator.next();
                    long sample = grid.sampleConnector();

                    if (found.contains(sample)) {
                        int newId = Utils.getNewId();

                        newGroup.addGrid(newId, grid);
                        iterator.remove();
                    }
                }
            }

            if (i != bestColor) {
                split.accept(newGroup);
            }
        }

        return true;
    }

    /**
     * Adds a new grid to the group.
     *
     * @param id The group id.
     * @param grid The grid object.
     */
    private void addGrid(int id, Grid<C> grid) {
        grids.put(id, Objects.requireNonNull(grid));

        for (long moved : grid.getConnectors().keySet()) {
            connectors.put(moved, id);
        }
    }

    /**
     * Gets near grids by a given position and connectivity value.
     *
     * @param pos The position of the grid.
     * @param connectivity The connectivity sides.
     * @return The grid set, guaranteed to not be null.
     */
    public ObjectSet<Grid<C>> getGridsAt(long pos, byte connectivity) {
        ObjectSet<Grid<C>> neighbors = new ObjectLinkedOpenHashSet<>();

        int grid = connectors.get(pos);

        // If we found connector stop
        if (grid != Utils.INVALID) {
            neighbors.add(grids.get(grid));
            return neighbors; // exit here
        }

        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long side = position.offset(direction).get();
            int id = connectors.get(side);

            if (id != Utils.INVALID) {
                if (Connectivity.has(connectivity, direction)) {
                    neighbors.add(grids.get(id));
                }
            }
        }

        return neighbors;
    }

    /**
     * Tests if a particular position is only connected to the group on a single side, or is the only entry in the group.
     *
     * @param pos The position to test.
     * @return Whether the position only has a single neighbor in the group, or is the only entry in the group.
     */
    private boolean isExternal(long pos) {
        // If the group contains less than 2 blocks, neighbors cannot exist.
        if (countBlocks() <= 1) {
            return true;
        }

        byte neighbors = 0;
        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long side = position.offset(direction).get();

            if (contains(side)) {
                neighbors += 1;
            }
        }

        return neighbors <= 1;
    }

    // Graph controlled interface

    /**
     * Merges one group to the another.
     * @param other The another group.
     * @param at The given position.
     */
    public void mergeWith(Group<C, N> other, long at) {
        nodes.putAll(other.nodes);
        connectors.putAll(other.connectors);

        for (int id : other.grids.keySet()) {
            if (grids.containsKey(id)) {
                throw new IllegalStateException("Group::mergeWith: Duplicate grid UUIDs when attempting to merge groups, this should never happen!");
            }
        }

        int pairing = connectors.get(at);

        if (pairing != Utils.INVALID) {
            Grid<C> currentGrid = grids.get(pairing);

            Pos position = new Pos(at);
            for (Dir direction : Dir.VALUES) {
                long side = position.offset(direction).get();

                if (!currentGrid.connects(at, direction)) {
                    continue;
                }

                int id = other.connectors.get(side);

                if (id == Utils.INVALID) {
                    continue;
                }

                Grid<C> grid = other.grids.remove(id);

                if (grid == null) {
                    // Already removed.
                    continue;
                }

                if (grid.connects(side, direction.invert())) {
                    currentGrid.mergeWith(grid);
                    for (long pos : grid.getConnectors().keySet()) {
                        connectors.put(pos, pairing);
                    }
                }
            }
        }

        grids.putAll(other.grids);
    }

    /**
     * Tests if a particular position is has some other nodes neighbours in the group.
     *
     * @param pos The position to test.
     * @return Whether the position only has a neighbors nodes.
     */
    /*private boolean isNeighborsNode(long pos) {

        // If the group contains less than 2 node, neighbors cannot exist.
        if (nodes.size() <= 1) {
            return false;
        }

        byte neighbors = 0;
        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long side = position.offset(direction).get();

            if (nodes.get(side) != null) {
                neighbors++;
            }
        }

        return neighbors > 0;
    }*/

    /**
     * Lookups for neighbors grids around given position.
     *
     * @param pos The search position.
     * @return The set of the grids which are neighbors to each other.
     */
    private IntSet getNeighboringGrids(long pos) {
        IntSet neighbors = new IntLinkedOpenHashSet(6);

        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long side = position.offset(direction).get();
            int id = connectors.get(side);

            if (id != Utils.INVALID) {
                neighbors.add(id);
            }
        }

        return neighbors;
    }
}
