package tesseract;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tesseract.api.electric.*;
import tesseract.graph.Connectivity;
import tesseract.graph.Graph;
import tesseract.graph.Group;

public class TesseractAPI {

    private static final Int2ObjectMap<Graph<IElectricCable, IElectricNode>> ELECTRIC_GRAPH = new Int2ObjectOpenHashMap<>();

    //TODO use this static default implementation
    public static final IElectricEvent DEFAULT_ELECTRIC_EVENT = new IElectricEvent() {
        @Override
        public void onOverVoltage(long node) {

        }

        @Override
        public void onOverAmperage(long cable) {

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
     * @return Create a instance of a class for a given node.
     */
    public static void addElectricNode(int dimension, long position, IElectricNode node, IElectricEvent event) {
        Graph<IElectricCable, IElectricNode> graph = getElectricGraph(dimension);
        graph.addNode(position, Connectivity.Cache.of(node));
        Group<IElectricCable, IElectricNode> g = graph.getGroupAt(position).get();
        if (g.controller == null)
            g.controller = new ElectricController(g, event);
        if (g.currentTickHost == null) {
            g.currentTickHost = node;
            node.reset(null, g.controller);
        }
    }

    /**
     * @param dimension The dimension id where the cable will be added.
     * @param position The position at which the cable will be added.
     * @param cable The cable object.
     * @return Create a instance of a class for a given cable connector.
     */
    public static void addElectricCable(int dimension, long position, IElectricCable cable) {
        getElectricGraph(dimension).addConnector(position, Connectivity.Cache.of(cable));
    }

    public static void removeNode(int dimension, long position){
        getElectricGraph(dimension).removeAt(position);
    }

}
