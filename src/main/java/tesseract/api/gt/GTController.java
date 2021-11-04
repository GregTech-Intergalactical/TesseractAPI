package tesseract.api.gt;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import tesseract.Tesseract;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.graph.*;
import tesseract.util.Node;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Class acts as a controller in the group of an electrical components.
 */
public class GTController extends Controller<Long, IGTCable, IGTNode> implements IGTEvent {

    private long totalVoltage, totalAmperage, lastVoltage, lastAmperage, totalLoss, lastLoss;
    private final Long2LongMap holders = new Long2LongLinkedOpenHashMap();
    //Cable monitoring.
    private Long2LongMap frameHolders = new Long2LongLinkedOpenHashMap();
    private Long2LongMap previousFrameHolder = new Long2LongLinkedOpenHashMap();
    //private final Object2IntMap<IGTNode> obtains = new Object2IntOpenHashMap<>();
    private final Long2ObjectMap<Map<Direction, List<GTConsumer>>> data = new Long2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     */
    public GTController(World dim) {
        super(IGTNode::fromPipe, dim);
    }

    /**
     * Executes when the group structure has changed.
     * <p>
     * First, it clears previous controller map, after it lookup for the position of node and looks for the around grids.
     * Second, it collects all producers and collectors for the grid and stores it into data map.
     * Finally, it will pre-build consumer objects which are available for the producers. So each producer has a list of possible
     * consumers with unique information about paths, loss, ect.
     * </p>
     *
     * @see tesseract.graph.Grid (Cache)
     */
    @Override
    public void change() {
        if (!changeInternal()) {
            Tesseract.LOGGER.warn("Error during GTController::change.");
        }
    }

    boolean handleInput(long pos, IGTNode producer) {
        if (data.containsKey(pos)) return true;

        if (producer.canOutput()) {
            for (Direction direction : Graph.DIRECTIONS) {
                if (producer.canOutput(direction)) {
                    long side = Pos.offset(pos, direction);// position.offset(direction).asLong();
                    List<GTConsumer> consumers = new ObjectArrayList<>();

                    Grid<IGTCable> grid = group.getGridAt(side, direction);
                    if (grid != null) {
                        for (Path<IGTCable> path : grid.getPaths(pos, direction)) {
                            if (!path.isEmpty()) {
                                Node target = path.target();
                                assert target != null;
                                if (!onCheck(producer, consumers, path, pos, target.asLong()))
                                    return false;
                            }
                        }
                    }
                    if (!consumers.isEmpty())
                        data.computeIfAbsent(pos, m -> new EnumMap<>(Direction.class)).put(getMapDirection(pos, direction.getOpposite()), consumers);

                        /*if (!consumers.isEmpty()) {
                            if (data.containsKey(pos)) {
                                onMerge(producer, consumers);
                            } else {
                                data.put(producer, consumers);
                            }
                        }*/
                }
            }
        }
        return true;
    }

    private boolean changeInternal() {
        data.clear();
        for (Long2ObjectMap.Entry<NodeCache<IGTNode>> e : group.getNodes().long2ObjectEntrySet()) {
            handleInput(e.getLongKey(), e.getValue().value());
        }

        for (Long2ObjectMap.Entry<Cache<IGTCable>> entry : group.getPipes()) {
            handleInput(entry.getLongKey(), wrapPipe(entry.getValue().value()));
        }

        for (Map<Direction, List<GTConsumer>> map : data.values()) {
            for (List<GTConsumer> consumers : map.values()) {
                consumers.sort(GTConsumer.COMPARATOR);
            }
        }
        return true;
    }

    /**
     * Merge the existing consumers with new ones.
     *
     * @param producer  The producer node.
     * @param consumers The consumer nodes.
     */
    private void onMerge(IGTNode producer, List<GTConsumer> consumers) {
        /*List<GTConsumer> existingConsumers = data.get(producer);
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
        }*/
    }

    /**
     * Adds available consumers to the list.
     *
     * @param producer    The producer node.
     * @param consumers   The consumer nodes.
     * @param path        The paths to consumers.
     * @param consumerPos The position of the consumer.
     * @param producerPos The position of the producer.
     * @return whether or not an issue arose checking node.
     */
    private boolean onCheck(IGTNode producer, List<GTConsumer> consumers, Path<IGTCable> path, long producerPos, long consumerPos) {
        NodeCache<IGTNode> nodee = group.getNodes().get(consumerPos);
        if (nodee == null) {
            Tesseract.LOGGER.warn("Error in onCheck, null cache.");
            return false;
        }
        IGTNode node = nodee.value();
        long pos = Pos.sub(consumerPos, producerPos);
        Direction dir = path != null ? path.target().getDirection()
                : Direction.getFacingFromVector(Pos.unpackX(pos), Pos.unpackY(pos), Pos.unpackZ(pos)).getOpposite();
        if (node.canInput(dir)) {
            GTConsumer consumer = new GTConsumer(node, path);
            int voltage = producer.getOutputVoltage() - consumer.getLoss();
            if (voltage <= 0) {
                return true;
            }
            consumers.add(consumer);
            return true;
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
        holders.long2LongEntrySet().forEach(e -> frameHolders.compute(e.getLongKey(), (a, b) -> {
            if (b == null) b = 0L;
            return b + e.getLongValue();
        }));
        holders.clear();
        this.group.getNodes().values().forEach(t -> t.value().tesseractTick());
        //obtains.clear();
    }

    @Override
    public int insert(long producerPos, long pipePos, Long stack, boolean simulate) {
        NodeCache<IGTNode> node = this.group.getNodes().get(producerPos);
        IGTNode producer;
        if (node == null) {
            //Fallback to a fake node from a pipe.
            producer = getPipeNode(pipePos);
            if (producer == null) {
                return 0;
            }
        } else {
            producer = node.value();
        }
        long key = producerPos == pipePos ? pipePos : Pos.sub(producerPos, pipePos);
        Direction dir = producerPos == pipePos ? Direction.NORTH : Direction.byLong(Pos.unpackX(key), Pos.unpackY(key), Pos.unpackZ(key));
        Map<Direction, List<GTConsumer>> map = this.data.get(producerPos);
        if (map == null) return 0;
        List<GTConsumer> list = map.get(dir);
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
            int received = consumer.getNode().getState().ampsReceived;
            amperage = Math.min(amperage, consumer.getNode().getInputAmperage() - received);
            // If we are here, then path had some invalid cables which not suits the limits of amps/voltage
            if (amperage <= 0)
                continue;
            if (!simulate && !consumer.canHandle(voltage_out)) {
                // Find corrupt cables and return
                for (Long2ObjectMap.Entry<IGTCable> c : consumer.getFull().long2ObjectEntrySet()) {
                    long pos = c.getLongKey();
                    IGTCable cable = c.getValue();

                    if (cable.getHandler(voltage_out, amperage) == GTStatus.FAIL_VOLTAGE) {
                        onCableOverVoltage(getWorld(), pos, voltage_out);
                        return 0;
                    }
                }
                return 0;
            }
            //TODO: For now just limit to 1 amp per insertion.
            amperage = 1;
            // Stores the amp into holder for path only for variate connection
            if (!simulate) {
                for (Long2ObjectMap.Entry<IGTCable> c : consumer.getFull().long2ObjectEntrySet()) {
                    long pos = c.getLongKey();
                    IGTCable cable = c.getValue();

                    long holder = holders.get(pos);
                    holders.put(pos, (holder == 0L) ? GTHolder.create(cable, amperage) : GTHolder.add(holder, amperage));

                    if (GTHolder.isOverAmperage(holders.get(pos))) {
                        onCableOverAmperage(getWorld(), pos, GTHolder.getAmperage(holders.get(pos)));
                        return 0;
                    }
                }
            }

            long amp = amperage; // cast here
            long extracted = voltage_out * amp;
            if (!simulate) {
                totalVoltage += extracted;
                totalLoss += (extracted - voltage);
                totalAmperage += amp;
                for (int i = 0; i < amp; i++) {
                    consumer.insert(voltage, false);
                }
                return voltage * amperage; //TODO: Make tesseract use longs.
            }
            return (int) voltage * amperage;
        }
        return 0;
    }

    @Override
    protected void onFrame() {
        lastVoltage = totalVoltage;
        lastAmperage = totalAmperage;
        lastLoss = totalLoss;
        totalAmperage = totalVoltage = totalLoss = 0L;
        previousFrameHolder = frameHolders;
        frameHolders = new Long2LongOpenHashMap();
    }

    @Override
    public void getInfo(long pos, @Nonnull List<String> list) {
        this.group.getGroupInfo(pos, list);
        list.add(String.format("GT Data size: %d", this.data.size()));
        /*int amp = GTHolder.getAmperage(previousFrameHolder.get(pos));
        return new String[]{
                "Total Voltage (per tick average): ".concat(Long.toString(lastVoltage / 20)),
                "Total Amperage (per tick average): ".concat(Long.toString(lastAmperage / 20)),
                "Cable amperage (last frame): ".concat(Integer.toString(amp))
        };*/

    }

    /**
     * GUI SYNC METHODS
     **/
    public long getTotalVoltage() {
        return lastVoltage;
    }

    public long totalAmps() {
        return lastAmperage;
    }

    public int cableFrameAverage(long pos) {
        return GTHolder.getAmperage(previousFrameHolder.get(pos));
    }

    public long totalLoss() {
        return lastLoss;
    }

    /**
     * END GUI SYNC METHODS
     **/

    @Override
    public ITickingController clone(INode group) {
        return new GTController(dim).set(group);
    }
}