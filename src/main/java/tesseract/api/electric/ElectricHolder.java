package tesseract.api.electric;

/**
 * A class that acts a holder of the packets that has passed thought cables.
 */
public class ElectricHolder {

    private int amperage;
    private final int max;

    /**
     * Creates instance of the holder.
     *
     * @param max The maximum amperage.
     * @param amperage The initial amperage.
     */
    protected ElectricHolder(int max, int amperage) {
        this.max = max;
        this.amperage = amperage;
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
        return max < amperage;
    }
}
