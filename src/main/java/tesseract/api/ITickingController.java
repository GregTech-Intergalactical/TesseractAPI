package tesseract.api;

import net.minecraft.util.Direction;
import net.minecraft.world.World;
import tesseract.graph.INode;
import tesseract.util.Pos;

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
     * @param group New group.
     * @return New controller for the group.
     */
    ITickingController<T, C, N> clone(INode group);

    /**
     * @return To get simple things like a some information.
     */
    String[] getInfo(long pos);

    /**
     * Core method of tesseract. Inserts an object into this pipe.
     * @param producerPos position of producer (pipe).
     * @param direction direction inserted into pipe.
     * @param stack the object inserted.
     * @param simulate to simulate insertion.
     * @return controller-sensitive insertion information(amount inserted).
     */
    int insert(Pos producerPos, Direction direction, T stack, boolean simulate);

    World getWorld();
}
