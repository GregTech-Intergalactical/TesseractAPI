package tesseract.api.gt;

import net.minecraft.core.Direction;
import tesseract.TesseractPlatformUtils;
import tesseract.api.GraphWrapper;


/**
 * An electric node is the unit of interaction with electric inventories.
 * <p>
 * Derived from the Redstone Flux power system designed by King Lemming and originally utilized in Thermal Expansion and related mods.
 * Created with consent and permission of King Lemming and Team CoFH. Released with permission under LGPL 2.1 when bundled with Forge.
 * Note: no longer derived from RF.
 * </p>
 */
public interface IGTNode {

    /**
     * Extracts a given amount of energy from the node.
     * @param voltage The amount to be extracted from the node.
     * @param simulate If true, the container will not be modified.
     * @return The amount of energy that was removed from the node.
     */
    long extractEu(long voltage, boolean simulate);
    /**
     * Inserts a given amount of energy into the node.
     * @param voltage The amount to be inserted into the node.
     * @param simulate If true, the container will not be modified.
     * @return The amount of energy that was added to the node.
     */
    long insertEu(long voltage, boolean simulate);

    /**
     * @return Gets the amount of energy currently stored.
     */
    long getEnergy();

    /**
     * @return Gets the maximum amount of energy that can be stored.
     */
    long getCapacity();

    /**
     * @return Gets the maximum amount of amperage that can be output.
     */
    long getOutputAmperage();

    /**
     * @return Gets the maximum amount of voltage that can be output.
     */
    long getOutputVoltage();

    /**
     * @return Gets the maximum amount of amperage that can be input.
     */
    long getInputAmperage();

    /**
     * @return Gets the maximum amount of voltage that can be input.
     */
    long getInputVoltage();

    /**
     * Gets if this storage can have energy extracted.
     *
     * @return If this is false, then any calls to extractEnergy will return 0.
     */
    boolean canOutput();

    /**
     * Used to determine if this storage can receive energy.
     *
     * @return If this is false, then any calls to receiveEnergy will return 0.
     */
    boolean canInput();

    /**
     * Used to determine if this storage can receive energy in the given direction.
     *
     * @param direction the direction.
     * @return If this is false, then any calls to receiveEnergy will return 0.
     */
    boolean canInput(Direction direction);

    /**
     * Used to determine which sides can output energy (if any).
     *
     * @param direction Direction to the output.
     * @return Returns true if the given direction is output side.
     */
    boolean canOutput(Direction direction);

    default long availableAmpsOutput() {
        if (!canOutput()) return 0;
        if (getOutputVoltage() == 0) return 0;
        long out = Math.min(getOutputAmperage(), (getEnergy() / getOutputVoltage()));
        if (out == -1) out = getOutputAmperage();
        out = Math.min(out, getState().extract(true, out));
        return out;
    }

    default long availableAmpsInput(long voltage) {
        if (!canInput()) return 0;
        if (getInputVoltage() == 0) return 0;
        if (getState().getAmpsReceived() >= getInputAmperage()) return 0;
        long availableEnergy = getCapacity() - getEnergy();
        long availableInputAmps = availableEnergy / getInputVoltage();
        long out = Math.min(getInputAmperage(), availableInputAmps);
        if (out == -1) out = getInputAmperage();
        out = Math.min(out, getState().receive(true, out));
        return out;
    }

    /**
     * Returns the inner state for this node, representing received/sent eu.
     *
     * @return state.
     */
    GTConsumer.State getState();

    //Called by consumers that cannot tick themselves, such as FE wrappers.
    default void tesseractTick() {

    }

    GraphWrapper.ICapabilityGetter<IGTNode> GT_GETTER = TesseractPlatformUtils::getGTNode;
}
