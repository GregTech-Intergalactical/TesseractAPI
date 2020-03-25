package tesseract.electric;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.*;
import tesseract.util.fast.ObjectLinkedStaticHashSet;

import java.util.Optional;

/**
 * Parent holder for electric system.
 */
public class ElectricSystem {
    public static final Int2ObjectMap<Graph<IElectricCable, IElectricNode>> ELECTRICITY = new Int2ObjectOpenHashMap<>();

    public ElectricSystem() {
        for (DimensionType dim : DimensionType.getAll()) {
            ELECTRICITY.put(dim.getId(), new Graph<>());
        }
    }

    private static Graph<IElectricCable, IElectricNode> instance(World world) {
        return ELECTRICITY.get(world.getDimension().getType().getId());
    }

    public static void register(World world, BlockPos pos, Object o) {
        if (!world.isRemote) {
            if (o instanceof IElectricCable) instance(world).addConnector(pos.toLong(), Connectivity.Cache.of(o));
            else if (o instanceof IElectricNode) instance(world).addNode(pos.toLong(), Connectivity.Cache.of(o));
        }
    }

    public static boolean unregister(World world, BlockPos pos) {
        return !world.isRemote && instance(world).removeAt(pos.toLong()) != null;
    }

    /**
     * @see ObjectLinkedStaticHashSet
     */
    public static void requestEnergyUnit(World world, BlockPos pos, ObjectLinkedStaticHashSet<Package> senders) {
        long at = pos.toLong();
        Graph<IElectricCable, IElectricNode> graph = instance(world);
        Optional<Group<IElectricCable, IElectricNode>> entry = graph.findGroup(at);
        if (!entry.isPresent()) return; // What if node wasn't initialized or removed at same time ?
        Group<IElectricCable, IElectricNode> group = entry.get();

        // Calculate loss, amp, paths

        // Build paths hash for the updating information about grids around
        int hash = 0;
        for (Grid<IElectricCable> grid : group.findGrids(at)) {
            for (Grid.Path<IElectricCable> path : grid.getPath(at)) {
                hash += path.hashCode();
            }
        }

        //
        Optional<Integer> prev = senders.getHash();
        if (!prev.isPresent() || hash != prev.get()) {
            hash = 0;
            senders.clear();

            for (Grid<IElectricCable> grid : group.findGrids(at)) {
                for (Grid.Path<IElectricCable> path : grid.getPath(at)) {
                    hash += path.hashCode();
                    Optional<IElectricNode> node = graph.findAt(path.target().get()).asEndpoint();
                    if (node.isPresent()) {
                        IElectricNode sender = node.get();
                        if (sender.canExtract()) {
                            senders.add(new Package(sender, path));
                        }
                    }
                }
            }

            senders.setHash(hash);
        }
    }

    /*public boolean isGridWasChanged(Group<IElectricCable, IElectricNode> group, long pos, ObjectSet<Package> senders) {

        for (Grid<IElectricCable> grid : group.findGrids(pos)) {
            ObjectSet<Grid.Path<IElectricCable>> paths = grid.getPath(pos);
            int code = paths.hashCode();
            if (code != senders.hashCode()) {
                //new ObjectLinkedStaticHashSet<>(code); /// Create obj with same hash
            }
        }



        return false;
    }*/

    public static class Package {

        private long loss;
        private IElectricNode sender;
        private ObjectList<IElectricCable> cables;

        public Package(IElectricNode sender, Grid.Path<IElectricCable> path) {
            this.sender = sender;
            for (IElectricCable cable : path.getFull()) {
                loss += cable.getLoss();
            }
            cables = path.getCross();
        }
    }
}
