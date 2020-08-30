package tesseract.api.energy;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.graph.Cache;
import tesseract.graph.Grid;
import tesseract.graph.INode;
import tesseract.graph.Path;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.List;

/**
 * Class acts as a controller in the group of a energy components.
 */
public class EnergyController extends Controller<ITesseractCable, ITesseractNode> {

    private long totalEnergy, lastEnergy;
    private final Long2ObjectMap<EnergyHolder> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectMap<ITesseractNode, List<EnergyConsumer>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.

     * @param dim The dimension id.
     */
    public EnergyController(int dim) {
        super(dim);
    }

    /**
     * Executes when the group structure has changed.
     * <p>
     * First, it clears previous controller map, after it lookup for the position of node and looks for the around grids.
     * Second, it collects all producers and collectors for the grid and stores it into data map.
     * Finally, it will pre-build consumer objects which are available for the producers. So each producer has a list of possible
     * consumers with unique information about paths, loss, ect.
     * </p>
     * @see tesseract.graph.Grid (Cache)
     */
    @Override
    public void change() {
        data.clear();

        for (Long2ObjectMap.Entry<Cache<ITesseractNode>> e : group.getNodes().long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            ITesseractNode producer = e.getValue().value();

            if (producer.canOutput()) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (producer.canOutput(direction)) {
                        List<EnergyConsumer> consumers = new ObjectArrayList<>();
                        long side = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(side)) {
                            onCheck(consumers, null, side);
                        } else {
                            Grid<ITesseractCable> grid = group.getGridAt(side, direction);
                            if (grid != null) {
                                for (Path<ITesseractCable> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        Node target = path.target();
                                        assert target != null;
                                        onCheck(consumers, path, target.asLong());
                                    }
                                }
                            }
                        }

                        if (!consumers.isEmpty()) {
                            if (data.containsKey(producer)) {
                                onMerge(producer, consumers);
                            } else {
                                data.put(producer, consumers);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Merge the existing consumers with new ones.
     *
     * @param producer The producer node.
     * @param consumers The consumer nodes.
     */
    private void onMerge(ITesseractNode producer, List<EnergyConsumer> consumers) {
        List<EnergyConsumer> existingConsumers = data.get(producer);
        for (EnergyConsumer c : consumers) {
            boolean found = false;
            for (EnergyConsumer ec : existingConsumers) {
                if (ec.getNode() == c.getNode()) {
                    found = true;
                }
                if (!found) existingConsumers.add(c);
            }
        }
    }

    /**
     * Adds available consumers to the list.
     *
     * @param consumers The consumer nodes.
     * @param path The paths to consumers.
     * @param pos The position of the producer.
     */
    private void onCheck(List<EnergyConsumer> consumers, Path<ITesseractCable> path, long pos) {
        ITesseractNode node = group.getNodes().get(pos).value();
        if (node.canInput()) consumers.add(new EnergyConsumer(node, path));
    }

    /**
     * Call on the updates to send energy.
     * <p>
     * Most of the magic going in producer class which acts as wrapper double it around controller map.
     * Firstly, method will look for the available producer and consumer.
     * Secondly, some amperage calculation is going using the consumer and producer data.
     * Thirdly, it will check the voltage and amperage for the single impulse by the lowest cost cable.
     * </p>
     * If that function will find corrupted cables, it will execute loop to find the corrupted cables and exit.
     * However, if corrupted cables wasn't found, it will looks for variate connection type and store the amp for that path.
     * After energy was send, loop will check the amp holder instances on ampers map to find cross-nodes where amps/voltage is exceed max limit.
     */
    @Override
    public void tick() {
        super.tick();
        holders.clear();

        for (Object2ObjectMap.Entry<ITesseractNode, List<EnergyConsumer>> e : data.object2ObjectEntrySet()) {
            ITesseractNode producer = e.getKey();

            long outputEnergy = Math.min(producer.getEnergy(), producer.getOutputEnergy());
            if (outputEnergy <= 0L) {
                continue;
            }

            for (EnergyConsumer consumer : e.getValue()) {

                long extracted = producer.extract(outputEnergy, true);
                if (extracted <= 0L) {
                    break;
                }

                long inserted = consumer.insert(extracted, true);
                if (inserted <= 0L) {
                    continue;
                }

                // Stores the energy into holder for path only for variate connection
                switch (consumer.getConnection()) {
                    case SINGLE:
                        long min = consumer.getMinCapacity(); // Fast check by the lowest cost cable
                        if (min < inserted) {
                            inserted = min;
                        }
                        break;

                    case VARIATE:
                        long limit = inserted;
                        for (Long2ObjectMap.Entry<ITesseractCable> p : consumer.getCross().long2ObjectEntrySet()) {
                            long pos = p.getLongKey();
                            ITesseractCable cable = p.getValue();

                            limit = Math.min(limit, holders.computeIfAbsent(pos, h -> new EnergyHolder(cable)).getCapacity());
                        }

                        if (limit > 0) {
                            for (long pos : consumer.getCross().keySet()) {
                                holders.get(pos).reduce(limit);
                            }
                        }

                        inserted = limit;
                        break;
                }

                if (inserted <= 0L) {
                    continue;
                }

                extracted = producer.extract(inserted, false);

                consumer.insert(extracted, false);

                totalEnergy += inserted;

                outputEnergy -= inserted;
                if (outputEnergy <= 0L) {
                    break;
                }
            }
        }
    }

    @Override
    protected void onFrame() {
        lastEnergy = totalEnergy;
        totalEnergy = 0L;
    }

    @Override
    public String[] getInfo() {
        return new String[]{
            "Total Energy: ".concat(Long.toString(lastEnergy))
        };
    }

    @Override
    public ITickingController clone(INode group) {
        return new EnergyController(dim).set(group);
    }
}