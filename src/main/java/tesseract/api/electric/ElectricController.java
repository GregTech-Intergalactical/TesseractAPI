package tesseract.api.electric;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.GraphWrapper;
import tesseract.graph.*;

/**
 * @es
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
    protected ElectricController(Graph<IElectricCable, IElectricNode> graph, long position, IElectricEvent event) {
        super(graph, position);
        this.event = event;
        this.ampers = new Long2ObjectLinkedOpenHashMap<>();
        this.controller = new Object2ObjectArrayMap<>();
    }

    /**
     * Called when grid is update.
     * <p>
     * Method is execute mainly for a primary node in Grid. This node will act as main update controller.
     * Firstly, it clear previous controller map, after it lookup for the position of node and looks for the around grids.
     * Secondly, it collect all producers and collectors for the grid and store it into controller map.
     * Finally, it will prebuilt consumer objects which are available for the producers. So each producer has a list of possible
     * consumers with unique information about paths, loss, ect. Therefore production object will be act as double iterated map.
     * </p>
     * @see tesseract.graph.Grid (Listener)
     * @param primary If true will be consider as a first node in grid.
     */
    @Override
    public void change(boolean primary) {
        controller.clear();

        // If true then producer will act as controller
        if (primary) {
            graph.findGroup(position).ifPresent(group -> {
                for (Grid<IElectricCable> grid : group.findGrids(position)) {
                    for (long pos : grid.getNodes().keySet()) {
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
                                            long voltage = producer.getOutputVoltage() - electric.getLoss();
                                            if (voltage > 0) {
                                                electric.setVoltage(voltage);
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
     * Call on the updates to send energy.
     * <p>
     * Most of the magic going in producer class which acts as wrapper double iterator around controller map.
     * Firstly, method will look for the available producer and consumer.
     * Secondly, some amperage calculation is going using the consumer and producer data.
     * Thirdly, it will check the voltage and amperage for the single impulse by the lowest cost cable.
     * </p>
     * If that function will find corrupted cables, it will execute loop to find the corrupted cables and exit.
     * However, if corrupted cables wasn't found, it will looks for variate connection type and store the amp for that path.
     * After energy was send, loop will check the amp holder instances on ampers map to find cross-nodes where amps/voltage is exceed max limit.
     */
    @Override
    public void update() {
        if (controller.size() == 0) return;

        try {
            long amperage;

            ElectricProducer producer = new ElectricProducer(controller);// loop:
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

                // Stores the amp into holder for path only for variate connection
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
                        break;
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