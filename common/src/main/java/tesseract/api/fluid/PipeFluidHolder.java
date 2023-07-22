package tesseract.api.fluid;


import earth.terrarium.botarium.api.fluid.FluidHolder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import tesseract.FluidPlatformUtils;

import java.util.Set;

/**
 * A class that acts as holder of the fluid that has passed thought pipes.
 */
public class PipeFluidHolder {

    private long pressureAvailable;
    public long tickPressure;
    public final int maxCapacity;
    private final Set<SetHolder> fluids = new ObjectOpenHashSet<>();

    /**
     * Creates instance of the holder.
     *
     * @param pipe The pipe connector.
     */
    public PipeFluidHolder(IFluidPipe pipe) {
        this.maxCapacity = pipe.getCapacity();
        this.tickPressure = pipe.getPressureInDroplets();

        this.pressureAvailable = tickPressure * 20L;
    }

    public void tick(long time) {
        pressureAvailable = Math.min(pressureAvailable + tickPressure, tickPressure * 20);
        this.fluids.removeIf(t -> time - t.timeAdded >= 20);
    }

    /**
     * Uses up a part of this pipe and adds the fluid to the set.
     *  @param pressure The added pressure.
     * @param fluid    The fluid type.
     */
    public void use(long pressure, Fluid fluid, long currentTime) {
        this.pressureAvailable -= pressure;
        SetHolder holder = new SetHolder(fluid, currentTime);
        fluids.remove(holder);
        fluids.add(holder);
    }

    /**
     * @return Gets the current available pressure. If 0 then no liquid can be sent
     */
    public long getPressureAvailable() {
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

    public void clear() {
        this.fluids.clear();
        this.pressureAvailable = tickPressure*20;
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
            return FluidPlatformUtils.getFluidId(fluid).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            ResourceLocation compare = FluidPlatformUtils.getFluidId(fluid);
            if (obj instanceof SetHolder s) {
                return FluidPlatformUtils.getFluidId(s.fluid).equals(compare);
            }
            if (obj instanceof Fluid f) {
                return FluidPlatformUtils.getFluidId(f).equals(compare);
            }
            if (obj instanceof FluidHolder s) {
                return FluidPlatformUtils.getFluidId(s.getFluid()).equals(compare);
            }
            if (obj instanceof ResourceLocation r) {
                return r.equals(compare);
            }
            return false;
        }
    }
}
