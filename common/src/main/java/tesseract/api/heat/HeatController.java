package tesseract.api.heat;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.api.capability.ITransactionModifier;
import tesseract.graph.Cache;
import tesseract.graph.Graph;
import tesseract.graph.INode;
import tesseract.graph.NodeCache;
import tesseract.util.Pos;

import java.util.List;

public class HeatController extends Controller<HeatTransaction,IHeatPipe,IHeatNode> {

    private final Long2IntMap previousTemperature = new Long2IntOpenHashMap();
    private final Long2IntMap currentTemperature = new Long2IntOpenHashMap();
    private final Long2IntMap connectedCount = new Long2IntOpenHashMap();

    /**
     * Creates instance of the controller.
     *
     * @param supplier The world.
     * @param getter the getter for nodes.
     */
    public HeatController(Level supplier, Graph.INodeGetter<IHeatNode> getter) {
        super(supplier, getter);
        previousTemperature.defaultReturnValue(-1);
        currentTemperature.defaultReturnValue(-1);
        connectedCount.defaultReturnValue(1);
    }

    @Override
    protected void onFrame() {
        for (Long2ObjectMap.Entry<Cache<IHeatPipe>> entry : this.group.connectorsEntries()) {
            /*long pos = entry.getLongKey();
            IHeatPipe value = entry.getValue().value();
            final int counts = connectedCount.get(pos);
            value.update(true);
            if (counts == 0) {
                HeatTransaction transaction = value.extract();
                transaction.addData(transaction.available(), -1, a -> {});
                transaction.commit();
                continue;
            }*/
            /*Connectivity.connectivityFor(value, dir -> {
                IHeatHandler.HeatTransaction transaction = value.extract();
                if (!transaction.isValid()) return;
                transaction.limitHeat(value.temperatureCoefficient() / counts);
                transfer(pos, dir,transaction);
                transaction.addData(transaction.available(), -1, a -> {});
                transaction.commit();
            });*/

            //Transfer q to both directions.
        }
    }

    @Override
    public void change() {
        connectedCount.clear();
        for (Long2ObjectMap.Entry<Cache<IHeatPipe>> entry : this.group.connectorsEntries()) {
            long pos = entry.getLongKey();
            IHeatPipe value = entry.getValue().value();
            int count = 0;
            for (Direction dir : Direction.values()) {
                if (value.validate(dir)) count++;
            }
            connectedCount.put(pos, count);
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public ITickingController<HeatTransaction, IHeatPipe, IHeatNode> clone(INode group) {
        return new HeatController(this.dim, this.getter).set(group);
    }

    @Override
    public void getInfo(long pos, @NotNull List<String> list) {

    }

    @Override
    public void insert(long producerPos, Direction side, HeatTransaction transaction, ITransactionModifier modifier) {

    }
}
