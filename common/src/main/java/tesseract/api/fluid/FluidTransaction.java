package tesseract.api.fluid;



import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import tesseract.api.Transaction;

import java.util.function.Consumer;

public class FluidTransaction extends Transaction<FluidHolder> {

    public final FluidHolder stack;

    public FluidTransaction(FluidHolder stack, Consumer<FluidHolder> consumer) {
        super(consumer);
        this.stack = stack;
    }

    public void addData(FluidHolder stack, Consumer<FluidHolder> consumer) {
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
