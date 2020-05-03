package tesseract.graph;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import org.apache.commons.collections4.SetUtils;
import tesseract.api.Controller;
import tesseract.graph.traverse.BFDivider;
import tesseract.util.Dir;
import tesseract.util.Pos;
import tesseract.util.CID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Group provides the functionality of a set of adjacent nodes that may or may not be linked.
 */
@ParametersAreNonnullByDefault
public class Group<C extends IConnectable, N extends IConnectable> implements INode {

    private final Long2ObjectMap<Cache<N>> nodes = new Long2ObjectLinkedOpenHashMap<>();
    private final Int2ObjectMap<Grid<C>> grids = new Int2ObjectLinkedOpenHashMap<>();
    private final Long2IntMap connectors = new Long2IntLinkedOpenHashMap(); // connectors pairing
    private final BFDivider divider = new BFDivider(this);
    private ITickingController controller = null;
    private ITickHost currentTickHost = null;

    // Prevent the creation of empty groups externally, a caller needs to use singleNode/singleConnector.
    private Group() {
        connectors.defaultReturnValue(CID.INVALID);
    }

    /**
     * @param pos The position of the node.
     * @param node The given node.
     * @param controller The given controller.
     * @return Create a instance of a class for a given position and node.
     */
    @Nonnull
    protected static <C extends IConnectable, N extends IConnectable> Group<C, N> singleNode(long pos, Cache<N> node, @Nullable Controller<C, N> controller) {
        Group<C, N> group = new Group<>();
        group.addNode(pos, node, controller);
        return group;
    }

    /**
     * @param pos The position of the connector.
     * @param connector The given connector.
     * @param controller The given controller.
     * @return Create a instance of a class for a given position and connector.
     */
    @Nonnull
    protected static <C extends IConnectable, N extends IConnectable> Group<C, N> singleConnector(long pos, Cache<C> connector, @Nullable Controller<C, N> controller) {
        Group<C, N> group = new Group<>();
        int id = CID.nextId();
        group.connectors.put(pos, id);
        group.grids.put(id, Grid.singleConnector(pos, connector));
        group.updateController(connector, controller);
        return group;
    }

    @Override
    public boolean contains(long pos) {
        return nodes.containsKey(pos) || connectors.containsKey(pos);
    }

    @Override
    public boolean linked(long from, @Nullable Dir towards, long to) {
        return contains(from) && contains(to);
    }

    @Override
    public boolean connects(long pos, @Nullable Dir towards) {
        return contains(pos);
    }

    /**
     * Resets the current tick host.
     */
    private void releaseControllerHost() {
        if (currentTickHost != null && controller != null) {
            currentTickHost.reset(controller, null);
        }
    }

    /**
     * Resets the current controller host.
     *
     * @param cache The given cache.
     */
    private void resetControllerHost(Cache<?> cache) {
        if (currentTickHost != null && cache.value() instanceof ITickHost && cache.value() == currentTickHost) {
            currentTickHost.reset(controller, null);
            findNextValidHost(cache);
        }
    }

    /**
     * Calls the changing method for the controller.
     *
     * @param cache The given cache object.
     * @param ticking The ticking instance.
     */
    private void updateController(Cache<?> cache, @Nullable Controller<C, N> ticking) {
        if (ticking == null) return;

        if (controller == null) {
            ticking.set(this);
            controller = ticking;
        }

        if (currentTickHost == null) {
            // If cache contains tick host set as a new one
            if (cache.value() instanceof ITickHost) {
                currentTickHost = (ITickHost) cache.value();
                currentTickHost.reset(null, controller);
            }
        }

        controller.change();
    }

    /**
     * Finds the next available host in the group.
     *
     * @param cache The given cache.
     */
    private void findNextValidHost(@Nullable Cache<?> cache) {
        if (controller == null) return;
        currentTickHost = null;

        // Lookup for a ticking host among nodes
        for (Cache<?> n : nodes.values()) {
            if (nextCache(cache, n)) {
                continue;
            }
            break;
        }

        if (currentTickHost == null) {
            // Lookup for a ticking host among connectors
            I: for (int id : connectors.values()) {
                Grid<C> grid = grids.get(id);

                for (Cache<?> c : grid.getConnectors().values()) {
                    if (nextCache(cache, c)) {
                        continue;
                    }
                    break I;
                }
            }
        }

        if (currentTickHost != null) {
            currentTickHost.reset(null, controller);
            controller.change();
        }
    }

    /**
     * Trying to switch for a new host.
     *
     * @param cache The given cache object.
     * @param o The current cache.
     * @return True or false.
     */
    private boolean nextCache(@Nullable Cache<?> cache, Cache<?> o) {
        if (o == cache || !(o.value() instanceof ITickHost)) {
            return true;
        }

        currentTickHost = (ITickHost) o.value();
        return false;
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
    @Nonnull
    public Set<Long> getBlocks() {
        return SetUtils.union(nodes.keySet(), connectors.keySet());
    }

    /**
     * @return Returns nodes map.
     */
    @Nonnull
    public Long2ObjectMap<Cache<N>> getNodes() {
        return Long2ObjectMaps.unmodifiable(nodes);
    }

    /**
     * @return Returns grids set.
     */
    @Nonnull
    public Int2ObjectMap<Grid<C>> getGrids() {
        return Int2ObjectMaps.unmodifiable(grids);
    }

    /**
     * @return Returns group controller.
     */
    @Nullable
    public ITickingController getController() {
        return controller;
    }

    /**
     * @return Returns group ticking host.
     */
    @Nullable
    public ITickHost getCurrentTickHost() {
        return currentTickHost;
    }

    /**
     * Adds a new node to the group.
     *
     * @param pos The given position.
     * @param node The given node.
     * @param controller The controller to use.
     */
    public void addNode(long pos, Cache<N> node, @Nullable Controller<C, N> controller) {
        nodes.put(pos, node);

        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            if (!node.connects(direction)) {
                continue;
            }

            long side = position.offset(direction).asLong();
            int id = connectors.get(side);

            // Add a node to the neighboring grid ?
            if (id != CID.INVALID) {
                Grid<C> grid = grids.get(id);
                side = position.offset(direction).asLong();

                if (grid.connects(side, direction.invert())) {
                    grid.addNode(pos, node);
                }
            }
        }

        updateController(node, controller);
    }

    /**
     * Adds a new connector to the group.
     * @param pos The given position.
     * @param connector The given connector.
     * @param controller The controller to use.
     */
    public void addConnector(long pos, Cache<C> connector, @Nullable Controller<C, N> controller) {

        Int2ObjectMap<Grid<C>> linked = new Int2ObjectLinkedOpenHashMap<>();
        Long2ObjectMap<Dir> joined = new Long2ObjectLinkedOpenHashMap<>();
        Grid<C> bestGrid = null;
        int bestCount = 0;
        int bestId = CID.INVALID;

        int neighbors = 0;
        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            if (!connector.connects(direction)) {
                continue;
            }

            long side = position.offset(direction).asLong();
            int id = connectors.get(side);

            if (id == CID.INVALID) {
                // Collect joining nodes
                if (nodes.containsKey(side)) {
                    neighbors++;
                    joined.put(side, direction);
                }
                continue;
            }

            neighbors++;

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
            bestId = CID.nextId();
            bestGrid = Grid.singleConnector(pos, connector);

            connectors.put(pos, bestId);
            grids.put(bestId, bestGrid);
            bestCount = -1; // For exit
        }

        if (bestGrid == null) {
            throw new IllegalStateException();
        }

        // Add neighbours nodes to the grid
        for (Long2ObjectMap.Entry<Dir> e : joined.long2ObjectEntrySet()) {
            long move = e.getLongKey();
            Dir direction = e.getValue();

            Cache<N> node = nodes.get(move);

            if (node.connects(direction.invert())) {
                bestGrid.addNode(move, node);
            }
        }

        if (bestCount == -1) {
            // Grid was just initialized
            return;
        }

        // Add to the best grid
        connectors.put(pos, bestId);
        bestGrid.addConnector(pos, connector);

        if (linked.size() > 1) {
            // Other grids to merge with
            for (Int2ObjectMap.Entry<Grid<C>> e : linked.int2ObjectEntrySet()) {
                int id = e.getIntKey();
                Grid<C> grid = e.getValue();

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

        updateController(connector, controller);
    }

    /**
     * Removes an entry from the Group, potentially splitting it if needed. By calling this function, the caller asserts
     * that this group contains the specified position; the function may misbehave if the group does not actually contain
     * the specified position.
     *
     * @param pos The position of the entry to remove.
     * @param split A consumer for the resulting fresh graphs from the split operation.
     */
    public void removeAt(long pos, Consumer<Group<C, N>> split) {

        // The contains() check can be skipped here, because Graph will only call remove() if it knows that the group contains the entry.
        // For now, it is retained for completeness and debugging purposes.
        if (!contains(pos)) {
            throw new IllegalArgumentException("Group::remove: Tried to call with a position that does not exist within the group.");
        }

        // If removing the entry would not cause a group split, then it is safe to remove the entry directly.
        if (isExternal(pos)) {
            if (removeNode(pos)) {
                return;
            }

            int pairing = removeConnector(pos);
            Grid<C> grid = grids.get(pairing);

            // No check is needed here, because the caller already asserts that the Group contains the specified position.
            // Thus, if this is not a node, then it is guaranteed to be a connector.
            grid.removeAt(
                pos,
                newGrid -> {
                    int newId = CID.nextId();
                    grids.put(newId, newGrid);

                    for (long move : newGrid.getConnectors().keySet()) {
                        connectors.put(move, newId);
                    }
                }
            );

            // Avoid leaving empty grids within the grid list.
            if (grid.countConnectors() == 0) {
                grids.remove(pairing);
            }

            return;
        }

        // If none of the fast routes work, we need to due a full group-traversal to figure out how the graph will be split.
        // The algorithm works by "coloring" each fragment of the group based on what it is connected to, and then from this,
        // splitting each colored portion into its own separate group.

        // For optimization purposes, the largest colored fragment remains resident within its original group.
        // Note: we don't remove the node yet, but instead just tell the Searcher to exclude it.
        // This is so that we can handle the grid splits ourselves at the end.
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

        List<Grid<C>> splitGrids = null;
        LongSet excluded = new LongOpenHashSet();

        int centerGridId = connectors.get(pos);
        if (centerGridId != CID.INVALID) {
            Grid<C> centerGrid = grids.remove(centerGridId);
            splitGrids = new ObjectArrayList<>();

            for (long move : centerGrid.getConnectors().keySet()) {
                removeConnector(move);
                excluded.add(move);
            }

            centerGrid.removeAt(pos, splitGrids::add);
            splitGrids.add(centerGrid);

        } else {
            removeNode(pos);
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
                    if (id == CID.INVALID) {
                        newGroup.nodes.put(reached, nodes.remove(reached));
                        continue;
                    }

                    Grid<C> grid = grids.get(id);
                    if (grid.contains(pos)) {
                        // This should be unreachable
                        throw new IllegalStateException("Group::remove: Searchable grid contains the removed position, the grid should have been removed already?!?");
                    }

                    // Move grid to new group
                    grids.remove(id);
                    newGroup.grids.put(id, grid);

                    for (long moved : grid.getConnectors().keySet()) {
                        removeConnector(moved);
                        newGroup.connectors.put(moved, id);
                    }
                }
            } else {
                newGroup = this;
            }

            // Add the fragments of the center grid, if present, to each group
            if (splitGrids != null) {
               Iterator<Grid<C>> it = splitGrids.iterator();

                while (it.hasNext()) {
                    Grid<C> grid = it.next();
                    long sample = grid.sampleConnector();

                    if (found.contains(sample)) {
                        int newId = CID.nextId();

                        newGroup.addGrid(newId, grid);
                        it.remove();
                    }
                }
            }

            if (i != bestColor) {
                if (controller != null) {
                    newGroup.controller = controller.clone(newGroup);
                    newGroup.findNextValidHost(null);
                }
                split.accept(newGroup);
            } else {
                releaseControllerHost();
                findNextValidHost(null);
            }
        }
    }

    /**
     * Removes the nodes from nearest grids and pairs.
     *
     * @param pos The position of the node.
     * @return True if were deleted, false otherwise.
     */
    private boolean removeNode(long pos) {
        Cache<N> node = nodes.remove(pos);
        if (node == null) {
            return false;
        }

        resetControllerHost(node);

        // Clear removing node from nearest grid
        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long side = position.offset(direction).asLong();
            int id = connectors.get(side);

            if (id != CID.INVALID) {
                grids.get(id).removeNode(pos);
            }
        }

        return true;
    }

    /**
     * Removes the connector from the grid.
     *
     * @param pos The position of the connector.
     * @return The grid id where removing occurred.
     */
    private int removeConnector(long pos) {
        int id = connectors.remove(pos);
        Grid<C> grid = grids.get(id);
        Cache<C> cable = grid.getConnectors().get(pos);

        resetControllerHost(cable);
        return id;
    }

    /**
     * Adds a new grid to the group.
     *
     * @param id The group id.
     * @param grid The grid object.
     */
    private void addGrid(int id, Grid<C> grid) {
        grids.put(id, grid);

        for (long moved : grid.getConnectors().keySet()) {
            connectors.put(moved, id);
        }
    }

    /**
     * Gets near grid by a given position and direction value.
     *
     * @param pos The position of the grid.
     * @param direction The direction we are looking to.
     * @return The grid map, guaranteed to not be null.
     */
    @Nullable
    public Grid<C> getGridAt(long pos, Dir direction) {
        int id = connectors.get(pos);

        if (id != CID.INVALID) {
            Grid<C> grid = grids.get(id);
            if (grid.connects(pos, direction.invert())) {
                return grid;
            }
        }

        return null;
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

        int neighbors = 0;
        Pos position = new Pos(pos);
        for (Dir direction : Dir.VALUES) {
            long side = position.offset(direction).asLong();

            if (contains(side)) {
                neighbors++;
            }
        }

        return neighbors <= 1;
    }

    /**
     * Merges one group to the another.
     * @param other The another group.
     * @param pos The given position.
     */
    public void mergeWith(Group<C, N> other, long pos) {
        other.releaseControllerHost();
        nodes.putAll(other.nodes);
        connectors.putAll(other.connectors);

        for (int id : other.grids.keySet()) {
            if (grids.containsKey(id)) {
                throw new IllegalStateException("Group::mergeWith: Duplicate grid UUIDs when attempting to merge groups, this should never happen!");
            }
        }

        int pairing = connectors.get(pos);

        if (pairing != CID.INVALID) {
            Grid<C> currentGrid = grids.get(pairing);

            Pos position = new Pos(pos);
            for (Dir direction : Dir.VALUES) {
                long side = position.offset(direction).asLong();

                if (!currentGrid.connects(pos, direction)) {
                    continue;
                }

                int id = other.connectors.get(side);

                if (id == CID.INVALID) {
                    continue;
                }

                Grid<C> grid = other.grids.remove(id);

                if (grid == null) {
                    // Already removed.
                    continue;
                }

                if (grid.connects(side, direction.invert())) {
                    currentGrid.mergeWith(grid);
                    for (long move : grid.getConnectors().keySet()) {
                        connectors.put(move, pairing);
                    }
                }
            }
        }

        grids.putAll(other.grids);
    }
}
