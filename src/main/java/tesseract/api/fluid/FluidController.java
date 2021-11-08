package tesseract.api.fluid;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController extends Controller<FluidStack, IFluidPipe, IFluidNode> implements IFluidEvent<FluidStack> {

    // TODO: assign the value from Antimatter config
    public final static boolean HARDCORE_PIPES = false;
    public final static boolean SLOOSH = false;
    public static double PIPE_LEAK = 0.8;
    private long totalPressure, lastPressure;
    private int maxTemperature, lastTemperature;
    private boolean isLeaking, lastLeaking;
    private final Long2ObjectMap<FluidHolder> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2ObjectMap<Map<Direction, List<FluidConsumer>>> data = new Long2ObjectLinkedOpenHashMap<>();
    private final List<Neighbour> neighbours = new ObjectArrayList<>();
    private final Long2IntMap pressureData = new Long2IntOpenHashMap(10);

    /**
     * Creates instance of the controller.
     *
     * @param world the world.
     */
    public FluidController(World world) {
        super(IFluidNode::fromPipe, world);
    }

    private void handleInput(long pos, IFluidNode producer) {
        if (data.containsKey(pos)) return;

        if (producer.canOutput()) {
            for (Direction direction : Graph.DIRECTIONS) {
                if (producer.canOutput(direction)) {
                    List<FluidConsumer> consumers = new ObjectArrayList<>();
                    long side = Pos.offset(pos, direction);// position.offset(direction).asLong();

                    Grid<IFluidPipe> grid = group.getGridAt(side, direction);
                    if (grid != null) {
                        for (Path<IFluidPipe> path : grid.getPaths(pos, direction)) {
                            if (!path.isEmpty()) {
                                Node target = path.target();
                                assert target != null;
                                onCheck(consumers, path, target.getDirection(), target.asLong());
                            }
                        }
                    }

                    if (!consumers.isEmpty()) {
                        data.computeIfAbsent(pos, map -> new EnumMap<>(Direction.class)).put(getMapDirection(pos, direction.getOpposite()), consumers);
                    }
                }
            }
        }
    }

    @Override
    public void change() {
        if (!SLOOSH) {
            data.clear();
            for (Long2ObjectMap.Entry<NodeCache<IFluidNode>> e : group.getNodes().long2ObjectEntrySet()) {
                handleInput(e.getLongKey(), e.getValue().value());
            }
            for (Long2ObjectMap.Entry<Cache<IFluidPipe>> e : group.getPipes()) {
                handleInput(e.getLongKey(), wrapPipe(e.getValue().value()));
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
                for (Long2ObjectMap.Entry<Cache<IFluidPipe>> ent : grid.getConnectors().long2ObjectEntrySet()) {
                    byte connectivity = ent.getValue().connectivity();
                    long pos = ent.getLongKey();
                    ImmutableList.Builder<Tuple<Direction, Either<IFluidPipe, IFluidNode>>> list = ImmutableList.builder();
                    for (Direction dir : Graph.DIRECTIONS) {
                        if (!Connectivity.has(connectivity, dir.getIndex())) continue;
                        long newPos = Pos.offset(pos, dir);
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
                List<Tuple<Direction, Either<IFluidPipe, IFluidNode>>> destination = neighbour.neighbours;
                int tanksToMoveTo = destination.stream().mapToInt(t -> t.getB().map(pipe -> pipe.getNode().canInput(t.getA()), node -> node.canInput(t.getA())) ? 1 : 0).sum();
                if (tanksToMoveTo < 1) continue;
                for (int i = 0; i < source.getTanks(); i++) {
                    FluidStack stack = source.getFluidInTank(i);
                    if (stack.isEmpty()) continue;
                    int toMove = (stack.getAmount() + tanksToMoveTo - 1) / (tanksToMoveTo);
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
                    copy.setAmount(stack.getAmount() - amount);
                    source.drainInput(copy, FluidAction.EXECUTE);
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
    private void onCheck(List<FluidConsumer> consumers, Path<IFluidPipe> path, Direction dir, long pos) {
        IFluidNode node = group.getNodes().get(pos).value();
        if (node.canInput()) consumers.add(new FluidConsumer(node, path, dir));
    }

    @Override
    public int insert(long producerPos, long pipePos, FluidStack stack, boolean simulate) {
        if (SLOOSH) return 0;
        if (stack.isEmpty()) return 0;
        long key = producerPos == pipePos ? pipePos : Pos.sub(producerPos, pipePos);
        Direction dir = producerPos == pipePos ? Direction.NORTH : Direction.byLong(Pos.unpackX(key), Pos.unpackY(key), Pos.unpackZ(key));
        Map<Direction, List<FluidConsumer>> map = this.data.get(producerPos);
        if (map == null) return 0;
        List<FluidConsumer> list = map.get(dir);
        if (list == null) return 0;

        FluidStack newStack = stack.copy();
        pressureData.clear();

        int outputAmount = stack.getAmount();
        loop:
        for (FluidConsumer consumer : list) {
            newStack.setAmount(outputAmount);
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
                        FluidHolder holder = holders.get(entry.getLongKey());
                        if (holder != null && !holder.allowFluid(newStack.getFluid())) {
                            amount = 0;
                            break;
                        }
                        long tempData = pressureData.get(entry.getLongKey());
                        amount = Math.min(amount, (holder != null || tempData > 0) ? entry.getValue().getPressure() - (holder != null ? holder.getPressure() : 0) - pressureData.get(entry.getLongKey()) : entry.getValue().getPressure());
                        if (amount == 0) continue loop;
                    }
                }
            }
            newStack.setAmount(amount);
            if (newStack.isEmpty()) continue;


            int temperature = stack.getFluid().getAttributes().getTemperature();
            boolean isGaseous = stack.getFluid().getAttributes().isGaseous();

            // Stores the pressure into holder for path only for variate connection
            if (!simulate) {
                boolean cantHandle = !consumer.canHandle(temperature, amount, isGaseous);
                for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull().long2ObjectEntrySet()) {
                    long pos = p.getLongKey();
                    IFluidPipe pipe = p.getValue();
                    if (!cantHandle) {
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
                    //Don't add more pressures if the stack is empty.
                    if (newStack.isEmpty()) break;

                    FluidHolder holder = holders.computeIfAbsent(pos, h -> new FluidHolder(pipe));
                    holder.add(amount, stack.getFluid());

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

            if (simulate) {
                //Insert temporary pressures.
                for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull().long2ObjectEntrySet()) {
                    final int finalAmount = amount;
                    pressureData.compute(p.getLongKey(), (k, v) -> v == null ? finalAmount : v + finalAmount);
                }
            }

            if (!simulate) {
                maxTemperature = Math.max(temperature, maxTemperature);
                totalPressure += amount;
            }

            if (!simulate && !newStack.isEmpty())
                consumer.insert(newStack, false);

            outputAmount -= amount;
            if (outputAmount <= 0) {
                break;
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
        holders.clear();
    }

    @Nullable
    public FluidHolder getCableHolder(long pos) {
        return holders.get(pos);
    }

    @Override
    public void getInfo(long pos, @Nonnull List<String> list) {
        this.group.getGroupInfo(pos, list);
        list.add(String.format("Fluid Data size: %d", this.data.size()));
    }
/*    @Override
    public String[] getInfo(long pos) {
        return new String[]{
                "Maximum Temperature: ".concat(Integer.toString(lastTemperature)),
                "Total Pressure: ".concat(Long.toString(lastPressure)),
                "Average pressure/tick: ".concat(Long.toString(lastPressure / 20)),
                "Any Leaks: ".concat(lastLeaking ? "Yes" : "No"),
        };
    }*/

    @Override
    public ITickingController clone(INode group) {
        return new FluidController(dim).set(group);
    }

    protected static class Neighbour {
        public final IFluidPipe source;
        public final long pos;
        public final List<Tuple<Direction, Either<IFluidPipe, IFluidNode>>> neighbours;

        public Neighbour(IFluidPipe source, long pos, List<Tuple<Direction, Either<IFluidPipe, IFluidNode>>> neighbours) {
            this.source = source;
            this.pos = pos;
            this.neighbours = neighbours;
        }
    }
}
