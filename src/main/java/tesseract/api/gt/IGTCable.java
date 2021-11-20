package tesseract.api.gt;

import tesseract.api.IConnectable;

/**
 * An electric cable is the unit of interaction with electric inventories.
 */
public interface IGTCable extends IConnectable {

    /**
     * Returns the energy that this electrical component will permit to lost through or be received in a single tick.
     *
     * @return A positive integer representing the loss energy per block, zero or negative indicates that this component doesn't have a loss.
     */
    int getLoss();

    /**
     * Returns the maximum amount of packets that this electrical component will permit to pass through or be received in a single tick.
     *
     * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no energy.
     */
    int getAmps();

    /**
     * Returns the maximum energy that this electrical component will permit to pass through or be received in a single packet.
     *
     * @return A positive integer representing the maximum accepted energy, zero or negative indicates that this component accepts no energy.
     */
    int getVoltage();

    /**
     * @param voltage  The current voltage.
     * @param amperage The current amperage.
     * @return Checks that the cable is able to handle single packet.
     */
    default GTStatus getHandler(long voltage, long amperage) {
        if (getVoltage() < voltage) return GTStatus.FAIL_VOLTAGE;
        else if (getAmps() < amperage) return GTStatus.FAIL_AMPERAGE;
        return GTStatus.SUCCESS;
    }
}

