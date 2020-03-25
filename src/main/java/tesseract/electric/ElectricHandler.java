package tesseract.electric;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.*;
import tesseract.util.fast.ObjectLinkedStaticHashSet;

import java.util.Optional;

public class ElectricHandler {

    private long position;
    private IElectricNode node;
    private Graph<IElectricCable, IElectricNode> graph;
    private Group<IElectricCable, IElectricNode> group;
    private ObjectLinkedStaticHashSet<Package> consumers;

    public ElectricHandler(World world, BlockPos pos, IElectricNode ref) {
        if (world.isRemote) return;
        graph = ElectricNet.instance(world);
        position = pos.toLong();
        node = ref;
        graph.addNode(pos.toLong(), Connectivity.Cache.of(node, () -> {
            Optional<Group<IElectricCable, IElectricNode>> entry = graph.findGroup(position);
            if (entry.isPresent()) {
                group = entry.get();
                consumers = new ObjectLinkedStaticHashSet<>();

                for (Grid<IElectricCable> grid : group.findGrids(position)) {
                    for (Grid.Path<IElectricCable> path : grid.getPaths(position)) {
                        Optional<IElectricNode> node = graph.findAt(path.target().get()).asEndpoint();

                        if (node.isPresent()) {
                            IElectricNode consumer = node.get();
                            if (consumer.canReceive()) {
                                consumers.add(new Package(consumer, path));
                            }
                        }
                    }
                }
            }
        }));
    }

    public ElectricHandler(World world, BlockPos pos, IElectricCable ref) {
        if (world.isRemote) return;
        graph = ElectricNet.instance(world);
        position = pos.toLong();
        graph.addConnector(pos.toLong(), Connectivity.Cache.of(ref));
    }

    public void sendEnergyUnit() {
        if (node.canExtract()) System.out.println(consumers);
    }

    public void remove() {
        if (graph != null) graph.removeAt(position);
    }

    public void info(PlayerEntity player) {
        if (graph != null) {
            /*player.sendMessage(new StringTextComponent("Graph contains " + graph.countGroups() + " groups:"));
            for (Int2ObjectMap.Entry<Group<IElectricCable, IElectricNode>> group : graph.getGroups().int2ObjectEntrySet()) {
                player.sendMessage(new StringTextComponent("   Group " + group.getIntKey() + " contains " + group.getValue().countBlocks() + " blocks "));
                for (Grid<IElectricCable> grid : group.getValue().getGrids().values()) {
                    player.sendMessage(new StringTextComponent("      Grid contains " + grid.countConnectors() + " connectors"));
                }
            }*/

            player.sendMessage(new StringTextComponent("_____________________"));
        }
    }

    private static class Package {
        long loss;
        IElectricNode consumer;
        ObjectList<IElectricCable> cables;

        Package(IElectricNode consumer, Grid.Path<IElectricCable> path) {
            this.consumer = consumer;
            for (IElectricCable cable : path.getFull()) {
                loss += cable.getLoss();
            }
            cables = path.getCross();
        }
    }
}
