package tesseract.api;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tesseract.graph.Group;
import tesseract.graph.INode;
import tesseract.util.Dir;
import tesseract.util.Pos;

import java.util.function.Function;

/**
 * Class acts as a controller in the group of some components.
 */
abstract public class Controller<T, C extends IConnectable, N> implements ITickingController<T, C, N> {

    protected int tick;
    protected final RegistryKey<World> dim;
    protected Group<T, C, N> group;

    protected final Function<RegistryKey<World>, ServerWorld> WORLD_SUPPLIER;

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     */
    protected Controller(Function<RegistryKey<World>, ServerWorld> supplier, RegistryKey<World> dim) {
        this.dim = dim;
        this.WORLD_SUPPLIER = supplier;
    }

    /**
     * Sets the group for the controller.
     *
     * @param container The group this controller handles.
     */
    public Controller<T, C, N> set(INode container) {
        this.group = (Group<T, C, N>) container;
        return this;
    }

    /**
     * Executes on the tick updates.
     */
    @Override
    public void tick() {
        tick++;
        if (tick % 20 == 0) {
            onFrame();
        }
    }

    /**
     * Frame handler, which executes each second.
     */
    protected abstract void onFrame();

    protected ServerWorld getWorld() {
        return this.WORLD_SUPPLIER.apply(dim);
    }
}
