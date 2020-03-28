package tesseract.electric;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.*;

/**
 * The Producer is a class that should wrap provider data.
 */
public class Producer implements IListener {

    private long position;
    private IElectricNode node;
    private ObjectSet<Consumer> consumers;
    private Graph<IElectricCable, IElectricNode> graph;

    /**
     * Creates instance of producer.
     *
     * @param graph The graph instance.
     * @param node The producer node.
     * @param position The position variable.
     */
    protected Producer(Graph<IElectricCable, IElectricNode> graph, IElectricNode node, long position) {
        this.node = node;
        this.graph = graph;
        this.position = position;
        this.consumers = new ObjectLinkedOpenHashSet<>();
    }

    @Override
    public void update() {
        graph.findGroup(position).ifPresent(group -> {
            consumers.clear();

            for (Grid<IElectricCable> grid : group.findGrids(position)) {
                for (Path<IElectricCable> path : grid.getPaths(position)) {
                    if (!path.isEmpty()) {
                        graph.findAt(path.target().get()).asEndpoint().ifPresent(consumer -> add(consumer, path));
                    }
                }
            }
        });
    }

    /**
     * Adds the consumer to the consumers list.
     *
     * @param consumer The consumer node.
     * @param path The path to consumer data.
     */
    private void add(IElectricNode consumer, Path<IElectricCable> path) {
        if (consumer.canInput()) {
            if (node.getOutputVoltage() > consumer.getInputVoltage()) {
                // Explode
            } else {
                consumers.add(new Consumer(consumer, path));
            }
        }
    }

    /**
     * @return Gets the producer node.
     */
    public IElectricNode getNode() {
        return node;
    }

    /**
     * @return Gets the consumers set.
     */
    public ObjectSet<Consumer> getConsumers() {
        return consumers;
    }
}