package tesseract.controller;

import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tesseract.api.fluid.FluidController;
import tesseract.api.fluid.FluidData;
import tesseract.api.fluid.IFluidNode;

import javax.annotation.Nonnull;
import java.util.function.Function;

// TODO: Make explosions depend on pressure, capacity, temperature
public class Fluid<T> extends FluidController<T, IFluidNode<T>> {

    /**
     * Creates instance of the tesseract.controller.
     *
     * @param dim The dimension id.
     */
    public Fluid(Function<RegistryKey<World>, ServerWorld> supplier, RegistryKey<World> dim) {
        super(supplier,dim);
    }

    @Override
    public void onPipeOverPressure(ServerWorld w, long pos, int pressure) {
        Utils.createExplosion(w, BlockPos.fromLong(pos), 4.0F, Explosion.Mode.BREAK);
    }

    @Override
    public void onPipeOverCapacity(ServerWorld w, long pos, int capacity) {
        Utils.createExplosion(w, BlockPos.fromLong(pos), 1.0F, Explosion.Mode.NONE);
    }

    @Override
    public void onPipeOverTemp(ServerWorld w, long pos, int temperature) {
        w.setBlockState(BlockPos.fromLong(pos), temperature >= Fluids.LAVA.getAttributes().getTemperature() ? Blocks.LAVA.getDefaultState() : Blocks.FIRE.getDefaultState());
    }

    @Override
    public void onPipeGasLeak(ServerWorld w, long pos, @Nonnull FluidData<T> fluid) {
        T resource = fluid.getStack();
       // resource.setAmount((int)(resource.getAmount() * AntimatterConfig.GAMEPLAY.PIPE_LEAK));
    }
}
