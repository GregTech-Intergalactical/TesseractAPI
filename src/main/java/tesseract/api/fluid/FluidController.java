package tesseract.api.fluid;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;
import tesseract.api.Consumer;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController<N extends IFluidNode> extends Controller<FluidStack, IFluidPipe, N> implements IFluidEvent {

    // TODO: assign the value from Antimatter config
    public static boolean HARDCORE_PIPES = false;
    public static double PIPE_LEAK = 0.9;
    private long totalPressure, lastPressure;
    private int maxTemperature, lastTemperature;
    private boolean isLeaking, lastLeaking;
    private final Long2ObjectMap<FluidHolder<FluidStack>> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectMap<N, Map<Dir, List<FluidConsumer>>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     */
    public FluidController(Function<RegistryKey<World>, ServerWorld> supplier, RegistryKey<World> dim) {
        super(supplier,dim);
    }

    @Override
    public void change() {
        data.clear();

        for (Long2ObjectMap.Entry<NodeCache<N>> e : group.getNodes().long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            N producer = e.getValue().value();

            if (producer.canOutput()) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (producer.canOutput(direction)) {
                        List<FluidConsumer> consumers = new ObjectArrayList<>();
                        long side = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(side)) {
                            onCheck(consumers, null, direction.getOpposite(), side);
                        } else {
                            Grid<IFluidPipe> grid = group.getGridAt(side, direction);
                            if (grid != null) {
                                for (Path<IFluidPipe> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        Node target = path.target();
                                        assert target != null;
                                        onCheck(consumers, path, target.getDirection(), target.asLong());
                                    }
                                }
                            }
                        }

                        if (!consumers.isEmpty()) {
                            data.computeIfAbsent(producer, map -> new EnumMap<>(Dir.class)).put(direction, consumers);
                        }
                    }
                }
            }
        }

        for (Map<Dir, List<FluidConsumer>> map : data.values()) {
            for (List<FluidConsumer> consumers : map.values()) {
                consumers.sort(Consumer.COMPARATOR);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        holders.clear();
    }

    /**
     * Adds available consumers to the list.
     *
     * @param consumers The consumer nodes.
     * @param path The paths to consumers.
     * @param dir The added direction.
     * @param pos The position of the producer.
     */
    private void onCheck(List<FluidConsumer> consumers, Path<IFluidPipe> path, Dir dir, long pos) {
        N node = group.getNodes().get(pos).value();
        if (node.canInput()) consumers.add(new FluidConsumer(node, path, dir));
    }

    public int insert(Pos producerPos, Dir direction, FluidStack stack, boolean simulate) {
        NodeCache<N> node = this.group.getNodes().get(producerPos.offset(direction).asLong());
        if (node == null) return 0;
        Map<Dir, List<FluidConsumer>> map = this.data.get(node.value());
        if (map == null) return 0;
        List<FluidConsumer> list = map.get(direction.getOpposite());
        if (list == null) return 0;

        int outputAmount = stack.getAmount();//producer.getOutputAmount(direction);
        for (FluidConsumer consumer : list) {
            if (!consumer.canHold(stack)) {
                continue;
            }

            int amount = consumer.insert(stack, true);
            if (amount <= 0) {
                continue;
            }
            FluidStack newStack = stack.copy();
            if (!HARDCORE_PIPES && amount > consumer.getMinPressure())
                amount = Math.min(amount,consumer.getMinPressure());

            newStack.setAmount(amount);
            int temperature = stack.getFluid().getAttributes().getTemperature();
            boolean isGaseous = stack.getFluid().getAttributes().isGaseous();

            //FluidStack drained = producer.extract(tank, amount, false);

            // If we are here, then path had some invalid pipes which not suits the limits of temp/pressure/gas
            if (!simulate && !consumer.canHandle(temperature, amount, isGaseous)) {
                // Find corrupt pipe and return
                for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull().long2ObjectEntrySet()) {
                    long pos = p.getLongKey();
                    IFluidPipe pipe = p.getValue();

                    switch (pipe.getHandler(temperature, amount, isGaseous)) {
                        case FAIL_TEMP:
                            onPipeOverTemp(getWorld(), pos, temperature);
                            return 0;
                        case FAIL_PRESSURE:
                            onPipeOverPressure(getWorld(), pos, amount);
                            return 0;
                        case FAIL_LEAK:
                            newStack = onPipeGasLeak(getWorld(), pos, newStack);
                            isLeaking = true;
                            break;
                    }
                }
            }

            // Stores the pressure into holder for path only for variate connection
            if (!simulate) {
                for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull().long2ObjectEntrySet()) {
                    long pos = p.getLongKey();
                    IFluidPipe pipe = p.getValue();

                    holders.computeIfAbsent(pos, h -> new FluidHolder<FluidStack>(pipe)).add(amount, stack);
                }
            }

            if (!simulate) {
                maxTemperature = Math.max(temperature, maxTemperature);
                totalPressure += amount;
            }

            if (!simulate)
                consumer.insert(newStack, false);

            outputAmount -= amount;
            if (outputAmount <= 0) {
                break;
            }
        }
        if (!simulate) {
            for (Long2ObjectMap.Entry<FluidHolder<FluidStack>> e : holders.long2ObjectEntrySet()) {
                long pos = e.getLongKey();
                FluidHolder absorber = e.getValue();

                // TODO: Find proper path to destroy

                if (absorber.isOverPressure()) {
                    onPipeOverPressure(getWorld(), pos, absorber.getPressure());
                }
                if (absorber.isOverCapacity()) {
                    onPipeOverCapacity(getWorld(), pos, absorber.getCapacity());
                }
            }
        }
        return stack.getAmount() - outputAmount;
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
    public String[] getInfo(long pos) {
        return new String[]{
            "Maximum Temperature: ".concat(Integer.toString(lastTemperature)),
            "Total Pressure: ".concat(Long.toString(lastPressure)),
            "Any Leaks: ".concat(lastLeaking ? "Yes" : "No")
        };
    }

    @Override
    public ITickingController clone(INode group) {
        return new FluidController<>(WORLD_SUPPLIER, dim).set(group);
    }
}
