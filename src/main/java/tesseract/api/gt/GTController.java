package tesseract.api.gt;

import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tesseract.api.ConnectionType;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.api.item.ItemConsumer;
import tesseract.graph.Cache;
import tesseract.graph.Grid;
import tesseract.graph.INode;
import tesseract.graph.Path;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Class acts as a controller in the group of an electrical components.
 */
public class GTController extends Controller<Long, IGTCable, IGTNode> implements IGTEvent {

    private long totalVoltage, totalAmperage, lastVoltage, lastAmperage;
    private final Long2LongMap holders = new Long2LongLinkedOpenHashMap();
    private final Object2IntMap<IGTNode> obtains = new Object2IntOpenHashMap<>();
    private final Object2ObjectMap<IGTNode, List<GTConsumer>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.

     * @param dim The dimension id.
     */
    public GTController(Function<RegistryKey<World>, ServerWorld> supplier, RegistryKey<World> dim) {
        super(supplier,dim);
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
        //noinspection StatementWithEmptyBody
        while(!changeInternal()); // not sure how many times we may break the network while changing it
    }
    private boolean changeInternal(){
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
                            if (!onCheck(producer, consumers, null, pos,side))
                                return false;
                        } else {
                            Grid<IGTCable> grid = group.getGridAt(side, direction);
                            if (grid != null) {
                                for (Path<IGTCable> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        Node target = path.target();
                                        assert target != null;
                                        if (!onCheck(producer, consumers, path,pos, target.asLong()))
                                            return false;
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
        return true;
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
    private boolean onCheck(IGTNode producer, List<GTConsumer> consumers, Path<IGTCable> path, long producerPos, long consumerPos) {
        Cache<IGTNode> nodee = group.getNodes().get(consumerPos);
        if (nodee == null) {
            System.out.println("Error in onCheck, null cache.");
            return false;
        }
        IGTNode node = nodee.value();
        Pos pos = new Pos(consumerPos).sub(new Pos(producerPos));
        Dir dir = path != null ? path.target().getDirection()
                : Dir.POS_TO_DIR.get(pos).getOpposite();
        if (node.canInput(dir) && node.connects(dir)) {
            GTConsumer consumer = new GTConsumer(node, path);
            int voltage = producer.getOutputVoltage() - consumer.getLoss();
            if (voltage <= 0) {
                return false;
            }

            if (voltage <= node.getInputVoltage()) {
                consumers.add(consumer);
                return true;
            } else {
                //onNodeOverVoltage(getWorld(), consumerPos, voltage);
                return false;
            }
        }
        return true;
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

        for (Long2LongMap.Entry e : holders.long2LongEntrySet()) {
            long pos = e.getLongKey();
            long holder = e.getLongValue();

            // TODO: Find proper path to destroy

            if (GTHolder.isOverAmperage(holder)) {
                onCableOverAmperage(getWorld(),pos, GTHolder.getAmperage(holder));
            }
        }
        holders.clear();
    }

    @Override
    public int insert(Pos producerPos, Dir direction, Long stack, boolean simulate) {
        Cache<IGTNode> node = this.group.getNodes().get(producerPos.offset(direction).asLong());
        if (node == null) return 0;
        IGTNode producer = node.value();
        List<GTConsumer> list = this.data.get(node.value());
        if (list == null) return 0;

        // Get the how many amps and energy producer can send
        long energy = producer.getEnergy();
        int voltage_out = producer.getOutputVoltage();
        int amperage_in = producer.getOutputAmperage();
        if (amperage_in <= 0) {
            return 0;
        }
        amperage_in = (int) Math.min((energy / voltage_out), amperage_in);
        if (amperage_in <= 0) { // just for sending the last piece of energy
            voltage_out = (int) energy;
            amperage_in = 1;
        }

        for (GTConsumer consumer : list) {
            int voltage = voltage_out - consumer.getLoss();
            if (voltage <= 0) {
                continue;
            }

            int amperage = consumer.getRequiredAmperage(voltage);

            // Look up how much it already got
            //int obtained = obtains.getInt(consumer.getNode());
           // amperage -= obtained;
            if (amperage <= 0) { // if this consumer received all the energy from the other producers
                continue;
            }

            // Remember amperes stored in this consumer
            amperage = Math.min(amperage_in, amperage);
            //if (!simulate)
                //obtains.put(consumer.getNode(), amperage + obtained);

            // If we are here, then path had some invalid cables which not suits the limits of amps/voltage
            if (!simulate && consumer.getConnection() != ConnectionType.ADJACENT && !consumer.canHandle(voltage_out)) {
                // Find corrupt cables and return
                for (Long2ObjectMap.Entry<IGTCable> c : consumer.getFull().long2ObjectEntrySet()) {
                    long pos = c.getLongKey();
                    IGTCable cable = c.getValue();

                    if (cable.getHandler(voltage_out, amperage) == GTStatus.FAIL_VOLTAGE) {
                        onCableOverVoltage(getWorld(), pos, voltage_out);
                    }
                }
                return 0;
            }

            // Stores the amp into holder for path only for variate connection
            if (!simulate) {
                for (Long2ObjectMap.Entry<IGTCable> c : consumer.getFull().long2ObjectEntrySet()) {
                    long pos = c.getLongKey();
                    IGTCable cable = c.getValue();

                    long holder = holders.get(pos);
                    holders.put(pos, (holder == 0L) ? GTHolder.create(cable, amperage) : GTHolder.add(holder, amperage));

                    if (GTHolder.isOverAmperage(holders.get(pos))) {
                        onCableOverAmperage(getWorld(), pos, GTHolder.getAmperage(holders.get(pos)));
                        break;
                    }
                }
            }

            long amp = amperage; // cast here
            long extracted = voltage_out * amp;
            if (!simulate) {
                totalVoltage += extracted;
                totalAmperage += amp;
                for (int i = 0; i < amp; i++) {
                    consumer.insert(voltage, false);
                }
                return stack.intValue();
            }
            return (int) extracted;
        }
        return 0;
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
        return new GTController(WORLD_SUPPLIER, dim).set(group);
    }
}