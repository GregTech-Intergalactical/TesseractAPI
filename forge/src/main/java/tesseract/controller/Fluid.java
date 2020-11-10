package tesseract.controller;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import tesseract.api.fluid.FluidController;
import tesseract.api.fluid.FluidData;
import tesseract.api.fluid.IFluidNode;

import javax.annotation.Nonnull;

import static net.minecraft.world.Explosion.Mode.*;

// TODO: Make explosions depend on pressure, capacity, temperature
public class Fluid<T> extends FluidController<T, IFluidNode<T>> {

    /**
     * Creates instance of the tesseract.controller.
     *
     * @param dim The dimension id.
     */
    public Fluid(int dim) {
        super(dim);
    }

    @Override
    public void onPipeOverPressure(int dim, long pos, int pressure) {
        Utils.getServerWorld(dim).ifPresent(w -> Utils.createExplosion(w, BlockPos.fromLong(pos), 4.0F, DESTROY));
    }

    @Override
    public void onPipeOverCapacity(int dim, long pos, int capacity) {
        Utils.getServerWorld(dim).ifPresent(w -> Utils.createExplosion(w, BlockPos.fromLong(pos), 1.0F, BREAK));
    }

    @Override
    public void onPipeOverTemp(int dim, long pos, int temperature) {
        Utils.getServerWorld(dim).ifPresent(w -> w.setBlockState(BlockPos.fromLong(pos), temperature >= Fluids.LAVA.getAttributes().getTemperature() ? Blocks.LAVA.getDefaultState() : Blocks.FIRE.getDefaultState()));
    }

    @Override
    public void onPipeGasLeak(int dim, long pos, @Nonnull FluidData<T> fluid) {
        T resource = fluid.getStack();
       // resource.setAmount((int)(resource.getAmount() * AntimatterConfig.GAMEPLAY.PIPE_LEAK));
    }
}
