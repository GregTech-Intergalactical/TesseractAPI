package tesseract.electric;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.*;
import tesseract.util.fast.ObjectLinkedStaticHashSet;

import java.util.Optional;

public class ElectricHandler {

    //private IConnectable connectable;
    private Long position;
    private Graph<IElectricCable, IElectricNode> graph;
    private ObjectLinkedStaticHashSet<Package> consumers;

    public ElectricHandler(World world, BlockPos pos, IConnectable o) {
        if (world.isRemote) return;
        graph = ElectricNet.instance(world);
        IListener update = () -> {
            position = pos.toLong();
            Optional<Group<IElectricCable, IElectricNode>> entry = graph.findGroup(position);
            if (entry.isPresent()) {
                Group<IElectricCable, IElectricNode> group = entry.get();
                consumers = new ObjectLinkedStaticHashSet<>();

                for (Grid<IElectricCable> grid : group.findGrids(position)) {
                    for (Grid.Path<IElectricCable> path : grid.getPath(position)) {
                        Optional<IElectricNode> node = graph.findAt(path.target().get()).asEndpoint();

                        if (node.isPresent()) {
                            IElectricNode sender = node.get();
                            if (sender.canReceive()) {
                                consumers.add(new Package(sender, path));
                            }
                        }
                    }
                }
            }
        };
        if (o instanceof IElectricNode) graph.addNode(pos.toLong(), Connectivity.Cache.of((IElectricNode)o));
        else if (o instanceof IElectricCable) graph.addConnector(pos.toLong(), Connectivity.Cache.of((IElectricCable)o));
    }

    public void sendEnergyUnit() {
        if (consumers != null) System.out.println(consumers);
    }

    public void remove() {
        if (position != null) {
            graph.removeAt(position);
        }
    }

    private static class Package {

        long loss;
        IElectricNode sender;
        ObjectList<IElectricCable> cables;

        Package(IElectricNode sender, Grid.Path<IElectricCable> path) {
            this.sender = sender;
            for (IElectricCable cable : path.getFull()) {
                loss += cable.getLoss();
            }
            cables = path.getCross();
        }
    }
}
