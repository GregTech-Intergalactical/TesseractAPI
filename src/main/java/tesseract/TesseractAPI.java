package tesseract;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tesseract.api.electric.*;
import tesseract.graph.Connectivity;
import tesseract.graph.Graph;
import tesseract.graph.Group;

public class TesseractAPI {

    private static final Int2ObjectMap<Graph<IElectricCable, IElectricNode>> ELECTRIC_GRAPH = new Int2ObjectOpenHashMap<>();

    /**
     * Gets the graph for the dimension, if it not exist it will create it.
     *
     * @param dimension The dimension id.
     * @return The graph instance for the world.
     */
    public static Graph<IElectricCable, IElectricNode> getElectricGraph(int dimension) {
        return ELECTRIC_GRAPH.computeIfAbsent(dimension, k -> new Graph<>());
    }

    /**
     * Creates an instance of a class for a given node.
     * @param dimension The dimension id where the node will be added.
     * @param position The position at which the node will be added.
     * @param node The node object.
     */
    public static void addElectricNode(int dimension, long position, IElectricNode node, IElectricEvent event) {
        Graph<IElectricCable, IElectricNode> graph = getElectricGraph(dimension);
        graph.addNode(position, Connectivity.Cache.of(node));
        Group<IElectricCable, IElectricNode> group = graph.getGroupAt(position);

        assert group != null;

        if (group.getController() == null) {
            group.setController(new ElectricController(group, event));
        }

        if (group.getCurrentTickHost() == null) {
            group.setCurrentTickHost(node);
            node.reset(null, group.getController());
        }
    }

    /**
     * Creates an instance of a class for a given cable connector.
     * @param dimension The dimension id where the cable will be added.
     * @param position The position at which the cable will be added.
     * @param cable The cable object.
     */
    public static void addElectricCable(int dimension, long position, IElectricCable cable) {
        getElectricGraph(dimension).addConnector(position, Connectivity.Cache.of(cable));
    }

    /**
     * Removes an instance of a class at the given position.
     * @param dimension The dimension id where the cable will be added.
     * @param position The position at which the cable will be added.
     */
    public static void removeElectric(int dimension, long position){
        getElectricGraph(dimension).removeAt(position);
    }
}
