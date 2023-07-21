package tesseract.api.rf;

import tesseract.api.Transaction;

import java.util.function.Consumer;

public class RFTransaction extends Transaction<Long> {
    public long rf;
    public RFTransaction(long rf, Consumer<Long> consumed) {
        super(consumed);
        this.rf = rf;
    }

    public void addData(long rf, Consumer<Long> consumer){
        this.addData(rf);
        this.rf -= rf;
        this.onCommit(consumer);
    }

    @Override
    public boolean isValid() {
        return rf > 0;
    }

    @Override
    public boolean canContinue() {
        return rf > 0;
    }
}
