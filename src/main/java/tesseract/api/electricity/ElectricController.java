package tesseract.api.electricity;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
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
public class ElectricController extends Controller<IElectricCable, IElectricNode> implements IElectricEvent {

    private long totalVoltage, totalAmperage, lastVoltage, lastAmperage;
    private final Long2ObjectMap<ElectricHolder> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectMap<IElectricNode, List<ElectricConsumer>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.

     * @param dim The dimension id.
     */
    public ElectricController(int dim) {
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

        for (Long2ObjectMap.Entry<Cache<IElectricNode>> e : group.getNodes().long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            IElectricNode producer = e.getValue().value();

            if (producer.canOutput()) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (producer.canOutput(direction)) {
                        List<ElectricConsumer> consumers = new ObjectArrayList<>();
                        long side = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(side)) {
                            onCheck(producer, consumers, null, side);
                        } else {
                            Grid<IElectricCable> grid = group.getGridAt(side, direction);
                            if (grid != null) {
                                for (Path<IElectricCable> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        Node target = path.target();
                                        assert target != null;
                                        onCheck(producer, consumers, path, target.asLong());
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

        for (List<ElectricConsumer> consumers : data.values()) {
            consumers.sort(Comparator.comparingInt(ElectricConsumer::getLoss).reversed());
        }
    }

    /**
     * Merge the existing consumers with new ones.
     *
     * @param producer The producer node.
     * @param consumers The consumer nodes.
     */
    private void onMerge(IElectricNode producer, List<ElectricConsumer> consumers) {
        List<ElectricConsumer> existingConsumers = data.get(producer);
        for (ElectricConsumer c : consumers) {
            boolean found = false;
            for (ElectricConsumer ec : existingConsumers) {
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
     * @param pos The position of the producer.
     */
    private void onCheck(IElectricNode producer, List<ElectricConsumer> consumers, Path<IElectricCable> path, long pos) {
        IElectricNode node = group.getNodes().get(pos).value();
        if (node.canInput()) {
            ElectricConsumer consumer = new ElectricConsumer(node, path);
            int voltage = producer.getOutputVoltage() - consumer.getLoss();
            if (voltage <= 0) {
                return;
            }

            if (voltage <= node.getInputVoltage()) {
                consumers.add(consumer);
            } else {
                onNodeOverVoltage(dim, pos, voltage);
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

        for (Object2ObjectMap.Entry<IElectricNode, List<ElectricConsumer>> e : data.object2ObjectEntrySet()) {
            IElectricNode producer = e.getKey();

            int outputVoltage = producer.getOutputVoltage();
            int outputAmperage = (int) Math.min(producer.getEnergy() / outputVoltage, producer.getOutputAmperage());
            if (outputAmperage <= 0) {
                continue;
            }

            for (ElectricConsumer consumer : e.getValue()) {
                int amperage = consumer.getRequiredAmperage(outputVoltage);
                if (amperage <= 0) { // if this consumer received all the energy from the other producers
                    continue;
                }

                // remember amperes stored in this consumer
                amperage = Math.min(outputAmperage, amperage);

                // If we are here, then path had some invalid cables which not suits the limits of amps/voltage
                if (consumer.getConnection() != ConnectionType.ADJACENT && !consumer.canHandle(outputVoltage, amperage)) {
                    // Find corrupt cables and return
                    for (Long2ObjectMap.Entry<IElectricCable> c : consumer.getFull().long2ObjectEntrySet()) {
                        long pos = c.getLongKey();
                        IElectricCable cable = c.getValue();

                        switch (cable.getHandler(outputVoltage, amperage)) {
                            case FAIL_VOLTAGE:
                                onCableOverVoltage(dim, pos, outputVoltage);
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
                    for (Long2ObjectMap.Entry<IElectricCable> c : consumer.getCross().long2ObjectEntrySet()) {
                        long pos = c.getLongKey();
                        IElectricCable cable = c.getValue();
                        holders.computeIfAbsent(pos, h -> new ElectricHolder(cable)).add(amperage);
                    }
                }

                long ampL = amperage; // cast here
                long inserted = (outputVoltage - consumer.getLoss()) * ampL;
                long extracted = outputVoltage * ampL;

                totalVoltage += extracted;
                totalAmperage += ampL;

                consumer.insert(inserted, false);
                producer.extract(extracted, false);

                outputAmperage -= amperage;
                if (outputAmperage <= 0) {
                    break;
                }
            }
        }

        for (Long2ObjectMap.Entry<ElectricHolder> e : holders.long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            ElectricHolder holder = e.getValue();

            // TODO: Find proper path to destroy

            if (holder.isOverAmperage()) {
                onCableOverAmperage(dim, pos, holder.getAmperage());
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
        return new ElectricController(dim).set(group);
    }
}