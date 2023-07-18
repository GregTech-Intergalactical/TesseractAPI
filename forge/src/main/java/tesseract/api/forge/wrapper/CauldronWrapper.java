package tesseract.api.forge.wrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CauldronWrapper implements IFluidHandler {
    private BlockState state;
    private final Level level;
    private final BlockPos pos;

    public CauldronWrapper(BlockState state, Level level, BlockPos pos) {
        this.state = state;
        this.level = level;
        this.pos = pos;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int i) {
        state = level.getBlockState(pos);
        if (state.getBlock() == Blocks.WATER_CAULDRON) {
            int level = state.getValue(LayeredCauldronBlock.LEVEL);
            int amount = level == 1 ? 334 : level == 2 ? 667 : 1000;
            return new FluidStack(Fluids.WATER, amount);
        }
        return state.getBlock() == Blocks.LAVA_CAULDRON ? new FluidStack(Fluids.LAVA, 1000) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int i) {
        return 1000;
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return fluidStack.getFluid() == Fluids.LAVA || fluidStack.getFluid() == Fluids.WATER;
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        this.state = level.getBlockState(pos);
        if (fluidStack.getFluid() == Fluids.LAVA && state.getBlock() == Blocks.CAULDRON) {
            if (fluidStack.getAmount() >= 1000) {
                if (fluidAction.execute()) level.setBlock(pos, Blocks.LAVA_CAULDRON.defaultBlockState(), 3);
                return 1000;
            }
        } else if (fluidStack.getFluid() == Fluids.WATER && (state.getBlock() == Blocks.CAULDRON || state.getBlock() == Blocks.WATER_CAULDRON)) {
            int cauldronLevel = state.getBlock() == Blocks.CAULDRON ? 0 : state.getValue(LayeredCauldronBlock.LEVEL);
            return switch (cauldronLevel) {
                case 0 -> {
                    if (fluidStack.getAmount() >= 1000) {
                        if (fluidAction.execute()) this.level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), 3);
                        yield 1000;
                    } else if (fluidStack.getAmount() >= 667){
                        if (fluidAction.execute()) this.level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2), 3);
                        yield 667;
                    } else if (fluidStack.getAmount() >= 334){
                        if (fluidAction.execute()) this.level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState(), 3);
                        yield 334;
                    }
                    yield 0;
                }
                case 1 -> {
                    if (fluidStack.getAmount() >= 667){
                        if (fluidAction.execute()) this.level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), 3);
                        yield 667;
                    } else if (fluidStack.getAmount() >= 334){
                        if (fluidAction.execute()) this.level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2), 3);
                        yield 334;
                    }
                    yield 0;
                }
                case 2 -> {
                    if (fluidStack.getAmount() >= 334){
                        if (fluidAction.execute()) this.level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), 3);
                        yield 334;
                    }
                    yield 0;
                }
                default -> 0;
            };
        }
        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        if ((state.getBlock() == Blocks.LAVA_CAULDRON && fluidStack.getFluid() == Fluids.LAVA) || (state.getBlock() == Blocks.WATER_CAULDRON && fluidStack.getFluid() == Fluids.WATER)){
            return drain(fluidStack.getAmount(), fluidAction);
        }
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int i, FluidAction fluidAction) {
        this.state = level.getBlockState(pos);
        if (state.getBlock() == Blocks.LAVA_CAULDRON && i >= 1000){
            if (fluidAction.execute()) this.level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
            return new FluidStack(Fluids.LAVA, 1000);
        } else if (state.getBlock() == Blocks.WATER_CAULDRON){
            int cauldronLevel = state.getValue(LayeredCauldronBlock.LEVEL);
            return switch (cauldronLevel){
                case 1 -> {
                    if (i >= 333){
                        if (fluidAction.execute()) this.level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                        yield new FluidStack(Fluids.WATER, 333);
                    }
                    yield FluidStack.EMPTY;
                }
                case 2 -> {
                    if (i >= 666){
                        if (fluidAction.execute()) this.level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                        yield new FluidStack(Fluids.WATER, 666);
                    } else if (i >= 333){
                        if (fluidAction.execute()) LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                        yield new FluidStack(Fluids.WATER, 333);
                    }
                    yield FluidStack.EMPTY;
                }
                case 3 -> {
                    if (i >= 1000){
                        if (fluidAction.execute()) this.level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                        yield new FluidStack(Fluids.WATER, 1000);
                    } else if (i >= 666){
                        if (fluidAction.execute()) this.level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1), 3);
                        yield new FluidStack(Fluids.WATER, 666);
                    } else if (i >= 333){
                        if (fluidAction.execute()) LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                        yield new FluidStack(Fluids.WATER, 333);
                    }
                    yield FluidStack.EMPTY;
                }
                default -> FluidStack.EMPTY;
            };
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CauldronWrapper) obj;
        return Objects.equals(this.state, that.state) &&
                Objects.equals(this.level, that.level) &&
                Objects.equals(this.pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, level, pos);
    }

    @Override
    public String toString() {
        return "CauldronWrapper[" +
                "state=" + state + ", " +
                "level=" + level + ", " +
                "pos=" + pos + ']';
    }

}
