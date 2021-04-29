package tesseract.controller;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;
import tesseract.api.fluid.FluidController;
import tesseract.api.fluid.IFluidNode;

import javax.annotation.Nonnull;
import java.util.function.Function;

// TODO: Make explosions depend on pressure, capacity, temperature
public class Fluid extends FluidController<IFluidNode> {

    /**
     * Creates instance of the tesseract.controller.
     *
     * @param dim The dimension id.
     */
    public Fluid(World dim) {
        super(dim);
    }

    @Override
    public void onPipeOverPressure(World w, long pos, int pressure) {
        if (HARDCORE_PIPES) Utils.createExplosion(w, BlockPos.fromLong(pos), 4.0F, Explosion.Mode.BREAK);
    }

    @Override
    public void onPipeOverCapacity(World w, long pos, int capacity) {
        Utils.createExplosion(w, BlockPos.fromLong(pos), 1.0F, Explosion.Mode.NONE);
    }

    @Override
    public void onPipeOverTemp(World w, long pos, int temperature) {
        w.setBlockState(BlockPos.fromLong(pos), temperature >= Fluids.LAVA.getAttributes().getTemperature() ? Blocks.LAVA.getDefaultState() : Blocks.FIRE.getDefaultState());
    }

    @Override
    public FluidStack onPipeGasLeak(World world, long pos, @Nonnull FluidStack fluid) {
        return super.onPipeGasLeak(world, pos, fluid);
        //return new FluidData<T>(fluid.getStack(), (int) Math.floor(fluid.getAmount() * PIPE_LEAK), fluid.getTemperature(), fluid.isGaseous());
    }
}
