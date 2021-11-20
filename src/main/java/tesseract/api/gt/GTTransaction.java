package tesseract.api.gt;

import tesseract.api.Transaction;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class GTTransaction extends Transaction<GTTransaction.TransferData> {

    public long availableAmps;
    public final long voltageOut;
    public long eu;
    public long usedAmps;
    public final Mode mode;

    public GTTransaction(long ampsAvailable, long voltageOut, Consumer<TransferData> consumer) {
        super(consumer);
        this.availableAmps = ampsAvailable;
        this.voltageOut = voltageOut;
        this.usedAmps = 0;
        this.mode = Mode.TRANSMIT;
    }

    public GTTransaction(long eu, Consumer<TransferData> consumer) {
        super(consumer);
        this.voltageOut = 0;
        this.eu = eu;
        this.mode = Mode.INTERNAL;
    }


    @Override
    public boolean isValid() {
        return (this.availableAmps > 0 && this.voltageOut > 0) || this.eu > 0;
    }

    @Override
    public boolean canContinue() {
        return availableAmps > usedAmps || eu > 0;
    }

    public long getAvailableAmps() {
        return this.availableAmps - this.usedAmps;
    }

    public long addAmps(long amps) {
        this.availableAmps += amps;
        return amps;
    }

    public TransferData addData(long amps, long loss, Consumer<TransferData> data, @Nullable Consumer<TransferData> modifier) {
        TransferData td = new TransferData(this, Math.min(amps, availableAmps - usedAmps), this.voltageOut).setLoss(loss);
        if (modifier != null) {
            modifier.accept(td);
        }
        this.addData(td);
        this.usedAmps += amps;
        this.onCommit(data);
        return td;
    }

    public TransferData addData(long eu, Consumer<TransferData> data) {
        eu = Math.min(eu, this.eu);
        TransferData dat = this.addData(new TransferData(this, eu));
        this.eu -= eu;
        this.onCommit(data);
        return dat;
    }

    public static class TransferData {
        private final long voltage;
        private long eu;
        private long ampsIn;
        private long ampsOut;
        private final long totalAmperage;
        private long loss;
        public final GTTransaction transaction;

        public TransferData(GTTransaction transaction, long amps, long voltage) {
            this.ampsIn = this.ampsOut = this.totalAmperage = amps;
            this.voltage = voltage;
            this.loss = 0;
            this.eu = 0;
            this.transaction = transaction;
        }

        public TransferData(GTTransaction transaction, long eu) {
            this.voltage = 0;
            this.eu = eu;
            this.totalAmperage = 0;
            this.transaction = transaction;
        }

        public long getEnergy(long amps, boolean input) {
            return input ? (voltage - loss) * amps : voltage * amps;
        }

        public long getTotalAmperage() {
            return totalAmperage;
        }

        public long getLoss() {
            return loss;
        }

        public TransferData setLoss(long loss) {
            this.loss = loss;
            return this;
        }

        public long getEu() {
            return eu;
        }

        public long drainEu(long eu) {
            this.eu -= eu;
            return eu;
        }

        public long getAmps(boolean input) {
            return input ? ampsIn : ampsOut;
        }

        public void useAmps(boolean input, long amps) {
            if (input) {
                ampsIn -= amps;
            } else {
                ampsOut -= amps;
            }
        }

        @Override
        public String toString() {
            if (transaction.mode == Mode.INTERNAL) {
                return "Internal: " + this.eu;
            } else {
                return "Transmit amps: " + this.totalAmperage + "  voltage: " + this.voltage + " loss: " + this.loss;
            }
        }

        public long getVoltage() {
            return voltage;
        }
    }

    public enum Mode {
        INTERNAL,
        TRANSMIT
    }
}
