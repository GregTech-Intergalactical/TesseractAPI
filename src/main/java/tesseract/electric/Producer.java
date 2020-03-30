package tesseract.electric;

import it.unimi.dsi.fastutil.ints.Int2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricEvent;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.*;

/**
 * A class that acts as a container for a producer.
 */
public class Producer implements IListener {

    private long position;
    private IElectricNode node;
    private Int2LongMap checks;
    private Long2ObjectMap<Cable> ampers;
    private ObjectList<Consumer> consumers;
    private Graph<IElectricCable, IElectricNode> graph;
    private IElectricEvent event;

    /**
     * Creates instance of the producer.
     *
     * @param graph The graph instance.
     * @param position The position of node.
     * @param node The producer node.
     * @param event The event listener.
     */
    protected Producer(Graph<IElectricCable, IElectricNode> graph, long position, IElectricNode node, IElectricEvent event) {
        this.node = node;
        this.graph = graph;
        this.event = event;
        this.position = position;
        this.consumers = new ObjectArrayList<>();
        this.checks = new Int2LongLinkedOpenHashMap();
        this.ampers = new Long2ObjectLinkedOpenHashMap<>();
    }

    @Override
    public void update() {
        graph.findGroup(position).ifPresent(group -> {
            consumers.clear();

            for (Grid<IElectricCable> grid : group.findGrids(position)) {
                for (Path<IElectricCable> path : grid.getPaths(position)) {
                    if (!path.isEmpty()) {
                        long pos = path.target().get();
                        graph.findAt(pos).asEndpoint().ifPresent(consumer -> {
                            if (consumer.canInput()) {
                                if (node.getOutputVoltage() > consumer.getInputVoltage()) {
                                    event.onOverVoltage(pos); // Here we can send pos or consumer ref
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
     * .
     */
    public void send() {
        if (!node.canOutput()) {
            return;
        }

        try {
            long amps = node.getOutputAmperage();

            for (int i = 0; i < consumers.size(); i++) {
                if (amps <= 0) break;

                Consumer consumer = consumers.get(i);
                if (consumer.isValid()) {
                    Packet energy = consumer.getEnergyRequired(node.getOutputVoltage());
                    long amperage = energy.update(amps); // Update amps to the available amount

                    node.extract(energy.getUsed() * energy.getAmps(), false);
                    consumer.insert(energy.getSend() * energy.getAmps(), false);

                    // If we are here, then path had some invalid cables which not suits the limits of amps/voltage
                    if (!consumer.canReceive(energy)) { // Fast check by the lowest cost cable
                        // Find corrupt cable and return
                        for (Long2ObjectMap.Entry<IElectricCable> entry : consumer.getCables(false).long2ObjectEntrySet()) {
                            if (!entry.getValue().canHandle(energy)) {
                                event.onOverAmperage(entry.getLongKey());
                            }
                        }
                        return;
                    }

                    checks.put(i, energy.getAmps());

                    amps = amperage;
                }
            }

            if (checks.size() > 1) {
                for (Int2LongMap.Entry check : checks.int2LongEntrySet()) {

                    Consumer consumer = consumers.get(check.getIntKey());
                    switch (consumer.getConnectionType()) {
                        case VARIATE:
                            for (Long2ObjectMap.Entry<IElectricCable> entry : consumer.getCables(true).long2ObjectEntrySet()) {
                                Cable cable = ampers.get(entry.getLongKey());
                                if (cable == null) {
                                    ampers.put(entry.getLongKey(), new Cable(entry.getValue(), check.getLongValue()));
                                } else {
                                    cable.add(check.getLongValue());
                                }
                            }
                            break;
                        case SINGLE:
                        case ADJACENT:
                            return;
                        default:
                            throw new IllegalStateException();
                    }
                }

                for (Long2ObjectMap.Entry<Cable> entry : ampers.long2ObjectEntrySet()) {
                    if (!entry.getValue().isValid()) {
                        event.onOverAmperage(entry.getLongKey());
                    }
                }
            }
        } finally {
            ampers.clear();
            checks.clear();
        }
    }
}