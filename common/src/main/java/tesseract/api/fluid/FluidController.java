package tesseract.api.fluid;

import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import tesseract.FluidPlatformUtils;
import tesseract.api.ConnectionType;
import tesseract.api.Consumer;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.api.capability.ITransactionModifier;
import tesseract.graph.Cache;
import tesseract.graph.Graph;
import tesseract.graph.Grid;
import tesseract.graph.INode;
import tesseract.graph.Path;
import tesseract.util.Node;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController extends Controller<FluidTransaction, IFluidPipe, IFluidNode>
        implements IFluidEvent<FluidHolder> {

    public final static boolean HARDCORE_PIPES = false;
    public final static boolean SLOOSH = false;
    public static double PIPE_LEAK = 0.8;
    private long totalPressure, lastPressure;
    private int maxTemperature, lastTemperature;
    private boolean isLeaking, lastLeaking;
    private final Long2ObjectMap<Map<Direction, List<FluidConsumer>>> data = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2LongMap pressureData = new Long2LongOpenHashMap(10);

    /**
     * Creates instance of the controller.
     *
     * @param world the world.
     */
    public FluidController(Level world, Graph.INodeGetter<IFluidNode> getter) {
        super(world, getter);
    }

    @Override
    public void change() {
        if (!SLOOSH) {
            data.clear();
            for (var entry : this.group.getNodes().long2ObjectEntrySet()) {
                long pos = entry.getLongKey();
                var producers = entry.getValue();

                for (Map.Entry<Direction, IFluidNode> tup : producers.values()) {
                    IFluidNode producer = tup.getValue();
                    Direction direction = tup.getKey();
                    if (producer.canOutput(direction)) {
                        List<FluidConsumer> consumers = new ObjectArrayList<>();
                        long side = Pos.offset(pos, direction);
                        
                        Grid<IFluidPipe> grid = group.getGridAt(side, direction);
                        if (grid != null) {
                            for (Path<IFluidPipe> path : grid.getPaths(pos)) {
                                if (!path.isEmpty()) {
                                    Node target = path.target();
                                    assert target != null;
                                    onCheck(producer, consumers, path, target.getDirection(), target.asLong());
                                }
                            }
                        } else if (group.getNodes().containsKey(side)) {
                            onCheck(producer, consumers, null, direction.getOpposite(), side);
                        }

                        if (!consumers.isEmpty()) {
                            data.computeIfAbsent(pos, map -> new EnumMap<>(Direction.class))
                                    .put(direction.getOpposite(), consumers);
                        }
                    }
                }
            }

            for (Map<Direction, List<FluidConsumer>> map : data.values()) {
                for (List<FluidConsumer> consumers : map.values()) {
                    consumers.sort(Consumer.COMPARATOR);
                }
            }
        }
    }

    /**
     * Adds available consumers to the list.
     *
     * @param consumers The consumer nodes.
     * @param path      The paths to consumers.
     * @param dir       The added direction.
     * @param pos       The position of the producer.
     */
    private void onCheck(IFluidNode producer, List<FluidConsumer> consumers, Path<IFluidPipe> path, Direction dir, long pos) {
        IFluidNode node = group.getNodes().get(pos).value(dir);
        if (node != null && node.allowsInsertion())
            consumers.add(new FluidConsumer(node,producer, path, dir));
    }

    @Override
    public void insert(long producerPos, Direction side, FluidTransaction transaction, ITransactionModifier modifier) {
        if (SLOOSH || !transaction.isValid())
            return;

        Map<Direction, List<FluidConsumer>> map = this.data.get(Pos.offset(producerPos, side));
        if (map == null)
            return;

        List<FluidConsumer> list = map.get(side);
        if (list == null) return; //TODO figure out how this becomes null

        pressureData.clear();

        loop: for (FluidConsumer consumer : list) {
            FluidHolder data = transaction.stack.copyHolder();
            if (!consumer.canHold(data)) {
                continue;
            }

            long amount = consumer.insert(data, true);
            if (amount <= 0) {
                continue;
            }
            if (!HARDCORE_PIPES) {
                if (consumer.getConnection() == ConnectionType.SINGLE) {
                    if (consumer.lowestPipePosition == -1) {
                        amount = Math.min(amount, consumer.getMinPressure() * 20);
                    } else {
                        amount = Math.min(amount, this.group.getConnector(consumer.lowestPipePosition).value().getHolder().getPressureAvailable());
                    }
                } else {
                    for (Long2ObjectMap.Entry<IFluidPipe> entry : consumer.getCross()
                            .long2ObjectEntrySet()) {
                        PipeFluidHolder holder = entry.getValue().getHolder();
                        if (!holder.allowFluid(data.getFluid())) {
                            amount = 0;
                            break;
                        }
                        long tempData = pressureData.get(entry.getLongKey());
                        amount = Math.min(amount, holder.getPressureAvailable() - tempData);
                        if (amount == 0)
                            continue loop;
                    }
                }
            }
            data.setAmount(amount);
            if (data.isEmpty())
                continue;

            if (consumer.getConnection() == ConnectionType.VARIATE) {
                for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getCross().long2ObjectEntrySet()) {
                    final long finalAmount = amount;
                    pressureData.compute(p.getLongKey(), (k, v) -> v == null ? finalAmount : v + finalAmount);
                }
            }
            transaction.addData(data.copyHolder(), a -> commitFluid(consumer, a));

            if (transaction.stack.isEmpty())
                break;
        }
    }
    public void commitFluid(FluidConsumer consumer, FluidHolder stack) {
        int temperature = FluidPlatformUtils.getFluidTemperature(stack.getFluid());
        long amount = stack.getFluidAmount();
        boolean isGaseous = FluidPlatformUtils.isFluidGaseous(stack.getFluid());
        boolean cantHandle = !consumer.canHandle(temperature, isGaseous);
        if (!cantHandle) {
            for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull().long2ObjectEntrySet()) {
                long pos = p.getLongKey();
                IFluidPipe pipe = p.getValue();
                switch (pipe.getHandler(stack, temperature, isGaseous)) {
                    case FAIL_TEMP -> {
                        onPipeOverTemp(getWorld(), pos, temperature);
                        return;
                    }
                    case FAIL_LEAK -> {
                        stack = onPipeGasLeak(getWorld(), pos, stack);
                        isLeaking = true;
                    }
                    default -> {
                    }
                }
            }
        }

        if (consumer.getConnection() == ConnectionType.SINGLE) {
           if (!checkCommitPipe(consumer.lowestPipePosition, amount, stack)) return;
        } else if (consumer.getConnection() == ConnectionType.VARIATE) {
            for (Long2ObjectMap.Entry<IFluidPipe> pathHolderEntry : consumer.getCross()
                    .long2ObjectEntrySet()) {
                if (!checkCommitPipe(pathHolderEntry.getLongKey(), amount, stack)) return;
            }
        }
        maxTemperature = Math.max(temperature, maxTemperature);
        totalPressure += amount;
        consumer.insert(stack, false);
    }

    private boolean checkCommitPipe(long pos, long amount, FluidHolder stack) {
        PipeFluidHolder holder = this.group.getConnector(pos).value().getHolder();
        holder.use(stack.getFluidAmount(), stack.getFluid(), getWorld().getGameTime());
        if (holder.isOverPressure()) {
            onPipeOverPressure(getWorld(), pos, amount, stack);
            return false;
        }
        if (holder.isOverCapacity()) {
            onPipeOverCapacity(getWorld(), pos, amount, stack);
            return false;
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        for (Cache<IFluidPipe> pipe : this.group.connectors()) {
            pipe.value().getHolder().tick(getWorld().getGameTime());
        }
    }

    @Override
    protected void onFrame() {
        lastTemperature = maxTemperature;
        lastPressure = totalPressure;
        lastLeaking = isLeaking;
        totalPressure = 0L;
        maxTemperature = 0;
        isLeaking = false;
    }

    @Override
    public void getInfo(long pos, @Nonnull List<String> list) {
        if (this.group != null) {
            this.group.getGroupInfo(pos, list);
            list.add(String.format("Fluid Data size: %d", this.data.size()));
        }
    }

    @Override
    public ITickingController clone(INode group) {
        return new FluidController(dim, getter).set(group);
    }

}
