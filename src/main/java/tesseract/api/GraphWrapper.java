package tesseract.api;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import tesseract.Tesseract;
import tesseract.graph.Cache;
import tesseract.graph.Graph;
import tesseract.graph.Group;

import java.util.function.BiFunction;
import java.util.function.Function;

public class GraphWrapper<T, C extends IConnectable, N> {

    protected final Object2ObjectMap<IWorld, Graph<T, C, N>> graph = new Object2ObjectOpenHashMap<>();
    // TODO: maybe do this better.
    protected final Function<World, Controller<T, C, N>> supplier;

    /**
     * Creates a graph wrapper.
     *
     * @param supplier The default controller supplier.
     */
    public GraphWrapper(Function<World, Controller<T, C, N>> supplier) {
        this.supplier = supplier;
    }

    /**
     * Creates an instance of a class for a given node.
     *
     * @param dim  The dimension id where the node will be added.
     * @param pos  The position at which the node will be added.
     * @param node The node object.
     */
    public void registerNode(IWorld dim, long pos, Direction side, BiFunction<Long, Direction, N> node) {
        if (dim.isClientSide())
            return;
        getGraph(dim).addNode(pos, node, side, () -> supplier.apply(dim instanceof World ? ((World) dim) : null),
                Tesseract.hadFirstTick(dim));
    }

    public void refreshNode(World dim, long pos) {
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
    public void registerConnector(World dim, long pos, C connector) {
        if (dim.isClientSide())
            return;
        getGraph(dim).addConnector(pos, new Cache<>(connector), supplier.apply(dim));
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
        return graph.computeIfAbsent(dim, k -> new Graph<>());
    }

    /**
     * Gets an instance of a controller at a given position.
     *
     * @param dim The dimension id where the electric component is exist.
     * @param pos The position at which the electric component is exist.
     * @return The controller object. (Can be null)
     */
    public ITickingController<T, C, N> getController(World dim, long pos) {
        if (dim.isClientSide())
            return null;
        Group<T, C, N> group = getGraph(dim).getGroupAt(pos);
        return group != null ? group.getController() : null;
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
        return getGraph(dim).removeAt(pos);
    }

    public void tick(World dim) {
        Graph<T, C, N> g = graph.get(dim);
        if (g != null)
            g.getGroups().forEach((pos, gr) -> gr.getController().tick());
    }

    public void onFirstTick(World dim) {
        getGraph(dim).onFirstTick();
        getGraph(dim).getGroups().values().forEach(t -> {
            try {
                t.getController().change();
            } catch (Exception ex) {
                Tesseract.LOGGER.warn("Error updating controller : " + ex);
            }
        });
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
}
