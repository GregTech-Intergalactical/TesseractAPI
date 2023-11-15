package tesseract.api.gt;

import tesseract.api.Transaction;

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
        return availableAmps > 0 || eu > 0;
    }

    public long getAvailableAmps() {
        return this.availableAmps;
    }

    public long addAmps(long amps) {
        this.availableAmps += amps;
        return amps;
    }

    public TransferData addData(long amps, double loss, Consumer<TransferData> data) {
        TransferData td = new TransferData(this, Math.min(amps, availableAmps), this.voltageOut).setLoss(loss);
        this.addData(td);
        this.usedAmps += amps;
        availableAmps -= amps;
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
        private double loss;
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
            return input ? (voltage - Math.round(loss)) * amps : voltage * amps;
        }

        public long getTotalAmperage() {
            return totalAmperage;
        }

        public double getLoss() {
            return loss;
        }

        public TransferData setLoss(double loss) {
            this.loss = Math.min(this.voltage, loss);
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

        public long consumeForNode(IGTNode node) {
            if (this.transaction.mode == GTTransaction.Mode.TRANSMIT) {
                long amps = Math.min(getAmps(true), node.availableAmpsInput(this.getVoltage()));
                amps = Math.min(amps, (node.getCapacity() - node.getEnergy()) / node.getInputVoltage());
                useAmps(true, amps);
                node.getState().receive(false, amps);
                return getEnergy(amps, true);
            } else {
                long toAdd = Math.min(getEu(), node.getCapacity() - node.getEnergy());
                return drainEu(toAdd);
            }
        }

        public long extractForNode(IGTNode node) {
            if (transaction.mode == GTTransaction.Mode.TRANSMIT) {
                long amps = Math.min(getAmps(false), node.availableAmpsOutput());
                amps = Math.min(amps, node.getEnergy() / node.getOutputVoltage());
                node.getState().extract(false, amps);
                useAmps(false, amps);
                return getEnergy(amps, false);
            } else {
                long toDrain = Math.min(getEu(), node.getEnergy());
                return drainEu(toDrain);
            }
    
        }

        public long getVoltage() {
            return voltage;
        }

        public GTTransaction getTransaction() {
            return transaction;
        }
    }

    public enum Mode {
        INTERNAL,
        TRANSMIT
    }
}
