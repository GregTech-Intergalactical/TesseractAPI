package tesseract.api.fluid;


import io.github.fabricators_of_create.porting_lib.extensions.RegistryNameProvider;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidStack;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A class that acts as holder of the fluid that has passed thought pipes.
 */
public class FluidHolder {

    private int pressureAvailable;
    public final int tickPressure, maxCapacity;
    private final Set<SetHolder> fluids = new ObjectOpenHashSet<>();

    /**
     * Creates instance of the holder.
     *
     * @param pipe The pipe connector.
     */
    public FluidHolder(IFluidPipe pipe) {
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
            return ((RegistryNameProvider)fluid).getRegistryName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            ResourceLocation compare = ((RegistryNameProvider)this.fluid).getRegistryName();
            if (obj instanceof SetHolder s) {
                return ((RegistryNameProvider)s.fluid).getRegistryName().equals(compare);
            }
            if (obj instanceof Fluid f) {
                return ((RegistryNameProvider)f).getRegistryName().equals(compare);
            }
            if (obj instanceof FluidStack s) {
                return ((RegistryNameProvider)s.getFluid()).getRegistryName().equals(compare);
            }
            if (obj instanceof ResourceLocation r) {
                return r.equals(compare);
            }
            return false;
        }
    }
}