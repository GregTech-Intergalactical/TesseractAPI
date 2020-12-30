package tesseract.api.gt;

import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.ConnectionType;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.Comparator;
import java.util.List;

/**
 * Class acts as a controller in the group of an electrical components.
 */
public class GTController extends Controller<IGTCable, IGTNode> implements IGTEvent {

    private long totalVoltage, totalAmperage, lastVoltage, lastAmperage;
    private final Long2LongMap holders = new Long2LongLinkedOpenHashMap();
    private final Object2IntMap<IGTNode> obtains = new Object2IntOpenHashMap<>();
    private final Object2ObjectMap<IGTNode, List<GTConsumer>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.

     * @param dim The dimension id.
     */
    public GTController(int dim) {
        super(dim);
    }

    /**
     * Executes when the group structure has changed.
     * <p>
     * First, it clears previous controller map, after it lookup for the position of node and looks for the around grids.
     * Second, it collects all producers and collectors for the grid and stores it into data map.
     * Finally, it will pre-build consumer objects which are available for the producers. So each producer has a list of possible
     * consumers with unique information about paths, loss, ect.
     * </p>
     * @see tesseract.graph.Grid (Cache)
     */
    @Override
    public void change() {
        data.clear();

        for (Long2ObjectMap.Entry<Cache<IGTNode>> e : group.getNodes().long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            IGTNode producer = e.getValue().value();

            if (producer.canOutput()) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (producer.canOutput(direction)) {
                        List<GTConsumer> consumers = new ObjectArrayList<>();
                        long side = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(side)) {
                            onCheck(producer, consumers, null, pos,side);
                        } else {
                            Grid<IGTCable> grid = group.getGridAt(side, direction);
                            if (grid != null) {
                                for (Path<IGTCable> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        Node target = path.target();
                                        assert target != null;
                                        onCheck(producer, consumers, path,pos, target.asLong());
                                    }
                                }
                            }
                        }

                        if (!consumers.isEmpty()) {
                            if (data.containsKey(producer)) {
                                onMerge(producer, consumers);
                            } else {
                                data.put(producer, consumers);
                            }
                        }
                    }
                }
            }
        }

        for (List<GTConsumer> consumers : data.values()) {
            consumers.sort(GTConsumer.COMPARATOR);
        }
    }

    /**
     * Merge the existing consumers with new ones.
     *
     * @param producer The producer node.
     * @param consumers The consumer nodes.
     */
    private void onMerge(IGTNode producer, List<GTConsumer> consumers) {
        List<GTConsumer> existingConsumers = data.get(producer);
        for (GTConsumer c : consumers) {
            boolean found = false;
            for (GTConsumer ec : existingConsumers) {
                if (ec.getNode() == c.getNode()) {
                    found = true;
                    if (ec.getLoss() > c.getLoss()) {
                        ec.copy(c);
                    }
                }
                if (!found) existingConsumers.add(c);
            }
        }
    }

    /**
     * Adds available consumers to the list.
     *
     * @param producer The producer node.
     * @param consumers The consumer nodes.
     * @param path The paths to consumers.
     * @param consumerPos The position of the consumer.
     * @param producerPos The position of the producer.
     */
    private void onCheck(IGTNode producer, List<GTConsumer> consumers, Path<IGTCable> path, long producerPos, long consumerPos) {
        Cache<IGTNode> nodee = group.getNodes().get(consumerPos);
        if (nodee == null) {
            System.out.println("Error in onCheck, null cache.");
            return;
        }
        IGTNode node = nodee.value();
        Pos pos = new Pos(consumerPos).sub(new Pos(producerPos));
        Dir dir = path != null ? path.target().getDirection().getOpposite()
                : Dir.POS_TO_DIR.get(pos).getOpposite();
        if (node.canInput(dir)) {
            GTConsumer consumer = new GTConsumer(node, path);
            int voltage = producer.getOutputVoltage() - consumer.getLoss();
            if (voltage <= 0) {
                return;
            }

            if (voltage <= node.getInputVoltage()) {
                consumers.add(consumer);
            } else {
                onNodeOverVoltage(dim, consumerPos, voltage);
            }
        }
    }

    /**
     * Call on the updates to send energy.
     * <p>
     * Most of the magic going in producer class which acts as wrapper double it around controller map.
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
        super.tick();
        holders.clear();
        obtains.clear();

        for (Object2ObjectMap.Entry<IGTNode, List<GTConsumer>> e : data.object2ObjectEntrySet()) {
            IGTNode producer = e.getKey();

            // Get the how many amps and energy producer can send
            long energy = producer.getEnergy();
            int voltage_out = producer.getOutputVoltage();
            int amperage_in = producer.getOutputAmperage();
            if (amperage_in <= 0) {
                continue;
            }
            amperage_in = (int) Math.min((energy / voltage_out), amperage_in);
            if (amperage_in <= 0) { // just for sending the last piece of energy
                voltage_out = (int) energy;
                amperage_in = 1;
            }

            for (GTConsumer consumer : e.getValue()) {
                int voltage = voltage_out - consumer.getLoss();
                if (voltage <= 0) {
                    continue;
                }

                int amperage = consumer.getRequiredAmperage(voltage);

                // Look up how much it already got
                int obtained = obtains.getInt(consumer.getNode());
                amperage -= obtained;
                if (amperage <= 0) { // if this consumer received all the energy from the other producers
                    continue;
                }

                // Remember amperes stored in this consumer
                amperage = Math.min(amperage_in, amperage);
                obtains.put(consumer.getNode(), amperage + obtained);

                // If we are here, then path had some invalid cables which not suits the limits of amps/voltage
                if (consumer.getConnection() != ConnectionType.ADJACENT && !consumer.canHandle(voltage_out, amperage)) {
                    // Find corrupt cables and return
                    for (Long2ObjectMap.Entry<IGTCable> c : consumer.getFull().long2ObjectEntrySet()) {
                        long pos = c.getLongKey();
                        IGTCable cable = c.getValue();

                        switch (cable.getHandler(voltage_out, amperage)) {
                            case FAIL_VOLTAGE:
                                onCableOverVoltage(dim, pos, voltage_out);
                                break;
                            case FAIL_AMPERAGE:
                                onCableOverAmperage(dim, pos, amperage);
                                break;
                        }
                    }
                    return;
                }

                // Stores the amp into holder for path only for variate connection
                if (consumer.getConnection() == ConnectionType.VARIATE) {
                    for (Long2ObjectMap.Entry<IGTCable> c : consumer.getCross().long2ObjectEntrySet()) {
                        long pos = c.getLongKey();
                        IGTCable cable = c.getValue();

                        long holder = holders.get(pos);
                        holders.put(pos, (holder == 0L) ? GTHolder.create(cable, amperage) : GTHolder.add(holder, amperage));
                    }
                }

                long amp = amperage; // cast here
                long inserted = voltage * amp;
                long extracted = voltage_out * amp;

                totalVoltage += extracted;
                totalAmperage += amp;

                consumer.insert(inserted, false);
                producer.extract(extracted, false);

                amperage_in -= amperage;
                if (amperage_in <= 0) {
                    break;
                }
            }
        }

        for (Long2LongMap.Entry e : holders.long2LongEntrySet()) {
            long pos = e.getLongKey();
            long holder = e.getLongValue();

            // TODO: Find proper path to destroy

            if (GTHolder.isOverAmperage(holder)) {
                onCableOverAmperage(dim, pos, GTHolder.getAmperage(holder));
            }
        }
    }

    @Override
    protected void onFrame() {
        lastVoltage = totalVoltage;
        lastAmperage = totalAmperage;
        totalAmperage = totalVoltage = 0L;
    }

    @Override
    public String[] getInfo() {
        return new String[]{
            "Total Voltage: ".concat(Long.toString(lastVoltage)),
            "Total Amperage: ".concat(Long.toString(lastAmperage)),
        };
    }

    @Override
    public ITickingController clone(INode group) {
        return new GTController(dim).set(group);
    }
}