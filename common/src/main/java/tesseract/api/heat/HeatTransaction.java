package tesseract.api.heat;

import tesseract.api.Transaction;

import java.util.function.Consumer;

public class HeatTransaction extends Transaction<Integer> {

    private int heatSize;
    private int temperature;
    private int usedHeat;

    public HeatTransaction(int heatSize, int temperature, Consumer<Integer> con) {
        super(con);
        this.heatSize = heatSize;
        this.temperature = temperature;
    }

    public void limitHeat(int heat) {
        this.heatSize = Math.min(heat, heatSize);
        this.heatSize = Math.max(heatSize, 0);
    }

    public int getTemperature() {
        return temperature;
    }

    public HeatTransaction ignoreTemperature() {
        this.temperature = -1;
        return this;
    }

    @Override
    public boolean isValid() {
        return heatSize > 0 && this.temperature > 0;
    }

    @Override
    public boolean canContinue() {
        return usedHeat < heatSize;
    }

    public int available() {
        return heatSize - usedHeat;
    }

    public int getUsedHeat() {
        return usedHeat;
    }

    public void addData(int heatAmount, int temperature, Consumer<Integer> consumer) {
        if (heatAmount == 0) return;
        if (temperature > this.temperature && this.temperature != -1 && temperature != -1) return;
        this.usedHeat += heatAmount;
        this.addData(heatAmount);
        this.onCommit(consumer);
    }
}
