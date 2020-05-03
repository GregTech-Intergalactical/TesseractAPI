package tesseract.api.electric;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A class that acts a holder of the packets that has passed thought cables.
 */
@ParametersAreNonnullByDefault
public class ElectricHolder {

    private int amperage;
    private final int maxAmperage;

    /**
     * Creates instance of the holder.
     *
     * @param cable The cable connector.
     */
    protected ElectricHolder(IElectricCable cable) {
        this.maxAmperage = cable.getAmps();
    }

    /**
     * Adds a new amperage.
     *
     * @param amperage The added amperage.
     */
    public void add(int amperage) {
        this.amperage += amperage;
    }

    /**
     * @return Gets a current amperage.
     */
    public int getAmperage() {
        return amperage;
    }

    /**
     * @return Checks that the holder is not able to handle it.
     */
    public boolean isOverAmperage() {
        return maxAmperage < amperage;
    }
}
