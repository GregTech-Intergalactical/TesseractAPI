package tesseract.api;

/**
 * A class that acts as holder of the data that has passed thought connectors.
 */
public class Absorber {

    private int max;
    private int value;

    /**
     * Creates instance of the holder.
     *
     * @param max The maximum value.
     * @param value The initial value.
     */
    public Absorber(int max, int value) {
        this.max = max;
        this.value = value;
    }

    /**
     * Adds a new value.
     *
     * @param value The added value.
     */
    public void add(int value) {
        this.value += value;
    }

    /**
     * @return Checks that the absorber is able to handle it.
     */
    public boolean canHandle() {
        return max >= value;
    }
}
