package tesseract;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tesseract.api.GraphWrapper;
import tesseract.api.electric.*;
import tesseract.graph.Connectivity;
import tesseract.graph.Graph;

public class TesseractAPI {

    private static final Int2ObjectMap<Graph<IElectricCable, IElectricNode>> ELECTRIC_GRAPH = new Int2ObjectOpenHashMap<>();

    //TODO use this static default implementation
    public static final IElectricEvent DEFAULT_ELECTRIC_EVENT = new IElectricEvent() {
        @Override
        public void onOverVoltage(IElectricNode node) {
            //NOOP
        }

        @Override
        public void onOverAmperage(IElectricCable cable) {
            //NOOP
        }
    };

    /**
     * Gets the graph for the dimension, if it not exist it will create it.
     *
     * @param dimId The dimension id.
     * @return The graph instance for the world.
     */
    public static Graph<IElectricCable, IElectricNode> getElectricGraph(int dimId) {
        return ELECTRIC_GRAPH.computeIfAbsent(dimId, k -> new Graph<>());
    }

    /**
     * @param dimension The dimension id where the node will be added.
     * @param position The position at which the node will be added.
     * @param node The node object.
     * @param event The event listener.
     * @return Create a instance of a class for a given node with controller.
     */
    public static ElectricController asElectricController(int dimension, long position, IElectricNode node, IElectricEvent event) {
        Graph<IElectricCable, IElectricNode> graph = getElectricGraph(dimension);
        ElectricController controller = new ElectricController(graph, position, event);
        graph.addNode(position, Connectivity.Cache.of(node, controller));
        return controller;
    }

    /**
     * @param dimension The dimension id where the node will be added.
     * @param position The position at which the node will be added.
     * @param node The node object.
     * @return Create a instance of a class for a given node.
     */
    public static GraphWrapper asElectricNode(int dimension, long position, IElectricNode node) {
        Graph<IElectricCable, IElectricNode> graph = getElectricGraph(dimension);
        graph.addNode(position, Connectivity.Cache.of(node));
        return new GraphWrapper(graph, position);
    }

    /**
     * @param dimension The dimension id where the cable will be added.
     * @param position The position at which the cable will be added.
     * @param cable The cable object.
     * @return Create a instance of a class for a given cable connector.
     */
    public static GraphWrapper asElectricCable(int dimension, long position, IElectricCable cable) {
        Graph<IElectricCable, IElectricNode> graph = getElectricGraph(dimension);
        graph.addConnector(position, Connectivity.Cache.of(cable));
        return new GraphWrapper(graph, position);
    }
}
