package tesseract.api.fluid;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tesseract.api.ConnectionType;
import tesseract.api.Consumer;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.api.capability.ITransactionModifier;
import tesseract.api.capability.TesseractBaseCapability;
import tesseract.graph.*;
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
        implements IFluidEvent<FluidStack> {

    public final static boolean HARDCORE_PIPES = false;
    public final static boolean SLOOSH = false;
    public static double PIPE_LEAK = 0.8;
    private long totalPressure, lastPressure;
    private int maxTemperature, lastTemperature;
    private boolean isLeaking, lastLeaking;
    private final Long2ObjectMap<Map<Direction, List<FluidConsumer>>> data = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2IntMap pressureData = new Long2IntOpenHashMap(10);
    //public final Long2IntMap sentPressure = new Long2IntOpenHashMap(10);

    /**
     * Creates instance of the controller.
     *
     * @param world the world.
     */
    public FluidController(World world, Graph.INodeGetter<IFluidNode> getter) {
        super(world, getter);
    }

    private void handleInput(long pos, NodeCache<IFluidNode> producers) {
        if (data.containsKey(pos))
            return;
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

    @Override
    public void change() {
        if (!SLOOSH) {
            data.clear();
            this.group.connectors().forEach(t -> t.value().getHolder().clear());
            for (Long2ObjectMap.Entry<NodeCache<IFluidNode>> e : group.getNodes().long2ObjectEntrySet()) {
                handleInput(e.getLongKey(), e.getValue());
            }

            this.group.pipeNodes().forEach(t -> handleInput(t, new NodeCache<>(t, getter)));

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
        if (node != null && node.canInput())
            consumers.add(new FluidConsumer(node,producer, path, dir));
    }

    @Override
    public void insert(long producerPos, Direction side, FluidTransaction transaction, ITransactionModifier modifier) {
        if (SLOOSH)
            return;
        if (!transaction.isValid())
            return;
        Map<Direction, List<FluidConsumer>> map = this.data.get(Pos.offset(producerPos, side));
        if (map == null)
            return;
        List<FluidConsumer> list = map.get(side);
        if (list == null)
            return;

        pressureData.clear();

        loop: for (FluidConsumer consumer : list) {
            FluidStack data = transaction.stack.copy();
            if (!consumer.canHold(data)) {
                continue;
            }

            int amount = consumer.insert(data, true);
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
                        FluidHolder holder = entry.getValue().getHolder();
                        if (!holder.allowFluid(data.getFluid())) {
                            amount = 0;
                            break;
                        }
                        int tempData = pressureData.get(entry.getLongKey());
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
                    final int finalAmount = amount;
                    pressureData.compute(p.getLongKey(), (k, v) -> v == null ? finalAmount : v + finalAmount);
                    if (p.getValue() instanceof TesseractBaseCapability) {
                        TesseractBaseCapability<?> cap = (TesseractBaseCapability<?>) p.getValue();
                        cap.callback.modify(data,cap.side, side, true);
                    }
                }
            }
            transaction.addData(data.copy(), a -> dataCommit(consumer, a));

            if (transaction.stack.isEmpty())
                break;
        }
    }
    public void dataCommit(FluidConsumer consumer, FluidStack stack) {
        int temperature = stack.getFluid().getAttributes().getTemperature();
        int amount = stack.getAmount();
        boolean isGaseous = stack.getFluid().getAttributes().isGaseous();
        boolean cantHandle = !consumer.canHandle(temperature, isGaseous);
        if (!cantHandle) {
            for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull().long2ObjectEntrySet()) {
                long pos = p.getLongKey();
                IFluidPipe pipe = p.getValue();
                switch (pipe.getHandler(stack, temperature, isGaseous)) {
                    case FAIL_TEMP:
                        onPipeOverTemp(getWorld(), pos, temperature);
                        return;
                    case FAIL_LEAK:
                        stack = onPipeGasLeak(getWorld(), pos, stack);
                        isLeaking = true;
                        break;
                    case FAIL_CAPACITY:
                        break;
                    default:
                        break;
                }
            }
        }
        if (consumer.getConnection() == ConnectionType.SINGLE) {
            FluidHolder holder = this.group.getConnector(consumer.lowestPipePosition).value().getHolder();
            holder.use(stack.getAmount(), stack.getFluid(), getWorld().getGameTime());
            if (holder.isOverPressure()) {
                onPipeOverPressure(getWorld(), consumer.lowestPipePosition, amount, stack);
                return;
            }
            if (holder.isOverCapacity()) {
                onPipeOverCapacity(getWorld(), consumer.lowestPipePosition, amount, stack);
                return;
            }
        } else if (consumer.getConnection() == ConnectionType.VARIATE) {
            for (Long2ObjectMap.Entry<IFluidPipe> pathHolderEntry : consumer.getCross()
                    .long2ObjectEntrySet()) {
                FluidHolder holder = pathHolderEntry.getValue().getHolder();
                holder.use(stack.getAmount(), stack.getFluid(), getWorld().getGameTime());
                if (holder.isOverPressure()) {
                    onPipeOverPressure(getWorld(), pathHolderEntry.getLongKey(), amount, stack);
                    return;
                }
                if (holder.isOverCapacity()) {
                    onPipeOverCapacity(getWorld(), pathHolderEntry.getLongKey(), amount, stack);
                    return;
                }
                if (pathHolderEntry.getValue() instanceof TesseractBaseCapability) {
                    TesseractBaseCapability<?> cap = (TesseractBaseCapability<?>) pathHolderEntry.getValue();
                    cap.callback.modify(data,cap.side, consumer.input, false);
                }
            }
        }
        /*consumer.getFull().keySet().forEach(l -> {
            int pressure = amount + this.sentPressure.get(l);
            this.sentPressure.put(l, pressure);
        });*/
        maxTemperature = Math.max(temperature, maxTemperature);
        totalPressure += amount;
        consumer.insert(stack, false);
    }

    @Override
    public void tick() {
        super.tick();
      //  sentPressure.clear();
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
