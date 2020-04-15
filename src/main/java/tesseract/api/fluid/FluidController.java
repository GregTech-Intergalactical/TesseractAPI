package tesseract.api.fluid;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.Absorber;
import tesseract.api.ConnectionType;
import tesseract.api.Consumer;
import tesseract.api.Controller;
import tesseract.graph.Group;
import tesseract.graph.INode;
import tesseract.graph.ITickingController;
import tesseract.graph.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static tesseract.TesseractAPI.GLOBAL_FLUID_EVENT;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController extends Controller<IFluidPipe, IFluidNode> {

    private Long2ObjectMap<FluidAbsorber> absorbs = new Long2ObjectLinkedOpenHashMap<>();
    private Object2ObjectMap<IFluidNode, ObjectList<FluidConsumer>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    public FluidController(int dim, Group<IFluidPipe, IFluidNode> group) {
        super(dim, group);
    }

    @Override
    public void change() {
        //TODO: Finish change method
    }

    /**
     * Call on the updates to send fluids.
     */
    @Override
    public void tick() {
        absorbs.clear();

        for (Object2ObjectMap.Entry<IFluidNode, ObjectList<FluidConsumer>> e : data.object2ObjectEntrySet()) {
            IFluidNode producer = e.getKey();
            int outputAmount = Math.min(producer.getOutputPressure(), producer.getCapacity());

            for (FluidConsumer consumer : e.getValue()) {
                Object stack = producer.extract(outputAmount, true);
                if (stack == null || !consumer.canHold(stack)) {
                    continue;
                }

                outputAmount = producer.getAmount(stack);
                Object fluid = producer.getFluid(stack);
                int temperature = producer.getTemperature(fluid);
                boolean isGaseous = producer.isGaseous(fluid);

                int amount = consumer.insert(stack, true);
                if (amount <= 0) {
                    continue;
                }

                Object drained = producer.extract(amount, false);
                assert drained != null;

                // If we are here, then path had some invalid pipes which not suits the limits of temp/pressure/gas
                if (!consumer.canHandle(temperature, amount, isGaseous) && consumer.getConnection() != ConnectionType.ADJACENT) { // Fast check by the lowest pipe cable
                    // Find corrupt pipe and return
                    for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull()) {
                        IFluidPipe pipe = p.getValue();
                        long pos = p.getLongKey();

                        switch (pipe.getHandler(temperature, amount, isGaseous)) {
                            case FAIL_TEMP:
                                GLOBAL_FLUID_EVENT.onPipeOverTemp(dim, pos, temperature);
                                return;
                            case FAIL_PRESSURE:
                                GLOBAL_FLUID_EVENT.onPipeOverPressure(dim, pos, amount);
                                return;
                            case FAIL_LEAK:
                                GLOBAL_FLUID_EVENT.onPipeGasLeak(dim, pos, drained);
                                break;
                        }
                    }
                }

                // Stores the pressure into holder for path only for variate connection
                if (consumer.getConnection() == ConnectionType.VARIATE) {
                    for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getCross()) {
                        IFluidPipe pipe = p.getValue();
                        long pos = p.getLongKey();

                        FluidAbsorber a = absorbs.get(pos);
                        if (a == null) {
                            absorbs.put(pos, new FluidAbsorber(pipe.getCapacity(), pipe.getPressure(), amount, fluid));
                        } else {
                            a.add(amount, fluid);
                        }
                    }
                }

                consumer.insert(drained, false);

                outputAmount -= amount;
                if (outputAmount <= 0)
                    break;
            }
        }

        for (Long2ObjectMap.Entry<FluidAbsorber> e : absorbs.long2ObjectEntrySet()) {
            FluidAbsorber absorber = e.getValue();
            long pos = e.getLongKey();

            if (absorber.isOverPressure()) {
                GLOBAL_FLUID_EVENT.onPipeOverPressure(dim, pos, absorber.get());
            }
            if (absorber.isOverCapacity()) {
                GLOBAL_FLUID_EVENT.onPipeOverCapacity(dim, pos, absorber.fluids.size());
            }
        }
    }

    /**
     * Creates new controller for split group.
     *
     * @param group The new group.
     * @return New controller for the group.
     */
    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public ITickingController clone(@Nonnull INode group) {
        assert (group instanceof Group<?, ?>);
        return new FluidController(dim, (Group<IFluidPipe, IFluidNode>) group);
    }

    /**
     * A class that acts as absorber of the fluid that has passed thought pipes.
     */
    private static class FluidAbsorber extends Absorber {

        int capacity;
        ObjectSet<Object> fluids = new ObjectOpenHashSet<>();

        /**
         * Creates instance of the absorber.
         *
         * @param capacity The maximum capacity.
         * @param max The maximum pressure.
         * @param pressure The initial pressure.
         * @param fluid The initial fluid.
         */
        public FluidAbsorber(int capacity, int max, int pressure, Object fluid) {
            super(max, pressure);
            this.capacity = capacity;
            this.fluids.add(fluid);
        }

        /**
         * Adds a new liquid.
         *
         * @param pressure The added pressure.
         * @param fluid The fluid type.
         */
        public void add(int pressure, Object fluid) {
            super.add(pressure);
            fluids.add(fluid);
        }

        /**
         * @return Checks that the absorber is not able to handle pressure.
         */
        public boolean isOverPressure() {
            return super.isOver();
        }

        /**
         * @return Checks that the absorber is not able to handle pressure.
         */
        public boolean isOverCapacity() {
            return fluids.size() > capacity;
        }

        @Override
        @Deprecated
        public void add(int value) {
        }

        @Override
        @Deprecated
        public boolean isOver() {
            return false;
        }
    }

    /**
     * A class that acts as a container for a fluid consumer.
     */
    private static class FluidConsumer extends Consumer<IFluidPipe, IFluidNode> {

        int isProof = 1;
        int minCapacity = Integer.MAX_VALUE;
        int minPressure = Integer.MAX_VALUE;
        int minTemperature = Integer.MAX_VALUE;

        /**
         * Creates instance of the consumer.
         *
         * @param consumer The consumer node.
         * @param path The path information.
         */
        FluidConsumer(@Nonnull IFluidNode consumer, @Nullable Path<IFluidPipe> path) {
            super(consumer, path);
        }

        /**
         * Adds fluid to the node. Returns amount of fluid that was filled.
         *
         * @param stack FluidStack attempting to fill the tank.
         * @param simulate If true, the fill will only be simulated.
         * @return Amount of fluid that was accepted (or would be, if simulated) by the tank.
         */
        int insert(Object stack, boolean simulate) {
            return consumer.insert(stack, simulate);
        }

        /**
         * @param stack FluidStack holding the Fluid to be queried.
         * @return If the tank can hold the fluid (EVER, not at the time of query).
         */
        boolean canHold(@Nonnull Object stack) {
            return consumer.canHold(stack);
        }

        /**
         * @param temperature The current temperature.
         * @param pressure The current pressure.
         * @param proof True if current liquid is in a gas state.
         * @return Checks that the consumer is able to receive fluid.
         */
        boolean canHandle(int temperature, int pressure, boolean proof) {
            return minTemperature >= temperature && minPressure >= pressure && isProof == (proof ? 1 : 0);
        }

        @Override
        protected void onConnectorCatch(@Nonnull IFluidPipe pipe) {
            isProof = Math.min(isProof, pipe.isGasProof() ? 1 : 0);
            minTemperature = Math.min(minTemperature, pipe.getTemperature());
            minCapacity = Math.min(minCapacity, pipe.getCapacity());
            minPressure = Math.min(minPressure, pipe.getPressure());
        }
    }
}
