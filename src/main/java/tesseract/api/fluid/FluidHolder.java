package tesseract.api.fluid;


import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.Set;

/**
 * A class that acts as holder of the fluid that has passed thought pipes.
 */
public class FluidHolder {

    private int pressureAvailable;
    private final int tickPressure, maxCapacity;
    private final Set<SetHolder> fluids = new ObjectOpenHashSet<>();

    /**
     * Creates instance of the holder.
     *
     * @param pipe The pipe connector.
     */
    protected FluidHolder(IFluidPipe pipe) {
        this.maxCapacity = pipe.getCapacity();
        this.tickPressure = pipe.getPressure();

        this.pressureAvailable = tickPressure * 20;
    }

    public void tick(long time) {
        pressureAvailable = Math.min(pressureAvailable + tickPressure, tickPressure * 20);
        this.fluids.removeIf(t -> time - t.timeAdded >= 20);
    }

    /**
     * Uses up a part of this pipe and adds the fluid to the set.
     *
     * @param pressure The added pressure.
     * @param fluid    The fluid type.
     */
    public void use(int pressure, Fluid fluid, long currentTime) {
        this.pressureAvailable -= pressure;
        SetHolder holder = new SetHolder(fluid, currentTime);
        fluids.remove(holder);
        fluids.add(holder);
    }

    /**
     * @return Gets the current available pressure. If 0 then no liquid can be sent
     */
    public int getPressureAvailable() {
        return pressureAvailable;
    }

    /**
     * @return Gets a current capacity.
     */
    public int getCapacity() {
        return fluids.size();
    }

    /**
     * @return Checks that the holder is not able to handle pressure.
     */
    public boolean isOverPressure() {
        return pressureAvailable < 0;
    }

    /**
     * @return Checks that the holder is not able to handle pressure.
     */
    public boolean isOverCapacity() {
        return maxCapacity < fluids.size();
    }

    public boolean allowFluid(Fluid fluid) {
        SetHolder holder = new SetHolder(fluid, 0);
        if (fluids.contains(holder)) {
            return true;
        }
        return maxCapacity > fluids.size();
    }

    public Set<SetHolder> getFluids() {
        return fluids;
    }

    public static class SetHolder {
        public final Fluid fluid;
        public long timeAdded;

        public SetHolder(final Fluid fluid, long added) {
            this.fluid = fluid;
            this.timeAdded = added;
        }

        @Override
        public int hashCode() {
            return fluid.getRegistryName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SetHolder) {
                return ((SetHolder) obj).fluid.getRegistryName().equals(this.fluid.getRegistryName());
            }
            if (obj instanceof Fluid) {
                return ((Fluid) obj).getRegistryName().equals(this.fluid.getRegistryName());
            }
            if (obj instanceof FluidStack) {
                return ((FluidStack) obj).getFluid().getRegistryName().equals(this.fluid.getRegistryName());
            }
            if (obj instanceof ResourceLocation) {
                return obj.equals(this.fluid.getRegistryName());
            }
            return false;
        }
    }
}
