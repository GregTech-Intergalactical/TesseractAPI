package tesseract.api;

import net.minecraft.world.World;
import tesseract.graph.INode;

import javax.annotation.Nonnull;
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
    void getInfo(long pos, @Nonnull List<String> list);

    /**
     * Core method of tesseract. Inserts an object into this pipe.
     *
     * @param producerPos position of node (can be pipe.)
     * @return controller-sensitive insertion information(amount inserted).
     */
    void insert(long producerPos, long pipePos, T transaction);

    /**
     * Returns the active world for this ticking controller.
     *
     * @return the world object.
     */
    World getWorld();

    /**
     * Creates a node object from a pipe.
     *
     * @param pipe
     * @return
     */
    N wrapPipe(final C pipe);
}
