package tesseract.api.gt;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import tesseract.Tesseract;
import tesseract.api.ConnectionType;
import tesseract.api.Controller;
import tesseract.api.DataHolder;
import tesseract.api.ITickingController;
import tesseract.api.capability.ITransactionModifier;
import tesseract.api.rf.RFDataHolder;
import tesseract.graph.Graph;
import tesseract.graph.Grid;
import tesseract.graph.INode;
import tesseract.graph.NodeCache;
import tesseract.graph.Path;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Class acts as a controller in the group of an electrical components.
 */
public class GTController extends Controller<GTDataHolder, IGTCable, IGTNode> implements IGTEvent {

    private long totalVoltage, totalAmperage, lastVoltage, lastAmperage, totalLoss, lastLoss;
    // Cable monitoring.
    private Long2LongMap frameHolders = new Long2LongLinkedOpenHashMap();
    private Long2LongMap previousFrameHolder = new Long2LongLinkedOpenHashMap();
    // private final Object2IntMap<IGTNode> obtains = new Object2IntOpenHashMap<>();
    private final Long2ObjectMap<Map<Direction, List<GTConsumer>>> data = new Long2ObjectLinkedOpenHashMap<>();

    public final LongSet cableIsActive = new LongOpenHashSet();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     */
    public GTController(Level dim, Graph.INodeGetter<IGTNode> getter) {
        super(dim, getter);
    }

    /**
     * Executes when the group structure has changed.
     * <p>
     * First, it clears previous controller map, after it lookup for the position of
     * node and looks for the around grids.
     * Second, it collects all producers and collectors for the grid and stores it
     * into data map.
     * Finally, it will pre-build consumer objects which are available for the
     * producers. So each producer has a list of possible
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

    private boolean changeInternal() {
        data.clear();
        for (Long2ObjectMap.Entry<NodeCache<IGTNode>> e : group.getNodes().long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            for (Map.Entry<Direction, IGTNode> tup : e.getValue().values()) {
                IGTNode producer = tup.getValue();
                Direction direction = tup.getKey();
                if (producer.canOutput(direction)) {
                    long side = Pos.offset(pos, direction);
                    List<GTConsumer> consumers = new ObjectArrayList<>();

                    Grid<IGTCable> grid = group.getGridAt(side, direction);
                    if (grid != null) {
                        for (Path<IGTCable> path : grid.getPaths(pos)) {
                            if (!path.isEmpty()) {
                                Node target = path.target();
                                assert target != null;
                                if (!onCheck(producer, consumers, path, target.asLong(), target.getDirection()))
                                    return false;
                            }
                        }
                    } else if (group.getNodes().containsKey(side)) {
                        onCheck(producer, consumers, null, side, direction.getOpposite());
                    }
                    if (!consumers.isEmpty())
                        data.computeIfAbsent(pos, m -> new EnumMap<>(Direction.class))
                                .put(direction.getOpposite(), consumers);
                }
            }
        }

        for (Map<Direction, List<GTConsumer>> map : data.values()) {
            for (List<GTConsumer> consumers : map.values()) {
                consumers.sort(GTConsumer.COMPARATOR);
            }
        }
        return true;
    }

    /**
     * Adds available consumers to the list.
     *
     * @param producer    The producer node.
     * @param consumers   The consumer nodes.
     * @param path        The paths to consumers.
     * @param consumerPos The position of the consumer.
     * @return whether or not an issue arose checking node.
     */
    private boolean onCheck(IGTNode producer, List<GTConsumer> consumers, Path<IGTCable> path, long consumerPos,
                            Direction dir) {
        NodeCache<IGTNode> nodee = group.getNodes().get(consumerPos);

        IGTNode node = nodee.value(dir);

        if (node != null && node.canInput(dir)) {
            GTConsumer consumer = new GTConsumer(node, producer, path);
            long voltage = producer.getOutputVoltage() - consumer.getLoss();
            if (voltage <= 0) {
                return true;
            }
            consumers.add(consumer);
            return true;
        }
        return true;
    }

    Long2IntMap pipeMap;
    int inserted;

    @Override
    public void tick() {
        super.tick();
        for (var connector : this.group.connectors()) {
            connector.value().setHolder(GTHolder.create(connector.value(), 0));
        }
        for (var node : this.group.getNodes().values()) {
            for (Map.Entry<Direction, IGTNode> n : node.values()) {
                n.getValue().tesseractTick();
                break;
            }
        }
        pipeMap = new Long2IntOpenHashMap();
        // obtains.clear();
        inserted = 0;
    }

    @Override
    public void insert(long pipePos, Direction side, GTDataHolder stack, ITransactionModifier modifier, boolean simulate) {
        Map<Direction, List<GTConsumer>> map = this.data.get(Pos.offset(pipePos, side));
        if (map == null)
            return;
        List<GTConsumer> list = map.get(side);
        if (list == null)
            return;
        NodeCache<IGTNode> node = this.group.getNodes().get(Pos.offset(pipePos, side));
        if (node == null)
            return;
        IGTNode producer = node.value(side.getOpposite());

        long voltageOut = producer.getOutputVoltage();

        /*
         * if (amperage_in <= 0) { // just for sending the last piece of energy
         * voltage_out = (int) energy;
         * amperage_in = 1;
         * }
         */
        inserted++;
        long voltageIn = stack.getImmutableData().getA();
        long amperageIn = stack.getImmutableData().getB();
        long usedAmps = 0;

        for (GTConsumer consumer : list) {

            if (amperageIn <= 0) {
                break;
            }
            long loss = consumer.getLoss();
            if (loss < 0 || loss > voltageOut) {
                continue;
            }

            long amperage = consumer.getRequiredAmperage(voltageOut - loss);
            if (amperage <= 0) { // if this consumer received all the energy from the other producers
                continue;
            }

            // Remember amperes stored in this consumer
            amperage = Math.min(amperageIn, amperage);
            // If we are here, then path had some invalid cables which not suits the limits
            // of amps/voltage
            GTDataHolder modify = new GTDataHolder(new Tuple<>(voltageIn - loss, amperageIn), 0L);
            if (modifier.modify(modify, null, side, simulate)) continue;
            if (modify.getData() > 0){
                amperageIn -= modify.getData();
                usedAmps += modify.getData();
                amperage -= modify.getData();
                if (amperage <= 0) continue;
                if (amperageIn == 0) break;
            }
            if (!simulate){
                dataCommit(consumer, voltageIn - loss, amperage, loss);
            }
            amperageIn -= amperage;
            usedAmps += amperage;
        }
        stack.setData(usedAmps);
    }

    /**
     * Callback from the transaction, that sends data to the consumer and also
     * verifies cable voltage/amperage.
     *
     * @param consumer the consumer.
     * @param voltage  the input voltage.
     * @param amperage the input amperage
     * @param loss     the loss
     */
    public void dataCommit(GTConsumer consumer, long voltage, long amperage, long loss) {
        if (!consumer.canHandle(voltage) || !consumer.canHandleAmp(amperage) || (consumer.getConnection() == ConnectionType.SINGLE
                && !(consumer.canHandleAmp(amperage)))) {
            for (Long2ObjectMap.Entry<IGTCable> c : consumer.getFull().long2ObjectEntrySet()) {
                long pos = c.getLongKey();
                IGTCable cable = c.getValue();
                switch (cable.getHandler(voltage, amperage)) {
                    case FAIL_VOLTAGE -> {
                        onCableOverVoltage(getWorld(), pos, voltage);
                        return;
                    }
                    case FAIL_AMPERAGE -> {
                        onCableOverAmperage(getWorld(), pos, amperage);
                        return;
                    }
                    default -> {
                    }
                }
            }
        }
        if (consumer.getConnection() == ConnectionType.VARIATE) {
            for (Long2ObjectMap.Entry<IGTCable> c : consumer.getCross().long2ObjectEntrySet()) {
                long pos = c.getLongKey();
                IGTCable cable = c.getValue();
                /*pipeMap.compute(c.getLongKey(), (l, i) ->{
                    if (i == null) return Math.toIntExact(data.getTotalAmperage());
                    return Math.toIntExact(i + data.getTotalAmperage());
                });*/
                cable.setHolder(GTHolder.add(cable.getHolder(), amperage));
                if (GTHolder.isOverAmperage(cable.getHolder())) {
                    onCableOverAmperage(getWorld(), pos, GTHolder.getAmperage(cable.getHolder()));
                    return;
                }
            }
        }
        // to keep track of burning cables.
        cableIsActive.addAll(consumer.uninsulatedCables);

        this.totalLoss += loss;
        this.totalAmperage += amperage;
        this.totalVoltage += amperage * voltage;
        consumer.getNode().insertAmps(voltage, amperage, false);
    }

    @Override
    protected void onFrame() {
        lastVoltage = totalVoltage;
        lastAmperage = totalAmperage;
        lastLoss = totalLoss;
        totalAmperage = totalVoltage = totalLoss = 0L;
        previousFrameHolder = frameHolders;
        frameHolders = new Long2LongOpenHashMap();
        cableIsActive.clear();
    }

    @Override
    public void getInfo(long pos, @NotNull List<String> list) {
        if (this.group != null) {
            this.group.getGroupInfo(pos, list);
            list.add(String.format("GT Data size: %d", this.data.size()));
        }
        /*
         * int amp = GTHolder.getAmperage(previousFrameHolder.get(pos));
         * return new String[]{
         * "Total Voltage (per tick average): ".concat(Long.toString(lastVoltage / 20)),
         * "Total Amperage (per tick average): ".concat(Long.toString(lastAmperage /
         * 20)),
         * "Cable amperage (last frame): ".concat(Integer.toString(amp))
         * };
         */

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
        return new GTController(dim, getter).set(group);
    }
}
