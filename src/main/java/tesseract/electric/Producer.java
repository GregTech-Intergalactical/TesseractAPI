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
        //consumers.size();
        graph.findGroup(position).ifPresent(group -> {
            consumers.clear();

            for (Grid<IElectricCable> grid : group.findGrids(position)) {
                for (Path<IElectricCable> path : grid.getPaths(position)) {
                    if (!path.isEmpty()) {
                        graph.findAt(path.target().get()).asEndpoint().ifPresent(consumer -> {
                            if (consumer.canInput()) {
                                if (node.getOutputVoltage() > consumer.getInputVoltage()) {
                                    // Explode
                                } else {
                                    consumers.add(new Consumer(consumer, path));
                                }
                            }
                        });
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
    /*private void add(IElectricNode consumer, Path<IElectricCable> path) {

    }*/

    /**
     * @return Gets if this producer can have energy extracted.
     */
    public boolean canOutput() {
        return node.canOutput();
    }

    /**
     * @return Gets the output amperage.
     */
    public long getOutputAmperage() {
        return node.getOutputAmperage();
    }

    /**
     * @return Gets the output voltage.
     */
    public long getOutputVoltage() {
        return node.getOutputVoltage();
    }

    /**
     * Extracts the packet from producers's buffer.
     * @param packet The provided packet.
     */
    public void extractEnergy(Packet packet) {
        node.extract(packet.getUsed() * packet.getAmps(), false);
    }

    /**
     * @return Gets the consumers array.
     */
    public ObjectSet<Consumer> getConsumers() {
        return consumers;
    }
}