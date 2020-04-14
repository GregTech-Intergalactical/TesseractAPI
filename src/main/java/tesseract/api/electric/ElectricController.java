package tesseract.api.electric;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.Absorber;
import tesseract.api.ConnectionType;
import tesseract.api.Controller;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static tesseract.TesseractAPI.GLOBAL_ELECTRIC_EVENT;

/**
 * Class acts as a controller in the group of an electrical components.
 */
public class ElectricController extends Controller<IElectricCable, IElectricNode> {

    private Long2ObjectMap<Absorber> absorbs = new Long2ObjectLinkedOpenHashMap<>();
    private Object2IntMap<IElectricNode> obtains = new Object2IntLinkedOpenHashMap<>();
    private Object2ObjectMap<IElectricNode, ObjectList<Consumer>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    public ElectricController(int dim, Group<IElectricCable, IElectricNode> group) {
        super(dim, group);
    }

    /**
     * Executes when the group structure has changed.
     * <p>
     * First, it clears previous controller map, after it lookup for the position of node and looks for the around grids.
     * Second, it collects all producers and collectors for the grid and stores it into data map.
     * Finally, it will pre-build consumer objects which are available for the producers. So each producer has a list of possible
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
     * Merge the existing consumers with new ones.
     *
     * @param producer The producer node.
     * @param consumers The consumer nodes.
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
     * @param producer The producer node.
     * @param consumers The consumer nodes.
     * @param path The paths to consumers.
     * @param pos The position of the producer.
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
     * Create new controller for split group.
     *
     * @param group The new group.
     * @return New controller for the group.
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
            int outputVoltage = producer.getOutputVoltage();
            int outputAmperage = producer.getOutputAmperage();
            if (outputAmperage <= 0) {
                continue;
            }

            for (Consumer consumer: e.getValue()) {
                int amperage = consumer.getRequiredAmperage(outputVoltage);

                // look up how much it already got
                amperage -= obtains.getInt(consumer.consumer);
                if (amperage <= 0) { // if this consumer received all the energy from the other producers
                    continue;
                }

                // remember amperes stored in this consumer
                amperage = Math.min(outputAmperage, amperage);
                obtains.put(consumer.consumer, obtains.getInt(consumer.consumer) + amperage);

                consumer.insert((outputVoltage - consumer.loss) * (long) amperage, false);
                producer.extract(outputVoltage * (long) amperage, false);

                // If we are here, then path had some invalid cables which not suits the limits of amps/voltage
                if (!consumer.canReceive(outputVoltage, amperage) && consumer.connection != ConnectionType.ADJACENT) { // Fast check by the lowest cost cable
                    // Find corrupt cable and return
                    for (Long2ObjectMap.Entry<IElectricCable> cable : consumer.full.long2ObjectEntrySet()) {
                        switch (cable.getValue().handleStatus(outputVoltage, amperage)) {
                            case FAIL_VOLTAGE:
                                GLOBAL_ELECTRIC_EVENT.onCableOverVoltage(dim, cable.getLongKey());
                                break;
                            case FAIL_AMPERAGE:
                                GLOBAL_ELECTRIC_EVENT.onCableOverAmperage(dim, cable.getLongKey());
                                break;
                        }
                    }
                    return;
                }

                // Stores the amp into holder for path only for variate connection
                if (consumer.connection == ConnectionType.VARIATE) {
                    for (Long2ObjectMap.Entry<IElectricCable> cable : consumer.cross.long2ObjectEntrySet()) {
                        Absorber a = absorbs.get(cable.getLongKey());
                        if (a == null) {
                            absorbs.put(cable.getLongKey(), new Absorber(cable.getValue().getAmps(), amperage));
                        } else {
                            a.add(amperage);
                        }
                    }
                }

                outputAmperage -= amperage;
                if (outputAmperage <= 0)
                    break;
            }
        }

        for (Long2ObjectMap.Entry<Absorber> entry : absorbs.long2ObjectEntrySet()) {
            if (!entry.getValue().canHandle()) {
                GLOBAL_ELECTRIC_EVENT.onCableOverAmperage(dim, entry.getLongKey());
            }
        }
    }

    /**
     * A class that acts as a container for a consumer.
     */
    private static class Consumer {

        private final int amperage;
        private final IElectricNode consumer;
        private final ConnectionType connection;

        private Long2ObjectMap<IElectricCable> full;
        private Long2ObjectMap<IElectricCable> cross;

        private int loss;
        private int minVoltage = Integer.MAX_VALUE;
        private int minAmperage = Integer.MAX_VALUE;

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
         * @param maxRecieve Amount of energy to be inserted.
         * @param simulate If true, the insertion will only be simulated.
         */
        void insert(long maxRecieve, boolean simulate) {
            consumer.insert(maxRecieve, simulate);
        }

        /**
         * @return Gets the amperage required for the consumer.
         */
        int getRequiredAmperage(int voltage) {
            return (int) Math.min(((consumer.getCapacity() - consumer.getPower()) + voltage - 1) / voltage, amperage);
        }

        /**
         * @param voltage The current voltage.
         * @param amperage The current amperage.
         * @return Checks that the consumer is able to receive energy.
         */
        boolean canReceive(int voltage, int amperage) {
            return minVoltage >= voltage && minAmperage >= amperage;
        }
    }
}