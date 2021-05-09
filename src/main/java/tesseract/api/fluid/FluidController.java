package tesseract.api.fluid;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import tesseract.api.Consumer;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.graph.*;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController<N extends IFluidNode> extends Controller<FluidStack, IFluidPipe, N> implements IFluidEvent<FluidStack> {

    // TODO: assign the value from Antimatter config
    public final static boolean HARDCORE_PIPES = false;
    public final static boolean SLOOSH = false;
    public static double PIPE_LEAK = 0.8;
    private long totalPressure, lastPressure;
    private int maxTemperature, lastTemperature;
    private boolean isLeaking, lastLeaking;
    private final Long2ObjectMap<FluidHolder<Fluid>> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectMap<N, Map<Direction, List<FluidConsumer>>> data = new Object2ObjectLinkedOpenHashMap<>();
    private final List<Neighbour> neighbours = new ObjectArrayList<>();
    /**
     * Creates instance of the controller.
     *
     * @param world the world.
     */
    public FluidController(World world) {
        super(world);
    }

    @Override
    public void change() {
        if (!SLOOSH) {
            data.clear();

            for (Long2ObjectMap.Entry<NodeCache<N>> e : group.getNodes().long2ObjectEntrySet()) {
                long pos = e.getLongKey();
                N producer = e.getValue().value();
    
                if (producer.canOutput()) {
                    Pos position = new Pos(pos);
                    for (Direction direction : Graph.DIRECTIONS) {
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
                                data.computeIfAbsent(producer, map -> new EnumMap<>(Direction.class)).put(direction, consumers);
                            }
                        }
                    }
                }
            }
    
            for (Map<Direction, List<FluidConsumer>> map : data.values()) {
                for (List<FluidConsumer> consumers : map.values()) {
                    consumers.sort(Consumer.COMPARATOR);
                }
            }
        } else {
            neighbours.clear();
            for (Int2ObjectMap.Entry<Grid<IFluidPipe>> entry : group.getGrids().int2ObjectEntrySet()) {
                Grid<IFluidPipe> grid = entry.getValue();
                for(Long2ObjectMap.Entry<Cache<IFluidPipe>> ent : grid.getConnectors().long2ObjectEntrySet()) {
                    byte connectivity = ent.getValue().connectivity();
                    long pos = ent.getLongKey();
                    ImmutableList.Builder<Tuple<Direction,Either<IFluidPipe, IFluidNode>>> list = ImmutableList.builder();
                    for (Direction dir : Graph.DIRECTIONS) {
                        if (!Connectivity.has(connectivity, dir.getIndex())) continue;
                        long newPos = new Pos(pos).offset(dir).asLong();
                        if (grid.contains(newPos)) {
                            Cache<IFluidPipe> newCache = grid.getConnectors().get(newPos);
                            if (newCache != null) {
                                list.add(new Tuple<>(dir, Either.left(newCache.value())));
                            } else if (group.getNodes().containsKey(newPos)) {
                                list.add(new Tuple<>(dir, Either.right(group.getNodes().get(newPos).value())));
                            } else {
                                throw new RuntimeException("Tesseract state broken, report this to mod authors");
                            }
                        }
                    }
                    neighbours.add(new Neighbour(ent.getValue().value(), pos, list.build()));
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (SLOOSH) {
            if (getWorld().getGameTime() % 10 != 0) return;
            for (Neighbour neighbour : this.neighbours) {
                IFluidNode source = neighbour.source.getNode();
                List<Tuple<Direction,Either<IFluidPipe, IFluidNode>>> destination = neighbour.neighbours;
                int tanksToMoveTo = destination.stream().mapToInt(t -> t.getB().map(pipe -> pipe.getNode().canInput(t.getA()), node -> node.canInput(t.getA())) ? 1 : 0).sum();
                if (tanksToMoveTo < 1) continue;
                for (int i = 0; i < source.getTanks(); i++) {
                    FluidStack stack = source.getFluidInTank(i);
                    if (stack.isEmpty()) continue;
                    int toMove = (stack.getAmount() + tanksToMoveTo - 1)/(tanksToMoveTo);
                    if (toMove == 0) {
                        if (stack.getAmount() == 0) continue;
                        toMove = 1;
                    }
                    int amount = stack.getAmount();
                    FluidStack copy = stack.copy();
                    copy.setAmount(toMove);
                    for (int j = 0; j < destination.size() && amount > 0; j++) {
                        Either<IFluidPipe, IFluidNode> dest = destination.get(j).getB();
                        int moved = dest.map(pipe -> pipe.getNode().fill(copy, FluidAction.SIMULATE), node -> node.fill(copy, FluidAction.SIMULATE));
                        FluidStatus status = dest.map(pipe -> {
                            return pipe.getHandler(stack.getFluid().getAttributes().getTemperature(), moved, stack.getFluid().getAttributes().isGaseous());
                        }, node -> FluidStatus.SUCCESS);
                        int temperature = stack.getFluid().getAttributes().getTemperature();
                        switch (status) {
                            case FAIL_TEMP:
                                onPipeOverTemp(getWorld(), new Pos(neighbour.pos).offset(destination.get(j).getA()).asLong(), temperature);
                                return;
                            case FAIL_PRESSURE:
                                onPipeOverPressure(getWorld(), new Pos(neighbour.pos).offset(destination.get(j).getA()).asLong(), amount, stack);
                                return;
                            case FAIL_LEAK:
                                onPipeGasLeak(getWorld(), new Pos(neighbour.pos).offset(destination.get(j).getA()).asLong(), copy);
                                continue;
                            case FAIL_CAPACITY:
                                return;
                            case SUCCESS:
                                dest.map(pipe -> pipe.getNode().fill(copy, FluidAction.EXECUTE), node -> node.fill(copy, FluidAction.EXECUTE));
                                break;
                        }
                        amount -= moved;
                    }
                    amount = Math.max(amount, 0);
                    copy.setAmount(stack.getAmount()-amount);
                    source.drainInput(copy, FluidAction.EXECUTE);
                }
            }
        } else {
            holders.clear();
        }
    }

    /**
     * Adds available consumers to the list.
     *
     * @param consumers The consumer nodes.
     * @param path The paths to consumers.
     * @param dir The added direction.
     * @param pos The position of the producer.
     */
    private void onCheck(List<FluidConsumer> consumers, Path<IFluidPipe> path, Direction dir, long pos) {
        N node = group.getNodes().get(pos).value();
        if (node.canInput()) consumers.add(new FluidConsumer(node, path, dir));
    }

    public int insert(Pos producerPos, Direction direction, FluidStack stack, boolean simulate) {
        if (SLOOSH) return 0;
        NodeCache<N> node = this.group.getNodes().get(producerPos.offset(direction).asLong());
        if (node == null) return 0;
        Map<Direction, List<FluidConsumer>> map = this.data.get(node.value());
        if (map == null) return 0;
        List<FluidConsumer> list = map.get(direction.getOpposite());
        if (list == null) return 0;

        int outputAmount = stack.getAmount();//producer.getOutputAmount(direction);
        FluidStack newStack = stack.copy();
        for (FluidConsumer consumer : list) {
            if (!consumer.canHold(newStack)) {
                continue;
            }

            int amount = consumer.insert(newStack, true);
            if (amount <= 0) {
                continue;
            }
            if (!HARDCORE_PIPES) {
                if (simulate) {
                    amount = Math.min(amount, consumer.getMinPressure());
                    for (Long2ObjectMap.Entry<IFluidPipe> entry : consumer.getFull().long2ObjectEntrySet()) {
                        FluidHolder<Fluid> holder = holders.get(entry.getLongKey());
                        amount = Math.min(amount, holder != null ? entry.getValue().getPressure() - holder.getPressure() : entry.getValue().getPressure());
                    }
                }
            }

            newStack.setAmount(amount);
            int temperature = stack.getFluid().getAttributes().getTemperature();
            boolean isGaseous = stack.getFluid().getAttributes().isGaseous();

            //FluidStack drained = producer.extract(tank, amount, false);

            // If we are here, then path had some invalid pipes which not suits the limits of temp/pressure/gas
            // only check if not simulate, otherwise it would never be called w/o simulate.
            if (!simulate && !consumer.canHandle(temperature, amount, isGaseous)) {
                // Find corrupt pipe and return
                for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull().long2ObjectEntrySet()) {
                    long pos = p.getLongKey();
                    IFluidPipe pipe = p.getValue();

                    switch (pipe.getHandler(temperature, amount, isGaseous)) {
                        case FAIL_TEMP:
                            onPipeOverTemp(getWorld(), pos, temperature);
                            return 0;
                        case FAIL_LEAK:
                            newStack = onPipeGasLeak(getWorld(), pos, newStack);
                            isLeaking = true;
                            break;
                        default:
                            break;
                    }
                }
            }

            // Stores the pressure into holder for path only for variate connection
            if (!simulate) {
                for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull().long2ObjectEntrySet()) {
                    long pos = p.getLongKey();
                    IFluidPipe pipe = p.getValue();

                    holders.computeIfAbsent(pos, h -> new FluidHolder<>(pipe)).add(amount, stack.getFluid());

                    FluidHolder<Fluid> holder = holders.get(pos);
                
                    if (holder.isOverPressure()) {
                        onPipeOverPressure(getWorld(), pos, holder.getPressure(), stack);
                        return 0;
                    }
                    if (holder.isOverCapacity()) {
                        onPipeOverCapacity(getWorld(), pos, holder.getCapacity(), stack);
                        return 0;
                    }
                }
            }

            if (!simulate) {
                maxTemperature = Math.max(temperature, maxTemperature);
                totalPressure += amount;
            }

            if (!simulate)
                consumer.insert(newStack, false);
            
            outputAmount -= amount;
            if (amount > 0) {
                break;
            }
            if (outputAmount <= 0) {
                break;
            }
            newStack.setAmount(outputAmount);
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
        return new FluidController<>(dim).set(group);
    }

    protected static class Neighbour {
        public final IFluidPipe source;
        public final long pos;
        public final List<Tuple<Direction,Either<IFluidPipe, IFluidNode>>> neighbours;

        public Neighbour(IFluidPipe source, long pos, List<Tuple<Direction,Either<IFluidPipe, IFluidNode>>> neighbours) {
            this.source = source;
            this.pos = pos;
            this.neighbours = neighbours;
        }
    }
}
