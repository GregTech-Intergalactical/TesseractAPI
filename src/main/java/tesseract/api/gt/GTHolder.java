package tesseract.api.gt;

/**
 * A class that acts a holder of the packets that has passed thought cables.
 */
public class GTHolder {

    private int amperage;
    private final int maxAmperage;

    /**
     * Creates instance of the holder.
     *
     * @param cable The cable connector.
     */
    protected GTHolder(IGTCable cable) {
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
