package tesseract.graph;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Direction;
import tesseract.Tesseract;
import tesseract.api.Controller;
import tesseract.api.IConnectable;
import tesseract.api.ITickingController;
import tesseract.util.CID;
import tesseract.util.Pos;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.LongFunction;
import java.util.function.Supplier;

/**
 * Class provides the functionality of any set of nodes.
 */
public class Graph<T, C extends IConnectable, N> implements INode {

    public static final Direction[] DIRECTIONS = Direction.values();
    private final Int2ObjectMap<Group<T, C, N>> groups = new Int2ObjectLinkedOpenHashMap<>();
    private final Long2IntMap positions = new Long2IntLinkedOpenHashMap(); // group positions
    private final Long2ObjectMap<EnumMap<Direction, Pending>> PENDING_NODES = new Long2ObjectOpenHashMap<>();

    public Graph() {
        positions.defaultReturnValue(CID.INVALID);
    }

    @Override
    public boolean contains(long pos) {
        return positions.containsKey(pos);
    }

    public void onFirstTick() {
        for (Long2ObjectMap.Entry<EnumMap<Direction, Pending>> m : PENDING_NODES.long2ObjectEntrySet()) {
            for (Map.Entry<Direction, Pending> entry : m.getValue().entrySet()) {
                Direction dir = entry.getKey();
                Pending v = entry.getValue();
                addNode(m.getLongKey(), v.fun, dir, v.controllerSupplier, true);
            }
        }
        PENDING_NODES.clear();
    }

    @Override
    public boolean linked(long from, Direction towards, long to) {
        return positions.containsKey(from) && positions.containsKey(to) && positions.get(from) == positions.get(to);
    }

    @Override
    public boolean connects(long pos, Direction towards) {
        return contains(pos);
    }

    /**
     * @return Gets the size of the groups map.
     */
    public int countGroups() {
        return groups.size();
    }

    /**
     * @return Gets the groups map.
     */
    public Int2ObjectMap<Group<T, C, N>> getGroups() {
        return Int2ObjectMaps.unmodifiable(groups);
    }

    /**
     * Adds a node to the graph at the specified position.
     *
     * @param pos        The position at which the node will be added.
     * @param node       The node to add, present as a LongFunction.
     * @param side       the side which the connector exists on. (Facing away from
     *                   node)
     * @param controller the controller supplier.
     * @return True on success or false otherwise.
     */
    public boolean addNode(long pos, BiFunction<Long, Direction, N> node, Direction side, Supplier<Controller<T, C, N>> controller,
                           boolean hadFirstTick) {
        if (hadFirstTick) {
            if (!contains(pos)) {
                long connectorPos = Pos.offset(pos, side);
                Group<T, C, N> tGroup = getGroupAt(connectorPos);
                if (tGroup == null) {
                    return false;
                }
                // Sanity checks.
                Cache<C> connector = tGroup.getConnector(connectorPos);
                if (connector == null)
                    return false;
                if (!connector.value().validate(side.getOpposite()))
                    return false;
                addNode(pos, controller.get(), new NodeCache<>(node.apply(pos, side), side));
                return true;
            } else if (this.getGroupAt(pos).getNodes().containsKey(pos)) {
                Group<T, C, N> group = this.getGroupAt(pos);
                if (group.getNodes().containsKey(pos)) {
                    long connectorPos = Pos.offset(pos, side);
                    // Make sure the relevant connector is valid.
                    Cache<C> connector = group.getConnector(connectorPos);
                    if (connector == null || !connector.value().validate(side.getOpposite()))
                        return false;
                    if (this.getGroupAt(pos).addSide(pos, side)) {
                        this.refreshNode(pos);
                    }
                }

                return true;
            }
        } else {
            PENDING_NODES.computeIfAbsent(pos, f -> new EnumMap<>(Direction.class)).put(side,
                    new Pending(controller, node));
            return true;
        }
        return false;
    }

    private void addNode(long pos, Controller<T,C,N> control, NodeCache<N> cache) {
        Group<T, C, N> group = add(pos, () -> Group.singleNode(pos, cache, control));
        if (group != null)
            group.addNode(pos, cache, control);
    }

    public void refreshNode(long pos) {
        if (contains(pos)) {
            ITickingController<T, C, N> controller = getGroupAt(pos).getController();
            if (Tesseract.hadFirstTick(controller.getWorld()))
                controller.change();
        }
    }

    /**
     * Adds a connector to the graph at the specified position.
     *
     * @param pos        The position at which the node will be added.
     * @param connector  The connector to add.
     * @param controller The controller to use.
     * @return True on success or false otherwise.
     */
    public boolean addConnector(long pos, Cache<C> connector, Controller<T, C, N> controller) {
        if (!contains(pos)) {
            Group<T, C, N> group = add(pos, () -> Group.singleConnector(pos, connector, controller));
            if (group != null)
                group.addConnector(pos, connector, controller);
            return true;
        }

        return false;
    }

    /**
     * Adds an item to the Graph, in a manner generic across nodes and connectors.
     *
     * @param pos    The position at which the item will be added.
     * @param single A group containing a single entry, if the position is not
     *               touching any existing positions.
     * @return An existing group, that the caller should add the entry to.
     */
    private Group<T, C, N> add(long pos, Supplier<Group<T, C, N>> single) {
        int id;
        IntSet mergers = getNeighboringGroups(pos);
        switch (mergers.size()) {
            case 0:
                id = CID.nextId();
                positions.put(pos, id);
                groups.put(id, single.get());
                return null;

            case 1:
                id = mergers.iterator().nextInt();
                positions.put(pos, id);
                return groups.get(id);

            default:
                Merged<T, C, N> data = beginMerge(mergers);
                positions.put(pos, data.bestId);
                for (Group<T, C, N> other : data.merged) {
                    data.best.mergeWith(other, pos);
                }
                return data.best;
        }
    }

    /**
     * Removes an entry from the Group, potentially splitting it if needed. By
     * calling this function, the caller asserts that this group contains the
     * specified position; the function may misbehave if the group does not actually
     * contain the specified position.
     *
     * @param pos The position of the entry to remove.
     */
    public boolean removeAt(long pos) {
        int id = positions.get(pos);

        if (id == CID.INVALID) {
            return false;
        }

        Group<T, C, N> group = groups.get(id);

        boolean ok = group.removeAt(pos, newGroup -> {
            int newId = CID.nextId();
            groups.put(newId, newGroup);

            // Mark the nodes as pointing at the new group
            for (long part : newGroup.getNodes().keySet()) {
                positions.put(part, newId);
            }

            // Mark the connectors as pointing at the new group
            for (Grid<C> grid : newGroup.getGrids().values()) {
                for (long part : grid.getConnectors().keySet()) {
                    positions.put(part, newId);
                }
            }
        });
        if (ok) {
            positions.remove(pos);
        }

        if (group.countBlocks() == 0) {
            groups.remove(id);
        }
        return ok;
    }

    /**
     * Gets the group by a given position.
     *
     * @param pos The position of the group.
     * @return The group, guaranteed to not be null.
     */
    public Group<T, C, N> getGroupAt(long pos) {
        int id = positions.get(pos);
        return (id != CID.INVALID) ? groups.get(id) : null;
    }

    /**
     * Starts a merging process for a given groups.
     *
     * @param mergers An array of neighbors groups id.
     * @return The wrapper with groups which should be merged.
     */
    private Merged<T, C, N> beginMerge(IntSet mergers) {
        int bestId = mergers.iterator().nextInt();
        Group<T, C, N> best = groups.get(bestId);
        int bestSize = best.countBlocks();

        for (int id : mergers) {
            Group<T, C, N> candidate = groups.get(id);
            int size = candidate.countBlocks();

            if (size > bestSize) {
                best = candidate;
                bestId = id;
                bestSize = size;
            }
        }

        List<Group<T, C, N>> mergeGroups = new ObjectArrayList<>(mergers.size() - 1);

        for (int id : mergers) {
            if (id == bestId) {
                continue;
            }

            Group<T, C, N> removed = groups.remove(id);

            // Remap each position to point to the correct group.
            for (long pos : removed.getBlocks()) {
                positions.put(pos, bestId);
            }

            mergeGroups.add(removed);
        }

        return new Merged<>(bestId, best, mergeGroups);
    }

    /**
     * Lookups for neighbors groups around given position.
     *
     * @param pos The search position.
     * @return The set of the groups which are neighbors to each other.
     */
    private IntSet getNeighboringGroups(long pos) {
        IntSet neighbors = new IntLinkedOpenHashSet(6);

        Pos position = new Pos(pos);
        for (Direction direction : Graph.DIRECTIONS) {
            long side = position.offset(direction).asLong();
            int id = positions.get(side);

            if (id != CID.INVALID) {
                neighbors.add(id);
            }
        }

        return neighbors;
    }

    /**
     * @apiNote Wrapper for merged groups.
     */
    private static class Merged<T, C extends IConnectable, N> {

        final int bestId;
        final Group<T, C, N> best;
        final List<Group<T, C, N>> merged;

        /**
         * Constructs a new Merged of the groups.
         */
        Merged(int bestId, Group<T, C, N> best, List<Group<T, C, N>> merged) {
            this.best = best;
            this.bestId = bestId;
            this.merged = merged;
        }
    }

    /**
     * Represents a pending node. This is used since you cannot access neighbours in
     * a world until first tick.
     */
    private class Pending {
        public final Supplier<Controller<T, C, N>> controllerSupplier;
        public final BiFunction<Long, Direction, N> fun;

        public Pending(Supplier<Controller<T, C, N>> controllerSupplier, BiFunction<Long, Direction, N> fun) {
            this.controllerSupplier = controllerSupplier;
            this.fun = fun;
        }
    }
}
