package tesseract.api;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.util.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import tesseract.Tesseract;
import tesseract.graph.Cache;
import tesseract.graph.Graph;
import tesseract.graph.Graph.INodeGetter;
import tesseract.graph.Group;
import tesseract.graph.NodeCache;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class GraphWrapper<T, C extends IConnectable, N> {

    private static final ObjectSet<GraphWrapper<?,?,?>> ALL_WRAPPERS = new ObjectOpenHashSet<>();

    protected final Object2ObjectMap<IWorld, Graph<T, C, N>> graph = new Object2ObjectOpenHashMap<>();
    protected final BiFunction<World, INodeGetter<N>, Controller<T, C, N>> supplier;
    protected final ICapabilityGetter<N> getter;
    private final Map<World, LongSet> PENDING_NODES = new Object2ObjectOpenHashMap<>();

    /**
     * Creates a graph wrapper.
     *
     * @param supplier The default controller supplier.
     */
    public GraphWrapper(BiFunction<World, INodeGetter<N>, Controller<T, C, N>> supplier, ICapabilityGetter<N> getter) {
        this.supplier = supplier;
        this.getter = getter;
        ALL_WRAPPERS.add(this);
    }

    /**
     * Creates an instance of a class for a given node.
     *
     * @param dim  The dimension id where the node will be added.
     * @param pos  The position at which the node will be added.
     */
    /*public void registerNode(IWorld dim, long pos, Direction side, BiFunction<Long, Direction, N> node) {
        if (dim.isClientSide())
            return;
        getGraph(dim).addNode(pos, node, side, () -> supplier.apply(dim instanceof World ? ((World) dim) : null),
                Tesseract.hadFirstTick(dim));
    }*/

    /**
     * Registers a connector into Tesseract.
     *
     * @param dim       The dimension id where the node will be added.
     * @param pos       The position at which the node will be added.
     * @param connector The connector object.
     */
    public void registerConnector(World dim, long pos, C connector, boolean regular) {
        if (dim.isClientSide())
            return;
        getGraph(dim).addConnector(pos, new Cache<>(connector));
        if (!Tesseract.hadFirstTick(dim)) {
            PENDING_NODES.computeIfAbsent(dim, d -> new LongOpenHashSet()).add(pos);
        } else {
            addNodes(dim, pos);
        }
    }

    public void blockUpdate(World dim, long connector, long node) {
        if (dim.isClientSide()) return;
        update(dim, node, Pos.subToDir(connector, node), false);
    }

    /**
     * Gets the graph for the type and dimension and will be instantiated if it does
     * not already exist.
     *
     * @param dim The dimension id.
     * @return The graph instance for the world.
     */
    public Graph<T, C, N> getGraph(IWorld dim) {
        assert !dim.isClientSide();
        INodeGetter<N> get = (a,b,c) -> getter.get((World)dim,a,b,c);
        return graph.computeIfAbsent(dim, k -> new Graph<>(() -> supplier.apply((World) dim, get)));
    }

    /**
     * Gets an instance of a controller at a given position.
     *
     * @param dim The dimension id where the electric component is exist.
     * @param pos The position at which the electric component is exist.
     * @return The controller object. (Can be null)
     */
    @Nonnull
    public ITickingController<T, C, N> getController(World dim, long pos) {
        if (dim.isClientSide()) {
            throw new IllegalStateException("Call to GraphWrapper::getController on client side!");
        }
        Group<T, C, N> group = getGraph(dim).getGroupAt(pos);
        INodeGetter<N> get = (a, b, c) -> getter.get((World) dim, a, b, c);

        return group != null ? group.getController() : supplier.apply(dim, get);
    }

    /**
     * Primary update method in Tesseract, receiving capability invalidations and block updates.
     *
     * @param pos the node position.
     */
    private void update(World dim, long pos, @Nonnull Direction side, boolean isInvalidate) {
        //offset to the connector.
        long cPos = Pos.offset(pos, side);
        Graph<T, C, N> graph = getGraph(dim);
        Group<T, C, N> group = graph.getGroupAt(cPos);
        if (group == null) return;
        //only update nodes
        Cache<C> cCache = group.getConnector(cPos);
        if (cCache == null) {
            NodeCache<N> nodeCache = group.getNodes().get(cPos);
            if (nodeCache == null) return;
        }
        NodeCache<N> cache = group.getNodes().get(pos);
        INodeGetter<N> get = (a, b, c) -> getter.get(dim, a, b, c);
        if (cache == null) {
            cache = new NodeCache<>(pos, get, (a, b) -> this.validate(graph, a, b), (a, b) -> this.update(dim, b, a, true));
            graph.addNode(pos, cache);
        } else {
            if (isInvalidate) {
                if (cache.updateSide(side)) {
                    group.getController().change();
                    return;
                }
            }
            updateNode(graph, pos);
        }
    }

    /**
     * Adds a node to the graph at the specified position.
     *
     * @param pos The position at which the node will be added.
     */
    public void addNodes(World dim, long pos) {
        Graph<T, C, N> graph = getGraph(dim);
        INodeGetter<N> get = (a, b, c) -> getter.get(dim, a, b, c);
        for (Direction dir : Graph.DIRECTIONS) {
            final long nodePos = Pos.offset(pos, dir);
            NodeCache<N> cache = new NodeCache<>(nodePos, get, (a, b) -> this.validate(graph, a, b), (a, b) -> this.update(dim, b, a, true));
            graph.addNode(nodePos, cache);
        }
    }


    public void onFirstTick(World dim) {
        LongSet set = PENDING_NODES.remove(dim);
        if (set != null) set.forEach((Consumer<? super Long>) l -> this.addNodes(dim, l));
    }


    private void updateNode(Graph<T, C, N> graph, long nodePos) {
        Group<T, C, N> group = graph.getGroupAt(nodePos);
        if (group == null) {
            return;
        }
        NodeCache<N> cache = group.getNodes().get(nodePos);
        if (cache == null) return;
        int count = cache.count();
        boolean ok = updateNodeSides(cache);
        if ((cache.count() != count) || cache.count() == 0) {
            graph.removeAt(nodePos);
            if (ok) {
                graph.addNode(nodePos, cache);
            }
        } else {
            group.getController().change();
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
    private boolean removeAt(Graph<T, C, N> graph, long pos) {
        Group<T, C, N> gr = graph.getGroupAt(pos);
        if (gr == null) return false;
        boolean ok = graph.removeAt(pos);
        if (ok) {
            for (Direction dir : Graph.DIRECTIONS) {
                updateNode(graph, Pos.offset(pos, dir));
            }
        }
        return ok;
    }

    private boolean updateNodeSides(NodeCache<N> node) {
        for (int i = 0; i < Graph.DIRECTIONS.length; i++) {
            node.updateSide(Graph.DIRECTIONS[i]);
        }
        return node.count() > 0;
    }


    boolean validate(Graph<T, C, N> graph, Direction side, long pos) {
        Group<T, C, N> group = graph.getGroupAt(Pos.offset(pos, side));
        if (group == null) return false;
        Cache<C> conn = group.getConnector(Pos.offset(pos, side));
        if (conn != null) {
            return conn.value().validate(side.getOpposite());
        }
        //NodeCache<N> cache = group.getNodes().get(Pos.offset(pos, side));
        return false;
    }


    /**
     * Removes an instance of a class at the given position.
     *
     * @param dim The dimension id where the electric component will be added.
     * @param pos The position at which the electric component will be added.
     */
    public boolean remove(World dim, long pos) {
        if (dim.isClientSide())
            return false;
        return removeAt(getGraph(dim), pos);
    }

    public void tick(World dim) {
        Graph<T, C, N> g = graph.get(dim);
        if (g != null)
            g.getGroups().forEach((pos, gr) -> gr.getController().tick());
    }

    public static Set<GraphWrapper<?,?,?>> getWrappers() {
        return ObjectSets.unmodifiable(ALL_WRAPPERS);
    }

    public void removeWorld(World world) {
        this.graph.remove(world);
    }

    public void clear() {
        this.graph.clear();
    }

    public void healthCheck() {
        this.graph.values().forEach(v -> v.getGroups().values().forEach(Group::healthCheck));
    }

    public interface ICapabilityGetter<T> {
        T get(World level, long pos, Direction capSide, Runnable capCallback);
    }
}
