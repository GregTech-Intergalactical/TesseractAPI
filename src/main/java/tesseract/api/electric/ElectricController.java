package tesseract.api.electric;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.ConnectionType;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Pos;

import java.util.Map;

/**
 *
 */
public class ElectricController implements ITickingController {
    private IElectricEvent event;
    private Long2ObjectMap<Holder> amps;
    private Object2ObjectMap<IElectricNode, ObjectList<Consumer>> data;
    Group<IElectricCable, IElectricNode> group;

    /**
     * Creates instance of the controller.
     *
     * @param group The group this controller handles.
     * @param event The event listener.
     */
    public ElectricController(Group<IElectricCable, IElectricNode> group, IElectricEvent event) {
        this.event = event;
        this.amps = new Long2ObjectLinkedOpenHashMap<>();
        this.data = new Object2ObjectLinkedOpenHashMap<>();
        this.group = group;
    }

    /**
     * Executes when the group structure has changed.
     * <p>
     * First, it clears previous controller map, after it lookup for the position of node and looks for the around grids.
     * Second, it collects all producers and collectors for the grid and stores it into controller map.
     * Finally, it will prebuild consumer objects which are available for the producers. So each producer has a list of possible
     * consumers with unique information about paths, loss, ect. Therefore production object will be act as double iterated map.
     * </p>
     * @see tesseract.graph.Grid (Cache)
     */
    @Override
    public void change() {
        data.clear();
        for (Long2ObjectMap.Entry<Connectivity.Cache<IElectricNode>> entry : group.getNodes().long2ObjectEntrySet()) {
            IElectricNode producer = entry.getValue().value();
            long pos = entry.getLongKey();

            if (producer.canOutput() && producer.getOutputAmperage() > 0) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (producer.canOutput(direction)) {
                        ObjectList<Consumer> consumers = new ObjectArrayList<>();
                        long offset = position.offset(direction).asLong();
                        if (group.getNodes().containsKey(offset)) {
                            Connectivity.Cache<IElectricNode> nb = group.getNodes().get(offset);
                            if (nb != null) {
                                checkConsumer(producer, consumers, null, offset);
                            }
                        } else {
                            Grid<IElectricCable> grid = group.getGridAt(offset, direction);
                            if (grid != null) {
                                for (Path<IElectricCable> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        long target = path.target().asLong();
                                        checkConsumer(producer, consumers, path, target);
                                    }
                                }
                            }
                        }

                        if (!consumers.isEmpty()) {
                            if (!data.containsKey(producer)) {
                                data.put(producer, consumers);
                            } else {
                                mergeConsumers(producer, consumers);
                            }
                        }
                    }
                }
            }
        }
    }

    private void mergeConsumers(IElectricNode producer, ObjectList<Consumer> consumers) {
        ObjectList<Consumer> existingConsumers = data.get(producer);
        for (Consumer c : consumers) {
            boolean found = false;
            for (Consumer ec : existingConsumers){
                if (ec.consumer == c.consumer) {
                    found = true;
                    if (ec.loss > c.loss) {
                        ec.loss = c.loss;
                        ec.cross = c.cross;
                        ec.full = c.full;
                    }
                }
                if (!found) existingConsumers.add(c);
            }
        }
    }

    private void checkConsumer(IElectricNode producer, ObjectList<Consumer> consumers, Path<IElectricCable> path, long pos) {
        IElectricNode c = group.getNodes().get(pos).value();
        if (c.canInput()) {
            if (producer.getOutputVoltage() > c.getInputVoltage()) {
                event.onOverVoltage(pos);
            } else {
                Consumer consumer = new Consumer(c, path);
                if (producer.getOutputVoltage() > consumer.getLoss())
                    consumers.add(consumer);
            }
        }
    }

    @Override
    public ITickingController clone(INode group) {
        assert (group instanceof Group<?, ?>);
        return new ElectricController((Group<IElectricCable, IElectricNode>) group, event);
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
    public void tick() {
        // TODO: maybe preallocate the map in change()
        Object2LongMap<IElectricNode> amperesConsumed = new Object2LongOpenHashMap<>(group.getNodes().size());
        amperesConsumed.defaultReturnValue(0);
        amps.clear();
        for(Map.Entry<IElectricNode, ObjectList<Consumer>> e : data.entrySet()) {
            IElectricNode producer = e.getKey();
            long outputVoltage = producer.getOutputVoltage();
            long outputAmperage = producer.getOutputAmperage();
            if (outputAmperage <= 0)
                continue;
            for (Consumer consumer: e.getValue()) {
                long amperage = consumer.getRequiredAmperage(outputVoltage);
                // look up how much it already got
                amperage -= amperesConsumed.getLong(consumer.consumer);
                if (amperage <= 0)  // if this consumer received all the energy from the other producers
                    continue;
                amperage = Math.min(outputAmperage, amperage);
                // remember amperes stored in this consumer
                amperesConsumed.put(consumer.consumer, amperesConsumed.getLong(consumer.consumer) + amperage);

                consumer.insert(outputVoltage * amperage);
                producer.extract(outputVoltage * amperage, false);

                // If we are here, then path had some invalid cables which not suits the limits of amps/voltage
                if (!consumer.canReceive(outputVoltage, amperage) && consumer.getConnectionType() != ConnectionType.ADJACENT) { // Fast check by the lowest cost cable
                    // Find corrupt cable and return
                    for (Long2ObjectMap.Entry<IElectricCable> entry : consumer.getCables(false).long2ObjectEntrySet()) {
                        if (!entry.getValue().canHandle(outputVoltage, amperage)) {
                            event.onOverAmperage(entry.getLongKey());
                        }
                    }
                    return;
                }

                // Stores the amp into holder for path only for variate connection
                if (consumer.getConnectionType() == ConnectionType.VARIATE) {
                    for (Long2ObjectMap.Entry<IElectricCable> entry : consumer.getCables(true).long2ObjectEntrySet()) {
                        Holder holder = amps.get(entry.getLongKey());
                        if (holder == null) {
                            amps.put(entry.getLongKey(), new Holder(entry.getValue().getAmps(), amperage));
                        } else {
                            holder.add(amperage);
                        }
                    }
                }
                outputAmperage -= amperage;
                if (outputAmperage <= 0)
                    break;
            }

            for (Long2ObjectMap.Entry<Holder> entry : amps.long2ObjectEntrySet()) {
                if (!entry.getValue().canHandle()) {
                    event.onOverAmperage(entry.getLongKey());
                }
            }
        }
    }

    /**
     * A class that acts as holder of the amps that has passed.
     */
    private static class Holder {

        private long max;
        private long amperage;

        /**
         * Creates instance of the holder.
         *
         * @param max The cable amperage limit.
         * @param amperage The current amps amount.
         */
        Holder(long max, long amperage) {
            this.max = max;
            this.amperage = amperage;
        }

        /**
         * Adds a new value to the amperage.
         * @param amperage The added value.
         */
        void add(long amperage) {
            this.amperage += amperage;
        }

        /**
         * @return Checks that the cable is able to transfer energy.
         */
        boolean canHandle() {
            return max >= amperage;
        }
    }

    /**
     * A class that acts as a container for a consumer.
     */
    private static class Consumer {

        private long loss = 0;
        final private long amperage;
        final private IElectricNode consumer;
        private Long2ObjectMap<IElectricCable> full;
        private Long2ObjectMap<IElectricCable> cross;

        private long min_voltage = Long.MAX_VALUE;
        private long min_amperage = Long.MAX_VALUE;

        /**
         * Creates instance of the consumer.
         *
         * @param consumer The consumer node.
         * @param path The path information.
         */
        Consumer(IElectricNode consumer, Path<IElectricCable> path) {
            this.consumer = consumer;
            this.full = (path == null) ? null : path.getFull();
            this.cross = (path == null) ? null : path.getCross();

            // Gets the total loss and min voltage and amperage
            if (full != null) {
                for (IElectricCable cable : full.values()) {
                    loss += cable.getLoss();
                    min_voltage = Math.min(min_voltage, cable.getVoltage());
                    min_amperage = Math.min(min_amperage, cable.getAmps());
                }
            }
            amperage = consumer.getInputAmperage();
        }

        /**
         * Adds energy to the node. Returns quantity of energy that was accepted.
         * @param energy Amount of energy to be inserted.
         */
        void insert(long energy) {
            consumer.insert(energy, false);
        }

        /**
         * @param crossroad If true will return crossroad cables, false to get full amount.
         * @return Gets the cables list.
         */
        Long2ObjectMap<IElectricCable> getCables(boolean crossroad) {
            return crossroad ? cross : full;
        }

        /**
         * @return Gets the cables loss.
         */
        long getLoss() {
            return loss;
        }

        /**
         * @return Gets the amperage required for the consumer.
         */
        long getRequiredAmperage(long voltage) {
            return Math.min(((consumer.getCapacity() - consumer.getPower()) + voltage - 1) / voltage, amperage);
        }

        /**
         * @return Gets the consumer connection type.
         */
        ConnectionType getConnectionType() {
            if (cross == null || cross.size() == 0) {
                return (full == null) ? ConnectionType.ADJACENT : ConnectionType.SINGLE;
            }
            return ConnectionType.VARIATE;
        }

        /**
         * @param voltage The current voltage.
         * @param amperage The current amperage.
         * @return Checks that the consumer is able to receive energy.
         */
        boolean canReceive(long voltage, long amperage) {
            return this.min_voltage >= voltage && this.min_amperage >= amperage;
        }
    }
}