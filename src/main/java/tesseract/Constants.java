package tesseract;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.Graph;

public class Constants {

    public static final String API_ID = "tesseract";
    public static final String API_NAME = "Tesseract API";

    private static final Int2ObjectMap<Graph<IElectricCable, IElectricNode>> ELECTRICITY = new Int2ObjectOpenHashMap<>();

    /**
     * Gets the graph for the dimension, if it not exist it will create it.
     *
     * @param id The dimension id.
     * @return The graph instance for the world.
     */
    public static Graph<IElectricCable, IElectricNode> electric(int id) {
        if (!ELECTRICITY.containsKey(id)) {
            ELECTRICITY.put(id, new Graph<>());
        }
        return ELECTRICITY.get(id);
    }
}