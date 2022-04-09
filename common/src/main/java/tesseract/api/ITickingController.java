package tesseract.api;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import tesseract.api.capability.ITransactionModifier;
import tesseract.graph.INode;

import java.util.List;

/**
 * Interface abstracting ticking behaviour for the groups in the graph.
 */
public interface ITickingController<T, C extends IConnectable, N> {

    /**
     * Server tick handler.
     */
    void tick();

    /**
     * Notify the controller about group structure change.
     */
    void change();

    /**
     * Creates new controller for split group.
     *
     * @param group New group.
     * @return New controller for the group.
     */
    ITickingController<T, C, N> clone(INode group);

    /**
     * @return To get simple things like a some information.
     */
    void getInfo(long pos, @NotNull List<String> list);

    /**
     * Core method of Tesseract. Inserts an object into this pipe.
     * @param producerPos the position of the producer
     * @param side the side at which the object was inserted into the pipe.
     * @param transaction the transaction object.
     */
    void insert(long producerPos, Direction side, T transaction, ITransactionModifier modifier);

    /**
     * Returns the active world for this ticking controller.
     *
     * @return the world object.
     */
    Level getWorld();
}
