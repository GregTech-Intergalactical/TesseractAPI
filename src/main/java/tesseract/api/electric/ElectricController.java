package tesseract.api.electric;

import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.GraphWrapper;
import tesseract.graph.*;

/**
 *
 */
public class ElectricController extends GraphWrapper implements IGridListener {

    private IElectricEvent event;
    private Long2ObjectMap<AmpHolder> ampers;
    private Object2ObjectArrayMap<IElectricNode, ObjectList<ElectricConsumer>> controller;

    /**
     * Creates instance of the controller.
     *
     * @param graph The graph instance.
     * @param position The position of node.
     * @param event The event listener.
     */
    public ElectricController(Graph<IElectricCable, IElectricNode> graph, long position, IElectricEvent event) {
        super(graph, position);
        this.event = event;
        this.ampers = new Long2ObjectLinkedOpenHashMap<>();
        this.controller = new Object2ObjectArrayMap<>();
    }

    @Override
    public void change(boolean primary) {
        controller.clear();

        // If true then producer will act as controller
        if (primary) {
            graph.findGroup(position).ifPresent(group -> {
                for (Grid<IElectricCable> grid : group.findGrids(position)) {
                    LongSet arr = grid.getNodes().keySet();
                    for (long pos : arr) {
                        IElectricNode producer = group.getNodes().get(pos).value();
                        if (producer.canOutput() && producer.getOutputAmperage() > 0) {
                            ObjectList<ElectricConsumer> consumers = new ObjectArrayList<>();

                            for (Path<IElectricCable> path : grid.getPaths(pos)) {
                                if (!path.isEmpty()) {
                                    IElectricNode consumer = group.getNodes().get(path.target().get()).value();
                                    if (consumer.canInput()) {
                                        if (producer.getOutputVoltage() > consumer.getInputVoltage()) {
                                            event.onOverVoltage(consumer);
                                        } else {
                                            ElectricConsumer electric = new ElectricConsumer(consumer, path);
                                            long require = producer.getOutputVoltage() - electric.getLoss();
                                            if (require > 0) {
                                                electric.setVoltage(require);
                                                consumers.add(electric);
                                            }
                                        }
                                    }
                                }
                            }

                            if (!consumers.isEmpty()) {
                                controller.put(producer, consumers);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * .
     */
    @Override
    public void update() {
        if (controller.size() == 0) return;

        try {
            long amperage;

            ElectricProducer producer = new ElectricProducer(controller);
            while (producer.hasNext()) {
                ElectricConsumer consumer = producer.next();
                if (consumer == null) break;

                amperage = consumer.getRequiredAmperage();
                amperage = producer.updateAmperage(amperage);

                consumer.insert(consumer.getVoltage() * amperage, false);
                producer.extract(producer.getVoltage() * amperage, false);

                // If we are here, then path had some invalid cables which not suits the limits of amps/voltage
                if (!consumer.canReceive(producer.getVoltage(), amperage)) { // Fast check by the lowest cost cable
                    // Find corrupt cable and return
                    for (IElectricCable cable : consumer.getCables(false).values()) {
                        if (!cable.canHandle(producer.getVoltage(), amperage)) {
                            event.onOverAmperage(cable);
                        }
                    }
                    return;
                }

                switch (consumer.getConnectionType()) {
                    case VARIATE:
                        for (Long2ObjectMap.Entry<IElectricCable> entry : consumer.getCables(true).long2ObjectEntrySet()) {
                            AmpHolder holder = ampers.get(entry.getLongKey());
                            if (holder == null) {
                                ampers.put(entry.getLongKey(), new AmpHolder(entry.getValue(), amperage));
                            } else {
                                holder.add(amperage);
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

            for (AmpHolder cable : ampers.values()) {
                if (!cable.canHandle()) {
                    event.onOverAmperage(cable.getCable());
                }
            }

        } finally {
            ampers.clear();
        }
    }

    /**
     * @apiNote Wrapper for a cable and to keep track of the amps that has passed.
     */
    private static class AmpHolder {

        long amperage;
        IElectricCable cable;

        /**
         * Creates instance of the holder.
         *
         * @param cable The cable node.
         * @param amperage The initial amps amount.
         */
        AmpHolder(IElectricCable cable, long amperage) {
            this.cable = cable;
            this.amperage = amperage;
        }

        /**
         * Adds a new value to the amperage.
         */
        void add(long value) {
            amperage += value;
        }

        /**
         * @return Checks that cable is able to transfer energy.
         */
        boolean canHandle() {
            return cable.getAmps() >= amperage;
        }

        /**
         * @return Gets the cables ref.
         */
        IElectricCable getCable() {
            return cable;
        }
    }
}