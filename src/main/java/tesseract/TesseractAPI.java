package tesseract;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tesseract.api.electric.ElectricController;
import tesseract.api.electric.IElectricCable;
import tesseract.api.electric.IElectricEvent;
import tesseract.api.electric.IElectricNode;
import tesseract.api.fluid.FluidController;
import tesseract.api.fluid.IFluidEvent;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemPipe;
import tesseract.api.item.ItemController;
import tesseract.graph.Graph;
import tesseract.graph.Group;
import tesseract.graph.Cache;

public class TesseractAPI {

    private static final Int2ObjectMap<Graph<IElectricCable, IElectricNode>> ELECTRIC_GRAPH = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Graph<IFluidPipe, IFluidNode>> FLUID_GRAPH = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Graph<IItemPipe, IItemNode>> ITEM_GRAPH = new Int2ObjectOpenHashMap<>();

    public static IElectricEvent GLOBAL_ELECTRIC_EVENT;
    public static IFluidEvent GLOBAL_FLUID_EVENT;

    /**
     * Gets the graph for the dimension, if it not exist it will create it.
     *
     * @param dim The dimension id.
     * @return The graph instance for the world.
     */
    public static Graph<IElectricCable, IElectricNode> getElectricGraph(int dim) {
        return ELECTRIC_GRAPH.computeIfAbsent(dim, graph -> new Graph<>());
    }

    /**
     * Gets the graph for the dimension, if it not exist it will create it.
     *
     * @param dim The dimension id.
     * @return The graph instance for the world.
     */
    public static Graph<IFluidPipe, IFluidNode> getFluidGraph(int dim) {
        return FLUID_GRAPH.computeIfAbsent(dim, graph -> new Graph<>());
    }

    /**
     * Gets the graph for the dimension, if it not exist it will create it.
     *
     * @param dim The dimension id.
     * @return The graph instance for the world.
     */
    public static Graph<IItemPipe, IItemNode> getItemGraph(int dim) {
        return ITEM_GRAPH.computeIfAbsent(dim, graph -> new Graph<>());
    }

    /**
     * Creates an instance of a class for a given node.
     * @param dim The dimension id where the node will be added.
     * @param pos The position at which the node will be added.
     * @param node The node object.
     */
    public static void registerElectricNode(int dim, long pos, IElectricNode node) {
        Graph<IElectricCable, IElectricNode> graph = getElectricGraph(dim);
        graph.addNode(pos, new Cache<>(node));
        Group<IElectricCable, IElectricNode> group = graph.getGroupAt(pos);

        assert group != null;

        if (group.getController() == null) {
            group.setController(new ElectricController(dim, group));
        }

        if (group.getCurrentTickHost() == null) {
            group.setCurrentTickHost(node);
            node.reset(null, group.getController());
        }
    }

    /**
     * Creates an instance of a class for a given node.
     * @param dim The dimension id where the node will be added.
     * @param pos The position at which the node will be added.
     * @param node The node object.
     */
    public static void registerFluidNode(int dim, long pos, IFluidNode node) {
        Graph<IFluidPipe, IFluidNode> graph = getFluidGraph(dim);
        graph.addNode(pos, new Cache<>(node));
        Group<IFluidPipe, IFluidNode> group = graph.getGroupAt(pos);

        assert group != null;

        if (group.getController() == null) {
            group.setController(new FluidController(dim, group));
        }

        if (group.getCurrentTickHost() == null) {
            group.setCurrentTickHost(node);
            node.reset(null, group.getController());
        }
    }

    /**
     * Creates an instance of a class for a given node.
     * @param dim The dimension id where the node will be added.
     * @param pos The position at which the node will be added.
     * @param node The node object.
     */
    public static void registerItemNode(int dim, long pos, IItemNode node) {
        Graph<IItemPipe, IItemNode> graph = getItemGraph(dim);
        graph.addNode(pos, new Cache<>(node));
        Group<IItemPipe, IItemNode> group = graph.getGroupAt(pos);

        assert group != null;

        if (group.getController() == null) {
            group.setController(new ItemController(dim, group));
        }

        if (group.getCurrentTickHost() == null) {
            group.setCurrentTickHost(node);
            node.reset(null, group.getController());
        }
    }

    /**
     * Creates an instance of a class for a given cable connector.
     * @param dim The dimension id where the cable will be added.
     * @param pos The position at which the cable will be added.
     * @param connector The cable object.
     */
    public static void registerElectricCable(int dim, long pos, IElectricCable connector) {
        getElectricGraph(dim).addConnector(pos, new Cache<>(connector));
    }

    /**
     * Creates an instance of a class for a given pipe connector.
     * @param dim The dimension id where the pipe will be added.
     * @param pos The position at which the pipe will be added.
     * @param connector The pipe object.
     */
    public static void registerFluidPipe(int dim, long pos, IFluidPipe connector) {
        getFluidGraph(dim).addConnector(pos, new Cache<>(connector));
    }

    /**
     * Creates an instance of a class for a given pipe connector.
     * @param dim The dimension id where the pipe will be added.
     * @param pos The position at which the pipe will be added.
     * @param connector The pipe object.
     */
    public static void registerItemPipe(int dim, long pos, IItemPipe connector) {
        getItemGraph(dim).addConnector(pos, new Cache<>(connector));
    }

    /**
     * Removes an instance of a class at the given position.
     * @param dim The dimension id where the electric component will be added.
     * @param pos The position at which the electric component will be added.
     */
    public static void removeElectric(int dim, long pos){
        getElectricGraph(dim).removeAt(pos);
    }

    /**
     * Removes an instance of a class at the given position.
     * @param dim The dimension id where the fluid component will be added.
     * @param pos The position at which the fluid component will be added.
     */
    public static void removeFluid(int dim, long pos){
        getFluidGraph(dim).removeAt(pos);
    }

    /**
     * Removes an instance of a class at the given position.
     * @param dim The dimension id where the item component will be added.
     * @param pos The position at which the item component will be added.
     */
    public static void removeItem(int dim, long pos){
        getItemGraph(dim).removeAt(pos);
    }
}
