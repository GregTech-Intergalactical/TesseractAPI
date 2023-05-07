package tesseract.api.fluid;

import earth.terrarium.botarium.api.fluid.FluidHolder;
import tesseract.api.Transaction;

import java.util.function.Consumer;

public class FluidTransaction extends Transaction<earth.terrarium.botarium.api.fluid.FluidHolder> {

    public final earth.terrarium.botarium.api.fluid.FluidHolder stack;

    public FluidTransaction(earth.terrarium.botarium.api.fluid.FluidHolder stack, Consumer<earth.terrarium.botarium.api.fluid.FluidHolder> consumer) {
        super(consumer);
        this.stack = stack;
    }

    public void addData(earth.terrarium.botarium.api.fluid.FluidHolder stack, Consumer<FluidHolder> consumer) {
        this.addData(stack);
        this.stack.setAmount(this.stack.getFluidAmount() - stack.getFluidAmount());
        this.onCommit(consumer);
    }

    @Override
    public boolean isValid() {
        return stack.getFluidAmount() > 0;
    }

    @Override
    public boolean canContinue() {
        return false;
    }
}
