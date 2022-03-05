package tesseract.controller;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import tesseract.api.fluid.FluidController;
import tesseract.api.fluid.IFluidNode;
import tesseract.graph.Graph;

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
    public Fluid(Level dim, Graph.INodeGetter<IFluidNode> get) {
        super(dim, get);
    }

    @Override
    public void onPipeOverPressure(Level w, long pos, int pressure, FluidStack fluid) {
        Utils.createExplosion(w, BlockPos.of(pos), 4.0F, Explosion.BlockInteraction.BREAK);
    }

    @Override
    public void onPipeOverCapacity(Level w, long pos, int capacity, FluidStack fluid) {
        Utils.createExplosion(w, BlockPos.of(pos), 1.0F, Explosion.BlockInteraction.NONE);
    }

    @Override
    public void onPipeOverTemp(Level w, long pos, int temperature) {
        w.setBlockAndUpdate(BlockPos.of(pos), temperature >= Fluids.LAVA.getAttributes().getTemperature() ? Blocks.LAVA.defaultBlockState() : Blocks.FIRE.defaultBlockState());
    }

    @Override
    public FluidStack onPipeGasLeak(Level world, long pos, @Nonnull FluidStack fluid) {
        if (fluid.isEmpty()) return fluid;
        FluidStack stack = fluid.copy();
        stack.setAmount((int) ((double) stack.getAmount() * PIPE_LEAK));
        if ((world.getGameTime() - lastGasLeakSound) > GAS_WAIT_TIME) {
            world.playSound(null, BlockPos.of(pos), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 0.9F + world.random.nextFloat() * 0.2F);
            lastGasLeakSound = world.getGameTime();
        }
        return stack;
    }
}
