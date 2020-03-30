package tesseract.api.electric;

import it.unimi.dsi.fastutil.ints.Int2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.graph.*;

/**
 * A class that acts as a container for a producer.
 */
public class ElectricProducer extends ElectricWrapper implements IGridListener {

    private IElectricNode node;
    private Int2LongMap checks;
    private Long2ObjectMap<ElectricAmpHolder> ampers;
    private ObjectList<ElectricConsumer> consumers;
    private IElectricEvent event;

    /**
     * Creates instance of the producer.
     *
     * @param graph The graph instance.
     * @param position The position of node.
     * @param node The producer node.
     * @param event The event listener.
     */
    public ElectricProducer(Graph<IElectricCable, IElectricNode> graph, long position, IElectricNode node, IElectricEvent event) {
        super(graph, position);
        this.node = node;
        this.event = event;
        this.consumers = new ObjectArrayList<>();
        this.checks = new Int2LongLinkedOpenHashMap();
        this.ampers = new Long2ObjectLinkedOpenHashMap<>();
    }

    @Override
    public void onGridUpdate() {
        graph.findGroup(position).ifPresent(group -> {
            consumers.clear();

            for (Grid<IElectricCable> grid : group.findGrids(position)) {
                for (Path<IElectricCable> path : grid.getPaths(position)) {
                    if (!path.isEmpty()) {
                        graph.findAt(path.target().get()).asEndpoint().ifPresent(consumer -> {
                            if (consumer.canInput()) {
                                if (node.getOutputVoltage() > consumer.getInputVoltage()) {
                                    event.onOverVoltage(consumer); // Here we can send pos or consumer ref
                                } else {
                                    consumers.add(new ElectricConsumer(consumer, path));
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void update() {
        send();
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

                ElectricConsumer consumer = consumers.get(i);
                if (consumer.isValid()) {
                    ElectricPacket energy = consumer.getEnergyRequired(node.getOutputVoltage());
                    long amperage = energy.update(amps); // Update amps to the available amount

                    node.extract(energy.getUsed() * energy.getAmps(), false);
                    consumer.insert(energy.getSend() * energy.getAmps(), false);

                    // If we are here, then path had some invalid cables which not suits the limits of amps/voltage
                    if (!consumer.canReceive(energy)) { // Fast check by the lowest cost cable
                        // Find corrupt cable and return
                        for (IElectricCable cable : consumer.getCables(false).values()) {
                            if (!cable.canHandle(energy)) {
                                event.onOverAmperage(cable);
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

                    ElectricConsumer consumer = consumers.get(check.getIntKey());
                    switch (consumer.getConnectionType()) {
                        case VARIATE:
                            for (Long2ObjectMap.Entry<IElectricCable> entry : consumer.getCables(true).long2ObjectEntrySet()) {
                                ElectricAmpHolder cable = ampers.get(entry.getLongKey());
                                if (cable == null) {
                                    ampers.put(entry.getLongKey(), new ElectricAmpHolder(entry.getValue(), check.getLongValue()));
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

                for (ElectricAmpHolder cable : ampers.values()) {
                    if (!cable.isValid()) {
                        event.onOverAmperage(cable.getCable());
                    }
                }
            }
        } finally {
            ampers.clear();
            checks.clear();
        }
    }
}