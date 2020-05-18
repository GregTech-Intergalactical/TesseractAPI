package tesseract.api;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tesseract.graph.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.IntFunction;

@ParametersAreNonnullByDefault
public class GraphWrapper<C extends IConnectable, N extends IConnectable> {

    protected final Int2ObjectMap<Graph<C, N>> graph = new Int2ObjectOpenHashMap<>();
    protected final IntFunction<Controller<C, N>> supplier;

    /**
     * Creates a graph wrapper.
     *
     * @param supplier The default controller supplier.
     */
    public GraphWrapper(IntFunction<Controller<C, N>> supplier) {
        this.supplier = supplier;
    }

    /**
     * Creates an instance of a class for a given node.
     *
     * @param dim The dimension id where the node will be added.
     * @param pos The position at which the node will be added.
     * @param node The node object.
     * @param controller The controller for the node
     */
    public void registerNode(int dim, long pos, N node, Controller<C, N> controller) {
        getGraph(dim).addNode(pos, new Cache<>(node), controller);
    }

    /**
     * Creates an instance of a class for a given node.
     *
     * @param dim The dimension id where the node will be added.
     * @param pos The position at which the node will be added.
     * @param node The node object.
     */
    public void registerNode(int dim, long pos, N node) {
        getGraph(dim).addNode(pos, new Cache<>(node), supplier.apply(dim));
    }

    /**
     * Creates an instance of a class for a given connector.
     *
     * @param dim The dimension id where the node will be added.
     * @param pos The position at which the node will be added.
     * @param connector The connector object.
     * @param controller The controller for the node
     */
    public void registerConnector(int dim, long pos, C connector, Controller<C, N> controller) {
        getGraph(dim).addConnector(pos, new Cache<>(connector), controller);
    }

    /**
     * Creates an instance of a class for a given connector.
     *
     * @param dim The dimension id where the node will be added.
     * @param pos The position at which the node will be added.
     * @param connector The connector object.
     */
    public void registerConnector(int dim, long pos, C connector) {
        getGraph(dim).addConnector(pos, new Cache<>(connector), supplier.apply(dim));
    }

    /**
     * Gets the graph for the type and dimension and will be instantiated if it does not already exist.
     *
     * @param dim The dimension id.
     * @return The graph instance for the world.
     */
    @Nonnull
    public Graph<C, N> getGraph(int dim) {
        return graph.computeIfAbsent(dim, k -> new Graph<>());
    }

    /**
     * Gets an instance of a controller at a given position.
     *
     * @param dim The dimension id where the electric component is exist.
     * @param pos The position at which the electric component is exist.
     * @return The controller object. (Can be null)
     */
    @Nullable
    public ITickingController getController(int dim, long pos) {
        Group<?, ?> group = getGraph(dim).getGroupAt(pos);
        return group != null ? group.getController() : null;
    }

    /**
     * Removes an instance of a class at the given position.
     *
     * @param dim The dimension id where the electric component will be added.
     * @param pos The position at which the electric component will be added.
     */
    public void remove(int dim, long pos) {
        getGraph(dim).removeAt(pos);
    }
}
