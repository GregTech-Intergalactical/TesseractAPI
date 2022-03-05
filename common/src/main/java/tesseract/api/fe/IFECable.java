package tesseract.api.fe;

import tesseract.api.IConnectable;

/**
 * A flux cable is the unit of interaction with electric inventories.
 */
public interface IFECable extends IConnectable {

    /**
     * Returns the maximum amount of energy that this item component will permit to pass through or be received in a single tick.
     *
     * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no energy.
     */
    long getCapacity();
}