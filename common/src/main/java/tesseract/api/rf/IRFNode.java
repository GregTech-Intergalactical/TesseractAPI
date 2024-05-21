package tesseract.api.rf;


import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.util.Updatable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.TesseractPlatformUtils;
import tesseract.api.GraphWrapper;

/**
 * A flux node is the unit of interaction with flux inventories.
 * <p>
 * Derived from the Redstone Flux power system designed by King Lemming and originally utilized in Thermal Expansion and related mods.
 * Created with consent and permission of King Lemming and Team CoFH. Released with permission under LGPL 2.1 when bundled with Forge.
 * </p>
 */
public interface IRFNode extends EnergyContainer, Updatable {

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

    @Override
    default void update() {

    }

    GraphWrapper.ICapabilityGetter<IRFNode> GETTER = TesseractPlatformUtils.INSTANCE::getRFNode;
}
