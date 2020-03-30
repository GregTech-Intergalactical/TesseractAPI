package tesseract.electric;

import tesseract.TesseractAPI;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricEvent;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.Connectivity;
import tesseract.graph.Graph;

/**
 * Class provides the functionality of a electricity with usage of graphs.
 * @apiNote default parameters are nonnull, methods return nonnull.
 */
public class Electric {

    private long position;
    private Producer producer;
    private Graph<IElectricCable, IElectricNode> graph;

    /**
     * Prevent the creation of empty handlers externally.
     *
     * @param dimension The dimension id where the node will be added.
     * @param position The position at which the node will be added.
     */
    private Electric(int dimension, long position) {
        this.graph = TesseractAPI.getElectricGraph(dimension);
        this.position = position;
    }

    /**
     * @param dimension The dimension id where the node will be added.
     * @param position The position at which the node will be added.
     * @param node The node ref.
     * @param event The event listener.
     * @return Create a instance of a class for a given producer/consumer node.
     */
    public static Electric ofProducer(int dimension, long position, IElectricNode node, IElectricEvent event) {
        Electric system = new Electric(dimension, position);
        system.producer = new Producer(system.graph, position, node, event);
        system.graph.addNode(position, Connectivity.Cache.of(node, system.producer));
        return system;
    }

    /**
     * @param dimension The dimension id where the node will be added.
     * @param position The position at which the node will be added.
     * @param node The node ref.
     * @return Create a instance of a class for a given consumer node.
     */
    public static Electric ofConsumer(int dimension, long position, IElectricNode node) {
        Electric system = new Electric(dimension, position);
        system.graph.addNode(position, Connectivity.Cache.of(node));
        return system;
    }

    /**
     * @param dimension The dimension id where the cable will be added.
     * @param position The position at which the cable will be added.
     * @param cable The cable ref.
     * @return Create a instance of a class for a given cable connector.
     */
    public static Electric ofCable(int dimension, long position, IElectricCable cable) {
        Electric system = new Electric(dimension, position);
        system.graph.addConnector(position, Connectivity.Cache.of(cable));
        return system;
    }

    /**
     * Sends the energy to available consumers.
     */
    public void update() {
        producer.send();
    }

    /**
     * Removes instance from the graph.
     */
    public void remove() {
        graph.removeAt(position);
    }
}
