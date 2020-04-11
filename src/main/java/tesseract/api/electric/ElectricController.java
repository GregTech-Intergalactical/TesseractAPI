package tesseract.api.electric;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.ConnectionType;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static tesseract.TesseractAPI.GLOBAL_ELECTRIC_EVENT;

/**
 *
 */
public class ElectricController implements ITickingController {

    private int dim;
    private Group<IElectricCable, IElectricNode> group;
    private Long2ObjectMap<Holder> absorbs = new Long2ObjectLinkedOpenHashMap<>();
    private Object2LongMap<IElectricNode> obtains = new Object2LongLinkedOpenHashMap<>();
    private Object2ObjectMap<IElectricNode, ObjectList<Consumer>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    public ElectricController(int dim, Group<IElectricCable, IElectricNode> group) {
        this.dim = dim;
        this.group = group;

        if (GLOBAL_ELECTRIC_EVENT == null) {
            throw new NullPointerException("GLOBAL_ELECTRIC_EVENT wasn't initialize");
        }
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
                            checkConsumer(producer, consumers, null, offset);
                        } else {
                            Grid<IElectricCable> grid = group.getGridAt(offset, direction);
                            if (grid != null) {
                                for (Path<IElectricCable> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        checkConsumer(producer, consumers, path, path.target().asLong());
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

    /**
     *
     *
     * @param producer
     * @param consumers
     */
    private void mergeConsumers(IElectricNode producer, ObjectList<Consumer> consumers) {
        ObjectList<Consumer> existingConsumers = data.get(producer);
        for (Consumer c : consumers) {
            boolean found = false;
            for (Consumer ec : existingConsumers) {
                if (ec.consumer == c.consumer) {
                    found = true;
                    if (ec.loss > c.loss) {
                        ec.loss = c.loss;
                        ec.full = c.full;
                        ec.cross = c.cross;
                    }
                }
                if (!found) existingConsumers.add(c);
            }
        }
    }

    /**
     *
     *
     * @param producer
     * @param consumers
     * @param path
     * @param pos
     */
    private void checkConsumer(IElectricNode producer, ObjectList<Consumer> consumers, Path<IElectricCable> path, long pos) {
        IElectricNode c = group.getNodes().get(pos).value();
        if (c.canInput()) {
            if (producer.getOutputVoltage() > c.getInputVoltage()) {
                GLOBAL_ELECTRIC_EVENT.onNodeOverVoltage(dim, pos);
            } else {
                Consumer consumer = new Consumer(c, path);
                if (producer.getOutputVoltage() > consumer.loss) {
                    consumers.add(consumer);
                }
            }
        }
    }

    /**
     *
     *
     * @param group The new group.
     * @return
     */
    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public ITickingController clone(@Nonnull INode group) {
        assert (group instanceof Group<?, ?>);
        return new ElectricController(dim, (Group<IElectricCable, IElectricNode>) group);
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
        obtains.clear();
        absorbs.clear();

        for(Object2ObjectMap.Entry<IElectricNode, ObjectList<Consumer>> e : data.object2ObjectEntrySet()) {
            IElectricNode producer = e.getKey();
            long outputVoltage = producer.getOutputVoltage();
            long outputAmperage = producer.getOutputAmperage();
            if (outputAmperage <= 0) {
                continue;
            }

            for (Consumer consumer: e.getValue()) {
                long amperage = consumer.getRequiredAmperage(outputVoltage);

                // look up how much it already got
                amperage -= obtains.getLong(consumer.consumer);
                if (amperage <= 0) { // if this consumer received all the energy from the other producers
                    continue;
                }

                // remember amperes stored in this consumer
                amperage = Math.min(outputAmperage, amperage);
                obtains.put(consumer.consumer, obtains.getLong(consumer.consumer) + amperage);

                consumer.insert(outputVoltage * amperage, false);
                producer.extract(outputVoltage * amperage, false);

                // If we are here, then path had some invalid cables which not suits the limits of amps/voltage
                if (!consumer.canReceive(outputVoltage, amperage) && consumer.connection != ConnectionType.ADJACENT) { // Fast check by the lowest cost cable
                    // Find corrupt cable and return
                    for (Long2ObjectMap.Entry<IElectricCable> entry : consumer.full.long2ObjectEntrySet()) {
                        switch (entry.getValue().getStatus(outputVoltage, amperage)) {
                            case FAIL_VOLTAGE:
                                GLOBAL_ELECTRIC_EVENT.onCableOverVoltage(dim, entry.getLongKey());
                                break;
                            case FAIL_AMPERAGE:
                                GLOBAL_ELECTRIC_EVENT.onCableOverAmperage(dim, entry.getLongKey());
                                break;
                        }
                    }
                    return;
                }

                // Stores the amp into holder for path only for variate connection
                if (consumer.connection == ConnectionType.VARIATE) {
                    for (Long2ObjectMap.Entry<IElectricCable> entry : consumer.cross.long2ObjectEntrySet()) {
                        Holder h = absorbs.get(entry.getLongKey());
                        if (h == null) {
                            absorbs.put(entry.getLongKey(), new Holder(entry.getValue().getAmps(), amperage));
                        } else {
                            h.add(amperage);
                        }
                    }
                }

                outputAmperage -= amperage;
                if (outputAmperage <= 0)
                    break;
            }
        }

        for (Long2ObjectMap.Entry<Holder> entry : absorbs.long2ObjectEntrySet()) {
            if (!entry.getValue().canHandle()) {
                GLOBAL_ELECTRIC_EVENT.onCableOverAmperage(dim, entry.getLongKey());
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
         *
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

        private long loss;
        private final long amperage;
        private final IElectricNode consumer;
        private final ConnectionType connection;
        private Long2ObjectMap<IElectricCable> full;
        private Long2ObjectMap<IElectricCable> cross;

        private long minVoltage = Long.MAX_VALUE;
        private long minAmperage = Long.MAX_VALUE;

        /**
         * Creates instance of the consumer.
         *
         * @param consumer The consumer node.
         * @param path The path information.
         */
        Consumer(@Nonnull IElectricNode consumer, @Nullable Path<IElectricCable> path) {
            this.consumer = consumer;

            if (path != null) {
                full = path.getFull();
                cross = path.getCross();
            }

            // Gets the total loss and min voltage and amperage
            if (full != null) {
                for (IElectricCable cable : full.values()) {
                    loss += cable.getLoss();
                    minVoltage = Math.min(minVoltage, cable.getVoltage());
                    minAmperage = Math.min(minAmperage, cable.getAmps());
                }
            }

            if (cross == null || cross.size() == 0) {
                connection = (full == null) ? ConnectionType.ADJACENT : ConnectionType.SINGLE;
            } else {
                connection = ConnectionType.VARIATE;
            }

            amperage = consumer.getInputAmperage();
        }

        /**
         * Adds energy to the node. Returns quantity of energy that was accepted.
         *
         * @param energy Amount of energy to be inserted.
         * @param simulate If true, the insertion will only be simulated.
         */
        void insert(long energy, boolean simulate) {
            consumer.insert(energy, simulate);
        }

        /**
         * @return Gets the amperage required for the consumer.
         */
        long getRequiredAmperage(long voltage) {
            return Math.min(((consumer.getCapacity() - consumer.getPower()) + voltage - 1) / voltage, amperage);
        }

        /**
         * @param voltage The current voltage.
         * @param amperage The current amperage.
         * @return Checks that the consumer is able to receive energy.
         */
        boolean canReceive(long voltage, long amperage) {
            return minVoltage >= voltage && minAmperage >= amperage;
        }
    }
}