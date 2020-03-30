package tesseract.api.electric;

/**
 * A class that acts as a container for a cable.
 */
public class Cable {

    private long amperage;
    private IElectricCable cable;

    /**
     * Creates instance of the cable.
     *
     * @param cable The cable connector.
     * @param amperage The initial amps amount.
     */
    protected Cable(IElectricCable cable, long amperage) {
        this.cable = cable;
        this.amperage = amperage;
    }

    /**
     * Adds a new value to the amperage.
     */
    public void add(long value) {
        amperage += value;
    }

    /**
     * @return Checks that cable is able to transfer energy.
     */
    public boolean isValid() {
        return cable.getAmps() <= amperage;
    }

    /**
     * @return Gets the cables ref.
     */
    public IElectricCable getCable() {
        return cable;
    }
}