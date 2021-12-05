package tesseract.api;

import java.util.function.Function;

import javax.annotation.Nonnull;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import tesseract.Tesseract;
import tesseract.graph.Cache;
import tesseract.graph.Graph;
import tesseract.graph.Graph.INodeGetter;
import tesseract.graph.Group;

public class GraphWrapper<T, C extends IConnectable, N> {

    protected final Object2ObjectMap<LevelAccessor, Graph<T, C, N>> graph = new Object2ObjectOpenHashMap<>();
    // TODO: maybe do this better.
    protected final Function<Level, Controller<T, C, N>> supplier;

    /**
     * Creates a graph wrapper.
     *
     * @param supplier The default controller supplier.
     */
    public GraphWrapper(Function<Level, Controller<T, C, N>> supplier) {
        this.supplier = supplier;
    }

    /**
     * Creates an instance of a class for a given node.
     *
     * @param dim  The dimension id where the node will be added.
     * @param pos  The position at which the node will be added.
     * @param node The node object.
     */
    /*public void registerNode(IWorld dim, long pos, Direction side, BiFunction<Long, Direction, N> node) {
        if (dim.isClientSide())
            return;
        getGraph(dim).addNode(pos, node, side, () -> supplier.apply(dim instanceof World ? ((World) dim) : null),
                Tesseract.hadFirstTick(dim));
    }*/

    public void refreshNode(Level dim, long pos) {
        if (dim.isClientSide())
            return;
        getGraph(dim).refreshNode(pos);
    }

    /**
     * Creates an instance of a class for a given connector.
     *
     * @param dim       The dimension id where the node will be added.
     * @param pos       The position at which the node will be added.
     * @param connector The connector object.
     */
    public void registerConnector(Level dim, long pos, C connector, INodeGetter<N> applier) {
        if (dim.isClientSide())
            return;
        getGraph(dim).addConnector(pos, new Cache<>(connector), applier, Tesseract.hadFirstTick(dim));
    }

    public void blockUpdate(Level dim, long connector, long node, INodeGetter<N> applier) {
        if (dim.isClientSide()) return;
        getGraph(dim).onUpdate(connector, node, applier);
    }

    /**
     * Gets the graph for the type and dimension and will be instantiated if it does
     * not already exist.
     *
     * @param dim The dimension id.
     * @return The graph instance for the world.
     */
    public Graph<T, C, N> getGraph(LevelAccessor dim) {
        assert !dim.isClientSide();
        return graph.computeIfAbsent(dim, k -> new Graph<>(() -> supplier.apply((Level) dim)));
    }

    /**
     * Gets an instance of a controller at a given position.
     *
     * @param dim The dimension id where the electric component is exist.
     * @param pos The position at which the electric component is exist.
     * @return The controller object. (Can be null)
     */
    @Nonnull
    public ITickingController<T, C, N> getController(Level dim, long pos) {
        if (dim.isClientSide())
            return null;
        Group<T, C, N> group = getGraph(dim).getGroupAt(pos);
        return group != null ? group.getController() : supplier.apply(dim);
    }

    /**
     * Removes an instance of a class at the given position.
     *
     * @param dim The dimension id where the electric component will be added.
     * @param pos The position at which the electric component will be added.
     */
    public boolean remove(Level dim, long pos) {
        if (dim.isClientSide())
            return false;
        return getGraph(dim).removeAt(pos, () -> supplier.apply(dim));
    }

    public void tick(Level dim) {
        Graph<T, C, N> g = graph.get(dim);
        if (g != null)
            g.getGroups().forEach((pos, gr) -> gr.getController().tick());
    }

    public void onFirstTick(Level dim) {
        getGraph(dim).onFirstTick();
        getGraph(dim).getGroups().values().forEach(t -> {
            try {
                t.getController().change();
            } catch (Exception ex) {
                Tesseract.LOGGER.warn("Error updating controller : " + ex);
            }
        });
    }

    public void removeWorld(Level world) {
        this.graph.remove(world);
    }

    public void clear() {
        this.graph.clear();
    }

    public void healthCheck() {
        this.graph.values().forEach(v -> v.getGroups().values().forEach(Group::healthCheck));
    }
}
