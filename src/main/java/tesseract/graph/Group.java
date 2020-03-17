package tesseract.graph;

import tesseract.graph.traverse.BFDivider;
import tesseract.graph.traverse.INodeContainer;
import tesseract.graph.visit.VisitableGrid;
import tesseract.graph.visit.VisitableGroup;
import tesseract.util.Dir;
import tesseract.util.Pos;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// default: parameters are nonnull, methods return nonnull
public class Group<C extends IConnectable, N extends IConnectable> implements INodeContainer, VisitableGroup<C, N> {

    private HashMap<Pos, Connectivity.Cache<N>> nodes;
    private HashMap<Pos, UUID> connectorPairing;
    private HashMap<UUID, Grid<C>> grids;

    private BFDivider divider;

    // Prevent the creation of empty groups externally, a caller needs to use singleNode/singleConnector.
    private Group() {
        nodes = new HashMap<>();
        connectorPairing = new HashMap<>();
        grids = new HashMap<>();
        divider = new BFDivider(this);
    }

    public static <C extends IConnectable, N extends IConnectable> Group<C, N> singleNode(Pos at, Connectivity.Cache<N> node) {
        Group<C, N> group = new Group<>();

        group.addNode(at, node);

        return group;
    }

    public static <C extends IConnectable, N extends IConnectable> Group<C, N> singleConnector(Pos at, Connectivity.Cache<C> connector) {
        Group<C, N> group = new Group<>();
        UUID id = group.getNewId();

        group.connectorPairing.put(at, id);
        group.grids.put(id, Grid.singleConnector(at, connector));

        return group;
    }

    @Override
    public boolean contains(Pos at) {
        Objects.requireNonNull(at);

        return nodes.containsKey(at) || connectorPairing.containsKey(at);
    }

    @Override
    public boolean linked(Pos from, Dir towards, Pos to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        return contains(from) && contains(to);
    }

    @Override
    public boolean connects(Pos position, Dir towards) {
        return contains(position);
    }

    @Override
    public int countBlocks() {
        return nodes.size() + connectorPairing.size();
    }

    @Override
    public void visitBlocks(Consumer<Pos> consumer) {
        nodes.keySet().forEach(consumer);
        connectorPairing.keySet().forEach(consumer);
    }

    @Override
    public void visitNodes(BiConsumer<Pos, N> visitor) {
        for (Map.Entry<Pos, Connectivity.Cache<N>> entry : nodes.entrySet()) {
            visitor.accept(entry.getKey(), entry.getValue().value());
        }
    }

    @Override
    public void visitGrids(Consumer<VisitableGrid<C>> visitor) {
        for (Grid<C> grid : grids.values()) {
            visitor.accept(grid);
        }
    }

    public void addNode(Pos at, Connectivity.Cache<N> node) {
        nodes.put(Objects.requireNonNull(at), Objects.requireNonNull(node));
    }

    public void addConnector(Pos at, Connectivity.Cache<C> connector) {
        connector = Objects.requireNonNull(connector);

        HashMap<UUID, Grid<C>> linkedGrids = new HashMap<>();
        UUID bestId = null;
        Grid<C> bestGrid = null;
        int bestCount = 0;

        int neighbors = 0;

        for (Dir direction : Dir.VALUES) {
            if (!connector.connects(direction)) {
                continue;
            }

            Pos offset = at.offset(direction);
            UUID id = connectorPairing.get(offset);

            if (id == null) {
                neighbors += nodes.containsKey(offset) ? 1 : 0;
                continue;
            }

            neighbors += 1;

            Grid<C> grid = grids.get(id);

            if (grid.connects(offset, direction.invert())) {
                linkedGrids.put(id, grid);

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

        if (linkedGrids.isEmpty()) {
            // Single connector grid
            UUID id = getNewId();

            connectorPairing.put(at, id);
            grids.put(id, Grid.singleConnector(at, connector));
            return;
        }

        if (bestGrid == null) {
            throw new IllegalStateException();
        }

        // Add to the best grid
        connectorPairing.put(at, bestId);
        bestGrid.addConnector(at, connector);

        if (linkedGrids.size() == 1) {
            // No other grids to merge with
            return;
        }

        for (Map.Entry<UUID, Grid<C>> entry : linkedGrids.entrySet()) {
            UUID id = entry.getKey();
            Grid<C> grid = entry.getValue();

            if (id.equals(bestId)) {
                continue;
            }

            final UUID target = bestId;

            bestGrid.mergeWith(at, grid);
            grid.visitConnectors((item, visited) -> connectorPairing.put(item, target));

            grids.remove(id);
        }
    }

    /**
     * Removes an entry from the Group, potentially splitting it if needed. By calling this function, the caller asserts
     * that this group contains the specified position; the function may misbehave if the group does not actually contain
     * the specified position.
     *
     * @param posToRemove The position of the entry to remove.
     * @param split       A consumer for the resulting fresh graphs from the split operation.
     * @return The removed entry, guaranteed to not be null.
     */
    public Entry<C, N> remove(Pos posToRemove, Consumer<Group<C, N>> split) {
        // The contains() check can be skipped here, because Graph will only call remove() if it knows that the group contains the entry.
        // For now, it is retained for completeness and debugging purposes.
        if (!contains(posToRemove)) {
            throw new IllegalArgumentException("Tried to call Group::remove with a position that does not exist within the group.");
        }

        // If removing the entry would not cause a group split, then it is safe to remove the entry directly.
        if (isExternal(posToRemove)) {
            Connectivity.Cache<N> node = nodes.remove(posToRemove);
            UUID pairing = connectorPairing.remove(posToRemove);

            if (node != null) {
                return Entry.node(node.value());
            }

            Grid<C> grid = grids.get(Objects.requireNonNull(pairing));

            // No check is needed here, because the caller already asserts that the Group contains the specified position.
            // Thus, if this is not a node, then it is guaranteed to be a connector.
            C removed = grid.remove(
                posToRemove,
                newGrid -> {
                    UUID newId = getNewId();
                    grids.put(newId, newGrid);

                    newGrid.visitConnectors((pos, connector) -> connectorPairing.put(pos, newId));
                }
            );

            // Avoid leaving empty grids within the grid list.
            if (grid.countConnectors() == 0) {
                grids.remove(pairing);
            }

            return Entry.connector(removed);
        }

        // If none of the fast routes work, we need to due a full group-traversal to figure out how the graph will be split.
        // The algorithm works by "coloring" each fragment of the group based on what it is connected to, and then from this,
        // splitting each colored portion into its own separate group.

        // For optimization purposes, the largest colored fragment remains resident within its original group.
        // Note: we don't remove the node yet, but instead just tell the Searcher to exclude it.
        // This is so that we can handle the grid splits ourselves at the end.
        ArrayList<HashSet<Pos>> colored = new ArrayList<>();

        int bestColor = divider.divide(
            removed -> removed.add(posToRemove),
            roots -> {
                for (Dir direction : Dir.VALUES) {
                    Pos side = posToRemove.offset(direction);

                    if (this.linked(posToRemove, direction, side)) {
                        roots.add(side);
                    }
                }
            },
            colored::add
        );

        HashSet<Grid<C>> splitGrids = null;
        HashSet<Pos> excluded = new HashSet<>();

        Entry<C, N> result;

        UUID centerGridId = connectorPairing.get(posToRemove);
        if (centerGridId != null) {
            Grid<C> centerGrid = grids.remove(centerGridId);
            splitGrids = new HashSet<>();

            centerGrid.visitConnectors((toMove, connector) -> {
                connectorPairing.remove(toMove);
                excluded.add(toMove);
            });

            result = Entry.connector(centerGrid.remove(posToRemove, splitGrids::add));
            splitGrids.add(centerGrid);
        } else {
            result = Entry.node(nodes.remove(posToRemove).value());
        }

        for (int i = 0; i < colored.size(); i++) {
            HashSet<Pos> found = colored.get(i);
            Group<C, N> newGroup;

            if (i != bestColor) {
                newGroup = new Group<>();

                for (Pos reached : found) {
                    if (newGroup.connectorPairing.containsKey(reached) || (excluded.contains(reached))) {
                        continue;
                    }

                    UUID gridId = connectorPairing.get(reached);

                    // Just a node then, simply add it to the new group.
                    // The maps are mutated directly here in order to retain the cached connectivity.
                    if (gridId == null) {
                        newGroup.nodes.put(reached, Objects.requireNonNull(this.nodes.remove(reached)));
                        continue;
                    }

                    Grid<C> grid = grids.get(gridId);
                    if (grid.contains(posToRemove)) {
                        // This should be unreachable
                        throw new IllegalStateException("Searchable grid contains the removed position, the grid should have been removed already?!?");
                    }

                    // Move grid to new group
                    grids.remove(gridId);
                    newGroup.grids.put(gridId, grid);

                    grid.visitConnectors((moved, connector) -> {
                        connectorPairing.remove(moved);
                        newGroup.connectorPairing.put(moved, gridId);
                    });
                }
            } else {
                newGroup = this;
            }

            // Add the fragments of the center grid, if present, to each group
            if (splitGrids != null) {
                Iterator<Grid<C>> iterator = splitGrids.iterator();

                while (iterator.hasNext()) {
                    Grid<C> grid = iterator.next();
                    Pos sample = grid.sampleConnector();

                    if (found.contains(sample)) {
                        UUID newId = newGroup.getNewId();

                        newGroup.addGrid(newId, grid);
                        iterator.remove();
                    }
                }
            }

            if (i != bestColor) {
                split.accept(newGroup);
            }
        }

        return Objects.requireNonNull(result);
    }

    private void addGrid(UUID id, Grid<C> grid) {
        this.grids.put(id, grid);

        grid.visitConnectors((moved, connector) -> connectorPairing.put(moved, id));
    }

    /**
     * Tests if a particular position is only connected to the group on a single side, or is the only entry in the group.
     *
     * @param pos The position to test
     * @return Whether the position only has a single neighbor in the group, or is the only entry in the group.
     */
    private boolean isExternal(Pos pos) {
        // If the group contains less than 2 blocks, neighbors cannot exist.
        if (this.countBlocks() <= 1) {
            return true;
        }

        int neighbors = 0;
        for (Dir direction : Dir.VALUES) {
            Pos face = pos.offset(direction);

            if (this.contains(face)) {
                neighbors += 1;
            }
        }

        return neighbors <= 1;
    }

    // Graph controlled interface
    void mergeWith(Group<C, N> other, Pos at) {
        nodes.putAll(other.nodes);
        connectorPairing.putAll(other.connectorPairing);

        other.grids.keySet().forEach(id -> {
            if (grids.containsKey(id)) {
                // TODO: Handle duplicate IDs
                throw new IllegalStateException("Duplicate grid UUIDs when attempting to merge groups, this should never happen!");
            }
        });

        UUID pairing = connectorPairing.get(at);

        if (pairing != null) {
            Grid<C> currentGrid = grids.get(pairing);

            for (Dir direction : Dir.VALUES) {
                Pos offset = at.offset(direction);

                if (!currentGrid.connects(at, direction)) {
                    continue;
                }

                UUID id = other.connectorPairing.get(offset);

                if (id == null) {
                    continue;
                }

                Grid<C> grid = other.grids.remove(id);

                if (grid == null) {
                    // Already removed.
                    continue;
                }

                if (grid.connects(offset, direction.invert())) {
                    currentGrid.mergeWith(at, grid);
                    grid.visitConnectors((position, connector) -> this.connectorPairing.put(position, pairing));
                }
            }
        }

        grids.putAll(other.grids);
    }

    private UUID getNewId() {
        UUID uuid = UUID.randomUUID();
        while (grids.containsKey(uuid)) {
            // Should never be called, but whatever.
            uuid = UUID.randomUUID();
        }

        return uuid;
    }
}
