package tesseract.electric;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import jdk.internal.jline.internal.Nullable;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.*;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.ArrayDeque;
import java.util.Iterator;

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

    public static Graph<IElectricCable, IElectricNode> instance(World world) {
        return ELECTRICITY.get(world.getDimension().getType().getId());
    }

    public static boolean addCable(World world, BlockPos pos, IElectricCable cable) {
        return !world.isRemote && instance(world).addConnector(pos.toLong(), Connectivity.Cache.of(cable));
    }

    public static boolean addNode(World world, BlockPos pos, IElectricNode node) {
        return !world.isRemote && instance(world).addNode(pos.toLong(), Connectivity.Cache.of(node));
    }

    public static boolean remove(World world, BlockPos pos) {
        return !world.isRemote && instance(world).remove(pos.toLong()) != null;
    }

    public static void requestEnergyUnit(World world, BlockPos pos, IElectricNode node) {
        //node.getInputAmperage();
        //node.getInputVoltage();
        //ObjectList<ObjectList<ArrayDeque<Node>>> path;
        long loss = 0L;
        long at = pos.toLong();
        Group<IElectricCable, IElectricNode> group = instance(world).findGroup(at);
        for (Grid<IElectricCable> grid : group.findGrids(at)) {
            for (ArrayDeque<Node> path : grid.getPath(at)) {
                Iterator<Node> iterator = path.descendingIterator();

                while(iterator.hasNext()) {
                    Node current = iterator.next();
                    IConnectable object = get(world, current);
                    if (object instanceof IElectricCable) {

                    }
                }
            }
        }
    }

    private static class PathData {
        Block dest;
        long loss;
        long amperage;
        ArrayDeque<Pos> path;
    }


    /*Group<IElectricCable, IElectricNode> group = instance(world).findGroup(at);
        if (group != null) {
        Connectivity.Cache<IElectricNode> node = group.getNodes().get(at);
        if (node != null) {
            for (Grid<IElectricCable> grid : group.findGrids(at)) {
                for (ArrayDeque<Node> path : grid.getPath(at)) {
                    Iterator<Node> iterator = path.descendingIterator();

                    while(iterator.hasNext()) {
                        Node current = iterator.next();
                        if (current.isCrossroad()) {
                            System.out.println(current);
                        }
                    }
                }
            }
        }
    }*/

    @Nullable
    public static IConnectable get(World world, Pos pos) {
        long at = pos.get();
        Group<IElectricCable, IElectricNode> group = instance(world).findGroup(at);
        if (group != null) {
            Connectivity.Cache<IElectricNode> node = group.getNodes().get(at);
            if (node != null) {
                return node.value();
            } else {
                for (Grid<IElectricCable> grid : group.getGrids().values()) {
                    Connectivity.Cache<IElectricCable> cable = grid.getConnectors().get(at);
                    if (cable != null) {
                        return cable.value();
                    }
                }
            }
        }

        return null;
    }
}
