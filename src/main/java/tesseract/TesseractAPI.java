package tesseract;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tesseract.api.electric.IElectricCable;
import tesseract.api.electric.IElectricNode;
import tesseract.graph.Graph;

public class TesseractAPI {

    private static final Int2ObjectMap<Graph<IElectricCable, IElectricNode>> ELECTRIC_GRAPH = new Int2ObjectOpenHashMap<>();

    /**
     * Gets the graph for the dimension, if it not exist it will create it.
     *
     * @param dimId The dimension id.
     * @return The graph instance for the world.
     */
    public static Graph<IElectricCable, IElectricNode> getElectricGraph(int dimId) {
        return ELECTRIC_GRAPH.computeIfAbsent(dimId, k -> new Graph<>());
    }
}
