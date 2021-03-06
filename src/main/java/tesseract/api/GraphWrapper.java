package tesseract.api;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tesseract.graph.Cache;
import tesseract.graph.Graph;
import tesseract.graph.Group;

import java.util.function.BiFunction;
import java.util.function.Function;

public class GraphWrapper<C extends IConnectable, N extends IConnectable> {

    protected final Object2ObjectMap<RegistryKey<World>, Graph<C, N>> graph = new Object2ObjectOpenHashMap<>();
    //TODO: maybe do this better.
    protected final BiFunction<Function<RegistryKey<World>, ServerWorld>,RegistryKey<World>, Controller<C, N>> supplier;
    protected final Function<RegistryKey<World>, ServerWorld> worldSupplier;

    /**
     * Creates a graph wrapper.
     *
     * @param supplier The default controller supplier.
     */
    public GraphWrapper(Function<RegistryKey<World>, ServerWorld> worldSupplier, BiFunction<Function<RegistryKey<World>, ServerWorld>,RegistryKey<World>, Controller<C, N>> supplier) {
        this.supplier = supplier;
        this.worldSupplier = worldSupplier;
    }

    /**
     * Creates an instance of a class for a given node.
     *
     * @param dim The dimension id where the node will be added.
     * @param pos The position at which the node will be added.
     * @param node The node object.
     */
    public void registerNode(RegistryKey<World> dim, long pos, N node) {
        getGraph(dim).addNode(pos, new Cache<>(node), supplier.apply(worldSupplier,dim));
    }

    /**
     * Creates an instance of a class for a given connector.
     *
     * @param dim The dimension id where the node will be added.
     * @param pos The position at which the node will be added.
     * @param connector The connector object.
     */
    public void registerConnector(RegistryKey<World> dim, long pos, C connector) {
        getGraph(dim).addConnector(pos, new Cache<>(connector), supplier.apply(worldSupplier, dim));
    }

    /**
     * Gets the graph for the type and dimension and will be instantiated if it does not already exist.
     *
     * @param dim The dimension id.
     * @return The graph instance for the world.
     */
    public Graph<C, N> getGraph(RegistryKey<World> dim) {
        return graph.computeIfAbsent(dim, k -> new Graph<>());
    }

    /**
     * Gets an instance of a controller at a given position.
     *
     * @param dim The dimension id where the electric component is exist.
     * @param pos The position at which the electric component is exist.
     * @return The controller object. (Can be null)
     */
    public ITickingController getController(RegistryKey<World> dim, long pos) {
        Group<?, ?> group = getGraph(dim).getGroupAt(pos);
        return group != null ? group.getController() : null;
    }

    /**
     * Removes an instance of a class at the given position.
     *
     * @param dim The dimension id where the electric component will be added.
     * @param pos The position at which the electric component will be added.
     */
    public void remove(RegistryKey<World> dim, long pos) {
        getGraph(dim).removeAt(pos);
    }

    public void tick(RegistryKey<World> dim) {
        Graph<C, N> g = graph.get(dim);
        if (g != null)
            g.getGroups().forEach((pos, gr) -> gr.getController().tick());
    }
}
