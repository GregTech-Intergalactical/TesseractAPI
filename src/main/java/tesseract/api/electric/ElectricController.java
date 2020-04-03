package tesseract.api.electric;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.ConnectionType;
import tesseract.api.GraphWrapper;
import tesseract.graph.*;

/**
 * @es
 */
public class ElectricController extends GraphWrapper implements IController {

    private IElectricEvent event;
    private Long2ObjectMap<Holder> amps;
    private Object2ObjectMap<IElectricNode, ObjectList<Consumer>> data;

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
        this.amps = new Long2ObjectLinkedOpenHashMap<>();
        this.data = new Object2ObjectLinkedOpenHashMap<>();
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
        data.clear();

        // If true then producer will act as controller
        if (primary) {
            graph.findGroup(position).ifPresent(group -> {
                for (Grid<IElectricCable> grid : group.findGrids(position)) {
                    for (long pos : grid.getNodes().keySet()) {
                        IElectricNode producer = group.getNodes().get(pos).value();
                        if (producer.canOutput() && producer.getOutputAmperage() > 0) {
                            ObjectList<Consumer> consumers = new ObjectArrayList<>();
                            for (Path<IElectricCable> path : grid.getPaths(pos)) {
                                if (!path.isEmpty()) {
                                    IElectricNode node = group.getNodes().get(path.target().get()).value();
                                    if (node.canInput()) {
                                        if (producer.getOutputVoltage() > node.getInputVoltage()) {
                                            event.onOverVoltage(node);
                                        } else {
                                            Consumer consumer = new Consumer(node, path);
                                            long voltage = producer.getOutputVoltage() - consumer.getLoss();
                                            if (voltage > 0) {
                                                consumer.setVoltage(voltage);
                                                consumers.add(consumer);
                                            }
                                        }
                                    }
                                }
                            }

                            if (!consumers.isEmpty()) {
                                data.put(producer, consumers);
                            }
                        }
                    }
                }
            });

            /*if (data.isEmpty() {

            }*/
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
        if (data.isEmpty()) return;

        try {
            Producer producer = new Producer(data); Consumer consumer;
            while ((consumer = producer.getConsumer()) != null) {

                long amperage = producer.getAmperage(consumer.getRequiredAmperage());

                consumer.setAmperage(consumer.getAmperage() - amperage);

                consumer.insert(consumer.getVoltage() * amperage);
                producer.extract(producer.getVoltage() * amperage);

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
                if (consumer.getConnectionType() == ConnectionType.VARIATE) {
                    for (Long2ObjectMap.Entry<IElectricCable> entry : consumer.getCables(true).long2ObjectEntrySet()) {
                        Holder holder = amps.get(entry.getLongKey());
                        if (holder == null) {
                            amps.put(entry.getLongKey(), new Holder(entry.getValue(), amperage));
                        } else {
                            holder.add(amperage);
                        }
                    }
                }
            }

            for (Holder holder : amps.values()) {
                if (!holder.canHandle()) {
                    event.onOverAmperage(holder.getCable());
                }
            }

        } finally {
            amps.clear();
        }
    }

    /**
     * A class that acts as holder of the amps that has passed.
     */
    private static class Holder {

        private long amperage;
        private IElectricCable cable;

        /**
         * Creates instance of the holder.
         *
         * @param cable The cable node.
         * @param amperage The initial amps amount.
         */
        Holder(IElectricCable cable, long amperage) {
            this.cable = cable;
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
            return cable.getAmps() >= amperage;
        }

        /**
         * @return Gets the cables object.
         */
        IElectricCable getCable() {
            return cable;
        }
    }

    /**
     * A class that acts as a container for a consumer.
     */
    private static class Consumer {

        private long loss;
        private long voltage;
        private long amperage;
        private IElectricNode consumer;
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
            this.full = path.getFull();
            this.cross = path.getCross();

            // Gets the total loss and min voltage and amperage
            for (IElectricCable cable : full.values()) {
                loss += cable.getLoss();
                min_voltage = Math.min(min_voltage, cable.getVoltage());
                min_amperage = Math.min(min_amperage, cable.getAmps());
            }
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
         * Sets the voltage.
         * @param voltage The voltage value with loss.
         */
        void setVoltage(long voltage) {
            this.voltage = voltage;
        }

        /**
         * @return Gets the voltage.
         */
        long getVoltage() {
            return voltage;
        }

        /**
         * Sets the amperage.
         * @param amperage The amperage value.
         */
        void setAmperage(long amperage) {
            this.amperage = amperage;
        }

        /**
         * @return Gets the voltage.
         */
        long getAmperage() {
            return amperage;
        }

        /**
         * Resets the amperage.
         */
        void resetAmperage() {
            amperage = consumer.getInputAmperage();
        }

        /**
         * @return Gets the amperage required for the consumer.
         */
        long getRequiredAmperage() {
            return Math.min(((consumer.getCapacity() - consumer.getPower()) + voltage - 1) / voltage, amperage);
        }

        /**
         * @return Gets the consumer connection type.
         */
        ConnectionType getConnectionType() {
            if (cross.size() == 0) {
                if (full.size() == 0) {
                    return ConnectionType.ADJACENT;
                }
                return ConnectionType.SINGLE;
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

        /**
         * @return Checks that the consumer is need energy and can receive it.
         */
        boolean isValid() {
            return consumer.getPower() < consumer.getCapacity() && loss < consumer.getInputVoltage() && amperage > 0;
        }
    }

    /**
     * A class that acts as a container for a producer.
     */
    private static class Producer {

        private int id; // Consumers global iterator between data dims
        private long voltage;
        private long amperage;

        private Consumer consumer;
        private IElectricNode producer;
        private ObjectList<Consumer> consumers;
        private ObjectIterator<IElectricNode> producers;
        private Object2ObjectMap<IElectricNode, ObjectList<Consumer>> data;

        /**
         * Creates instance of the producer.
         *
         * @param data The map with a data.
         */
        Producer(Object2ObjectMap<IElectricNode, ObjectList<Consumer>> data) {
            this.data = data;
            this.producers = data.keySet().iterator();

            /*
             * Resets the amperage on the new iteration for all consumers
             * Because we already modified amperage of the consumers before.
             */
            for (ObjectList<Consumer> consumers : data.values()) {
                for (Consumer consumer : consumers) {
                    consumer.resetAmperage();
                }
            }
        }

        /**
         * Moves to the next available producer.
         * @return Gets the next needed consumer.
         */
        Consumer getConsumer() {

            /*
             * When we shift to an another producer dim, update amps for the same consumer index.
             * This can happen when consumer receive parts of requirement amps from different producers.
             */
            long prev = (consumer != null) ? consumer.getAmperage() : -1;

            // Lookup for the available producer
            while (!isValid()) {
                if (!producers.hasNext()) return null;
                producer = producers.next();
                voltage = producer.getOutputVoltage();
                amperage = producer.getOutputAmperage();

                consumers = data.get(producer);
                consumer = null;
            }

            // Lookup for the available consumer
            boolean none = (consumer == null);
            while (consumer == null || !consumer.isValid()) {
                if (consumers.size() == id) return null;
                consumer = consumers.get(id);
                if (none && prev > 0) { //  means that we moved previous consumer to next producer
                    consumer.setAmperage(prev); none = false;
                }
                id++;
            }

            return consumer;
        }

        /**
         * @param amperage The amperage of the consumer.
         * @return Gets the amperage required for the producer.
         */
        long getAmperage(long amperage) {
            long temp = this.amperage - amperage;
            if (temp < 0) {
                amperage = this.amperage;
                this.amperage = 0;
                id--; // Moved back to ask next producer again
            } else {
                this.amperage = temp;
            }
            return amperage;
        }

        /**
         * @return Gets the producer voltage.
         */
        long getVoltage() {
            return voltage;
        }

        /**
         * Removes energy from the node. Returns quantity of energy that was removed.
         * @param energy Amount of energy to be extracted.
         */
        void extract(long energy) {
            producer.extract(energy, false);
        }

        /**
         * @return Checks that the provider can supply energy.
         */
        boolean isValid() {
            return producer != null && producer.getPower() > 0 && amperage > 0;
        }
    }
}