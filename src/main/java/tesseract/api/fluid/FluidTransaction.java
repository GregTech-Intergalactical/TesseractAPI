package tesseract.api.fluid;

import net.minecraftforge.fluids.FluidStack;
import tesseract.api.Transaction;

import java.util.function.Consumer;

public class FluidTransaction extends Transaction<FluidStack> {

    public final FluidStack stack;

    public FluidTransaction(FluidStack stack, Consumer<FluidStack> consumer) {
        super(consumer);
        this.stack = stack;
    }

    public void addData(FluidStack stack, Consumer<FluidStack> consumer) {
        this.addData(stack);
        this.stack.setAmount(this.stack.getAmount() - stack.getAmount());
        this.onCommit(consumer);
    }

    @Override
    public boolean isValid() {
        return stack.getAmount() > 0;
    }

    @Override
    public boolean canContinue() {
        return false;
    }
}
