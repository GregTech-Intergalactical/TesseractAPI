package tesseract.graph;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.LazyOptional;
import tesseract.Tesseract;
import tesseract.api.Controller;
import tesseract.api.IConnectable;
import tesseract.api.ITickingController;
import tesseract.util.CID;
import tesseract.util.Pos;

import java.nio.channels.IllegalSelectorException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle.Control;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Class provides the functionality of any set of nodes.
 */
public class Graph<T, C extends IConnectable, N> implements INode {

    public static final Direction[] DIRECTIONS = Direction.values();
    private final Int2ObjectMap<Group<T, C, N>> groups = new Int2ObjectLinkedOpenHashMap<>();
    private final Long2IntMap positions = new Long2IntLinkedOpenHashMap(); // group positions
    private final Long2ObjectMap<INodeGetter<N>> PENDING_NODES = new Long2ObjectOpenHashMap<>();
    private final Supplier<Controller<T,C,N>> controller;

    public Graph(Supplier<Controller<T,C,N>> controller) {
        positions.defaultReturnValue(CID.INVALID);
        this.controller = controller;
    }

    @Override
    public boolean contains(long pos) {
        return positions.containsKey(pos);
    }

    public void onFirstTick() {
        for (Long2ObjectMap.Entry<INodeGetter<N>> m : PENDING_NODES.long2ObjectEntrySet()) {
            addNodes(getGroupAt(m.getLongKey()).getConnector(m.getLongKey()).value(), m.getLongKey(), m.getValue());
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

    public void onUpdate(long connectorPos, long nodePos, INodeGetter<N> getter) {
        Direction side = Pos.subToDir(nodePos, connectorPos);
        Group<T,C,N> group = this.getGroupAt(connectorPos);
        if (group == null) return;
        Cache<C> conn = group.getConnector(connectorPos);
        if (conn == null) return;
        boolean ok = conn.value().validate(side);
        NodeCache<N> node = group.getNodes().get(nodePos);
        if (node == null && ok) {
            NodeCache<N> cache = new NodeCache<>(nodePos, getter, this);
            addNode(nodePos, controller.get(), cache);
        } else if (node != null) {
           updateNode(nodePos, controller);
        }
        //we have a node already, cap wasn't updated tho so we can leave
    }

    /**
     * @return Gets the groups map.
     */
    public Int2ObjectMap<Group<T, C, N>> getGroups() {
        return Int2ObjectMaps.unmodifiable(groups);
    }

    void onCapabilityInvalidate(long pos) {
        Group<T,C,N> group = this.getGroupAt(pos);
        if (group == null) return;
        if (!group.contains(pos)) return;
        boolean isConnector = group.getConnector(pos) != null;
        if (!isConnector) {
            updateNode(pos, controller);
        }
    }

    boolean validate(Direction side, long pos) {
        Group<T,C,N> group = this.getGroupAt(Pos.offset(pos,side));
        if (group == null) return false;
        Cache<C> conn = group.getConnector(Pos.offset(pos, side));
        if (conn == null) return false;
        return conn.value().validate(side.getOpposite());
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
    private boolean addNodes(C connector, long pos, INodeGetter<N> node) {
           for (Direction dir : Graph.DIRECTIONS) {
                final long nodePos = Pos.offset(pos, dir);
                NodeCache<N> cache = new NodeCache<>(nodePos, node, this);
                if (cache.count() > 0) addNode(nodePos, controller.get(), cache);
           }
           return true;
    }

    private void addNode(long pos, Controller<T,C,N> control, NodeCache<N> cache) {
        Group<T, C, N> group = add(pos, () -> Group.singleNode(pos, cache, control));
        if (group != null)
            group.addNode(pos, cache, control);
    }

    public void refreshNode(long pos) {
        if (contains(pos)) {
            ITickingController<T, C, N> controller = getGroupAt(pos).getController();
            if (Tesseract.hadFirstTick(controller.getWorld())) {
                try {
                    controller.change();
                } catch (Exception ex) {
                    Tesseract.LOGGER.warn("Error updating controller : " + ex);
                }
            }
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
    public boolean addConnector(long pos, Cache<C> connector, INodeGetter<N> node, boolean hadFirstTick) {
        if (!contains(pos)) {
            Group<T, C, N> group = add(pos, () -> Group.singleConnector(pos, connector, controller.get()));
            if (group != null)
                group.addConnector(pos, connector, controller.get());
            if (!hadFirstTick) {
                PENDING_NODES.put(pos, node);
            } else {
                addNodes(connector.value(), pos, node);
            }
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
    public boolean removeAt(long pos, Supplier<Controller<T, C, N>> controller) {
        Group<T,C,N> gr = this.getGroupAt(pos);
        if (gr == null) return false;
        boolean isConnector = gr.getConnector(pos) != null;
        if (!isConnector) {
            throw new IllegalStateException("Attempting to call Graph::removeAt at an invalid position");
        }
        boolean ok = removeInternal(pos);
        if (ok) {
            for (Direction dir : Graph.DIRECTIONS) {
                updateNode(Pos.offset(pos, dir), controller);
            }
        }

        return ok;
    }

    private boolean removeInternal(long pos) {
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

    private void updateNode(long nodePos, Supplier<Controller<T, C, N>> controller) {
        Group<T,C,N> group = this.getGroupAt(nodePos);
        if (group == null) {
            return;
        }
        NodeCache<N> cache = group.getNodes().get(nodePos);
        if (cache == null) return;
        boolean ok = updateNodeSides(cache);
        removeInternal(nodePos);
        if (ok) {
            if (controller == null) {
                throw new IllegalStateException("expected non-null controller supplier in graph::refreshNodes");
            }
            addNode(nodePos, controller.get(), cache);
        }
    }

    private boolean updateNodeSides(NodeCache<N> node) {
        for (int i = 0; i < Graph.DIRECTIONS.length; i++) {
            node.updateSide(Graph.DIRECTIONS[i]);
        }
        return node.count() > 0;
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

    public interface INodeGetter<T> {
        T get(long pos, Direction capSide, Runnable capCallback);
    }
}
