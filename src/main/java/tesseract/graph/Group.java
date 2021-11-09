package tesseract.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.collections4.SetUtils;
import tesseract.Tesseract;
import tesseract.api.Controller;
import tesseract.api.IConnectable;
import tesseract.api.ITickingController;
import tesseract.graph.traverse.BFDivider;
import tesseract.util.CID;
import tesseract.util.Pos;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Group provides the functionality of a set of adjacent nodes that may or may not be linked.
 */
public class Group<T, C extends IConnectable, N> implements INode {

    private final Long2ObjectMap<NodeCache<N>> nodes = new Long2ObjectLinkedOpenHashMap<>();
    private final Int2ObjectMap<Grid<C>> grids = new Int2ObjectLinkedOpenHashMap<>();
    private final Long2IntMap connectors = new Long2IntLinkedOpenHashMap(); // connectors pairing
    private final BFDivider divider = new BFDivider(this);
    private ITickingController<T, C, N> controller = null;

    // Prevent the creation of empty groups externally, a caller needs to use singleNode/singleConnector.
    private Group() {
        connectors.defaultReturnValue(CID.INVALID);
    }

    /**
     * @param pos        The position of the node.
     * @param node       The given node.
     * @param controller The given controller.
     * @return Create a instance of a class for a given position and node.
     */
    protected static <T, C extends IConnectable, N> Group<T, C, N> singleNode(long pos, NodeCache<N> node, Controller<T, C, N> controller) {
        Group<T, C, N> group = new Group<>();
        group.addNode(pos, node, controller);
        return group;
    }

    /**
     * @param pos        The position of the connector.
     * @param connector  The given connector.
     * @param controller The given controller.
     * @return Create a instance of a class for a given position and connector.
     */
    protected static <T, C extends IConnectable, N> Group<T, C, N> singleConnector(long pos, Cache<C> connector, Controller<T, C, N> controller) {
        Group<T, C, N> group = new Group<>();
        int id = CID.nextId();
        group.connectors.put(pos, id);
        group.grids.put(id, Grid.singleConnector(pos, connector));
        group.updateController(controller);
        return group;
    }

    @Override
    public boolean contains(long pos) {
        return nodes.containsKey(pos) || connectors.containsKey(pos);
    }

    @Override
    public boolean linked(long from, Direction towards, long to) {
        return contains(from) && contains(to);
    }

    @Override
    public boolean connects(long pos, Direction towards) {
        return contains(pos);
    }

    /**
     * Calls the changing method for the controller.
     *
     * @param ticking The ticking instance.
     */
    private void updateController(Controller<T, C, N> ticking) {
        if (ticking == null)
            return;

        if (controller == null) {
            ticking.set(this);
            controller = ticking;
        }
        if (Tesseract.hadFirstTick(controller.getWorld()))
            controller.change();
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
    public Set<Long> getBlocks() {
        return SetUtils.union(nodes.keySet(), connectors.keySet());
    }

    /**
     * @return Returns nodes map.
     */
    public Long2ObjectMap<NodeCache<N>> getNodes() {
        return Long2ObjectMaps.unmodifiable(nodes);
    }

    public Iterable<Long2ObjectMap.Entry<Cache<C>>> getPipes() {
        return () -> this.grids.values().stream().flatMap(t -> t.getConnectors().long2ObjectEntrySet().stream().filter(p -> p.getValue().registerAsNode())).iterator();
    }

    /**
     * @return Returns grids set.
     */
    public Int2ObjectMap<Grid<C>> getGrids() {
        return Int2ObjectMaps.unmodifiable(grids);
    }

    /**
     * @return Returns group controller.
     */
    public ITickingController<T, C, N> getController() {
        return controller;
    }

    /**
     * Adds a new node to the group.
     *
     * @param pos        The given position.
     * @param node       The given node.
     * @param controller The controller to use.
     */
    public void addNode(long pos, NodeCache<N> node, Controller<T, C, N> controller) {
        nodes.put(pos, node);

        Pos position = new Pos(pos);
        for (Direction direction : Graph.DIRECTIONS) {
            int connector = connectors.get(position.offset(direction).asLong());
            if (connector == CID.INVALID) {
                continue;
            }
            Grid<C> grid = grids.get(connector);
            if (!grid.connects(position.offset(direction).asLong(), direction.getOpposite())) {
                continue;
            }

            grid.addNode(pos, node);
        }

        updateController(controller);
    }

    /**
     * Adds a new connector to the group.
     *
     * @param pos        The given position.
     * @param connector  The given connector.
     * @param controller The controller to use.
     */
    public void addConnector(long pos, Cache<C> connector, Controller<T, C, N> controller) {

        Int2ObjectMap<Grid<C>> linked = new Int2ObjectLinkedOpenHashMap<>();
        Long2ObjectMap<Direction> joined = new Long2ObjectLinkedOpenHashMap<>();
        Grid<C> bestGrid = null;
        int bestCount = 0;

        int bestId = CID.INVALID;

        Pos position = new Pos(pos);
        for (Direction direction : Graph.DIRECTIONS) {
            if (!connector.connects(direction)) {
                continue;
            }

            long side = position.offset(direction).asLong();
            int id = connectors.get(side);

            if (id == CID.INVALID) {
                // Collect joining nodes
                if (nodes.containsKey(side)) {
                    joined.put(side, direction);
                }
                continue;
            }

            Grid<C> grid = grids.get(id);

            if (grid.connects(side, direction.getOpposite())) {
                linked.put(id, grid);

                if (grid.countConnectors() > bestCount) {
                    bestCount = grid.countConnectors();
                    bestGrid = grid;
                    bestId = id;
                }
            }
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
        for (Long2ObjectMap.Entry<Direction> e : joined.long2ObjectEntrySet()) {
            long move = e.getLongKey();
            Direction direction = e.getValue();
            if (connector.connects(direction)) {
                bestGrid.addNode(move, nodes.get(move));
            }
        }

        if (bestCount != -1) {
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
        }
        updateController(controller);
    }

    private void internalRemove(long pos, Consumer<Group<T, C, N>> split) {
        // The contains() check can be skipped here, because Graph will only call remove() if it knows that the group contains the entry.
        // For now, it is retained for completeness and debugging purposes.
        if (!contains(pos)) {
            throw new IllegalArgumentException("Group::remove: Tried to call with a position that does not exist within the group.");
        }

        // If removing the entry would not cause a group split, then it is safe to remove the entry directly.
        if (isExternal(pos)) {
            if (removeNode(pos)) {
                if (controller != null)
                    controller.change();
                return;
            }

            int pairing = connectors.remove(pos);
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
                    for (Direction direction : Graph.DIRECTIONS) {
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
                connectors.remove(move);
                //nodes.remove(move);
                excluded.add(move);
            }

            centerGrid.removeAt(pos, splitGrids::add);
            splitGrids.add(centerGrid);

        } else {
            removeNode(pos);
        }

        for (int i = 0; i < colored.size(); i++) {
            LongSet found = colored.get(i);
            Group<T, C, N> newGroup;

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
                        connectors.remove(moved);
                        nodes.remove(moved);
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
                }
                split.accept(newGroup);
            } else if (controller != null)
                controller.change();
        }
    }

    /**
     * Removes an entry from the Group, potentially splitting it if needed. By calling this function, the caller asserts
     * that this group contains the specified position; the function may misbehave if the group does not actually contain
     * the specified position.
     *
     * @param pos   The position of the entry to remove.
     * @param split A consumer for the resulting fresh graphs from the split operation.
     */
    public boolean removeAt(long pos, Consumer<Group<T, C, N>> split) {
        NodeCache<N> node = nodes.get(pos);
        boolean flag = false;
        if (node != null) {
            flag = updateNode(pos, node);
        }
        internalRemove(pos, split);
        //Readd the node if it should not be removed completely.
        if (flag) {
            addNode(pos, node, (Controller<T, C, N>) getController());
        }
        return true;
    }

    private boolean updateNode(long pos, NodeCache<N> node) {
        if (node.isPipe()) return false;
        boolean ret = true;
        for (int i = 0; i < Graph.DIRECTIONS.length; i++) {
            Direction dir = Graph.DIRECTIONS[i];
            long offset = Pos.offset(pos, dir);
            Grid<C> grid = this.getGridAt(offset, dir);
            if (grid != null) {
                Cache<C> connector = grid.getConnectors().get(offset);
                if (connector != null) {
                    boolean ok = connector.value().validate(dir.getOpposite());
                    if (!ok) {
                        ret &= node.clearSide(dir);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Removes the nodes from nearest grids and pairs.
     *
     * @param pos The position of the node.
     * @return True if were deleted, false otherwise.
     */
    private boolean removeNode(long pos) {
        NodeCache<N> node = nodes.remove(pos);
        if (node == null) {
            return false;
        }

        // Clear removing node from nearest grid
        Pos position = new Pos(pos);
        for (Direction direction : Graph.DIRECTIONS) {
            long side = position.offset(direction).asLong();
            int id = connectors.get(side);

            if (id != CID.INVALID) {
                grids.get(id).removeNode(pos);
            }
        }
        return true;
    }

    /**
     * Adds a new grid to the group.
     *
     * @param id   The group id.
     * @param grid The grid object.
     */
    private void addGrid(int id, Grid<C> grid) {
        grids.put(id, grid);

        for (Long2ObjectMap.Entry<Cache<C>> moved : grid.getConnectors().long2ObjectEntrySet()) {
            connectors.put(moved.getLongKey(), id);
        }
    }

    /**
     * Gets near grid by a given position and direction value.
     *
     * @param pos       The position of the grid.
     * @param direction The direction we are looking to.
     * @return The grid map, guaranteed to not be null.
     */
    public Grid<C> getGridAt(long pos, Direction direction) {
        int id = connectors.get(pos);

        if (id != CID.INVALID) {
            Grid<C> grid = grids.get(id);
            if (grid.connects(pos, direction.getOpposite())) {
                return grid;
            }
        } else {
            id = connectors.get(Pos.offset(pos, direction.getOpposite()));
            if (id != CID.INVALID) {
                return grids.get(id);
            }
        }

        return null;
    }

    public void getGroupInfo(long pos, List<String> list) {
        Grid<C> grid = this.grids.get(connectors.get(pos));
        if (grid == null) return;
        list.add(String.format("Connector count (grid): %d", grid.countConnectors()));
        list.add(String.format("Node count (grid): %d", grid.countNodes()));
        list.add(String.format("Connector count (group): %d", connectors.size()));
        list.add(String.format("Node count (group): %d", nodes.size()));
    }

    public Cache<C> getConnector(long pos) {
        int id = this.connectors.get(pos);
        if (id != CID.INVALID) {
            return this.grids.get(id).getConnectors().get(pos);
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
        for (Direction direction : Graph.DIRECTIONS) {
            long side = position.offset(direction).asLong();

            if (contains(side)) {
                neighbors++;
            }
        }

        return neighbors <= 1;
    }

    /**
     * Merges one group to the another.
     *
     * @param other The another group.
     * @param pos   The given position.
     */
    public void mergeWith(Group<T, C, N> other, long pos) {
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
            for (Direction direction : Graph.DIRECTIONS) {
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

                if (grid.connects(side, direction.getOpposite())) {
                    currentGrid.mergeWith(grid);
                    for (long move : grid.getConnectors().keySet()) {
                        connectors.put(move, pairing);
                    }
                }
            }
        }

        grids.putAll(other.grids);
    }

    /**
     * Checks the health of this group, if there is any issue present.
     */
    public void healthCheck() {
        /*Long2IntMap count = new Long2IntOpenHashMap();

        for (Int2ObjectMap.Entry<Grid<C>> grids : this.grids.int2ObjectEntrySet()) {
            Long2ObjectMap<Cache<C>> grid = grids.getValue().getConnectors();
            for (Long2ObjectMap.Entry<Cache<C>> connectors : grid.long2ObjectEntrySet()) {
                BlockPos pos = BlockPos.fromLong(connectors.getLongKey());
                Cache<C> cache = connectors.getValue();
                C value = cache.value();

                byte cachedConn = cache.connectivity();
                for (int i = 0; i < Graph.DIRECTIONS.length; i++) {
                    boolean connects = value.connects(Graph.DIRECTIONS[i]);
                    boolean connectsCache = Connectivity.has(cachedConn, i);
                    if (connects != connectsCache) {
                        warn(pos);
                    }
                    if (connectsCache) {
                        count.compute(pos.offset(Graph.DIRECTIONS[i]).toLong(), (k, v) ->
                                v == null ? 1 : v + 1
                        );

                    }
                }
            }
        }
        for (Long2ObjectMap.Entry<NodeCache<N>> node : this.nodes.long2ObjectEntrySet()) {
            NodeCache<N> cache = node.getValue();
            if (cache.isPipe()) continue;
            if (cache.count() != count.get(node.getLongKey())) {
                warn(BlockPos.fromLong(node.getLongKey()));
                Tesseract.LOGGER.error("Expected " + cache.count() + " connections but only got " + count.get(node.getLongKey()));
                Tesseract.LOGGER.error("This is a bug, report to mod authors");
            }
        }*/
    }

    public boolean addSide(long pos, Direction side) {
        NodeCache<N> cache = this.nodes.get(pos);
        if (cache != null) {
            return cache.setSide(side);
        }
        return false;
    }

    private void warn(BlockPos pos) {
        Tesseract.LOGGER.error("Caught invalid position in Tesseract at position: " + pos);
    }
}
