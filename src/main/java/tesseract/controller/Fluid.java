package tesseract.controller;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tesseract.api.fluid.FluidController;

import javax.annotation.Nonnull;

// TODO: Make explosions depend on pressure, capacity, temperature
public class Fluid extends FluidController {

    private long lastGasLeakSound = 0;
    private static final int GAS_WAIT_TIME = 40;

    /**
     * Creates instance of the tesseract.controller.
     *
     * @param dim The dimension id.
     */
    public Fluid(World dim) {
        super(dim);
    }

    @Override
    public void onPipeOverPressure(World w, long pos, int pressure, FluidStack fluid) {
        Utils.createExplosion(w, BlockPos.of(pos), 4.0F, Explosion.Mode.BREAK);
    }

    @Override
    public void onPipeOverCapacity(World w, long pos, int capacity, FluidStack fluid) {
        Utils.createExplosion(w, BlockPos.of(pos), 1.0F, Explosion.Mode.NONE);
    }

    @Override
    public void onPipeOverTemp(World w, long pos, int temperature) {
        w.setBlockAndUpdate(BlockPos.of(pos), temperature >= Fluids.LAVA.getAttributes().getTemperature() ? Blocks.LAVA.defaultBlockState() : Blocks.FIRE.defaultBlockState());
    }

    @Override
    public FluidStack onPipeGasLeak(World world, long pos, @Nonnull FluidStack fluid) {
        if (fluid.isEmpty()) return fluid;
        FluidStack stack = fluid.copy();
        stack.setAmount((int) ((double) stack.getAmount() * PIPE_LEAK));
        if ((world.getGameTime() - lastGasLeakSound) > GAS_WAIT_TIME) {
            world.playSound(null, BlockPos.of(pos), SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.3F, 0.9F + world.random.nextFloat() * 0.2F);
            lastGasLeakSound = world.getGameTime();
        }
        return stack;
    }
}
