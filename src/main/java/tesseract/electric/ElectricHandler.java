package tesseract.electric;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import tesseract.electric.api.*;
import tesseract.graph.*;

/**
 * Handler provides the functionality of a electricity with usage of graphs.
 * @apiNote default parameters are nonnull, methods return nonnull.
 */
public class ElectricHandler {

    private static long position;

    private static Graph<IElectricCable, IElectricNode> graph;
    private static final ObjectSet<Consumer> CONSUMERS = new ObjectLinkedOpenHashSet<>();
    
    /**
     *
     * @param dim
     * @param pos
     * @param producer
     */
    public ElectricHandler(int dim, long pos, IElectricNode producer) {
        position = pos;
        graph = ElectricNet.instance(dim);
        graph.addNode(position, Connectivity.Cache.of(producer, () -> {
            graph.findGroup(position).ifPresent(group -> {
                CONSUMERS.clear();

                for (Grid<IElectricCable> grid : group.findGrids(position)) {
                    for (Grid.Path<IElectricCable> path : grid.getPaths(position)) {
                        if(path.isEmpty()) continue;

                        graph.findAt(path.target().get()).asEndpoint().ifPresent(consumer -> {
                            if (consumer.canInput()) {
                                if (producer.getOutputVoltage() > consumer.getInputVoltage()) {
                                    // Explode
                                } else {
                                    CONSUMERS.add(new Consumer(consumer, path));
                                }
                            }
                        });
                    }
                }
            });
        }));
    }

    /**
     *
     * @param dim
     * @param pos
     * @param cable
     */
    public ElectricHandler(int dim, long pos, IElectricCable cable) {
        position = pos;
        graph = ElectricNet.instance(dim);
        graph.addConnector(pos, Connectivity.Cache.of(cable));
    }

    /**
     *
     */
    public void send(IElectricNode producer) {
        if (producer.canOutput()) {
            long amps = producer.getOutputAmperage();
            for (Consumer consumer : CONSUMERS) {
                if (amps <= 0) break;

                if (consumer.isValid()) {
                    Packet required = consumer.getEnergyRequired(producer.getOutputVoltage());
                    long amperage = required.get(amps);

                    producer.extract(required.used * required.amps, false);
                    consumer.node.insert(required.send * required.amps, false);

                    amps = amperage;
                }
            }
        }
    }

    /**
     * Removes instance from the graph.
     */
    public void remove() {
        graph.removeAt(position);
    }

    /**
     * The Consumer is a class that should wrap data.
     */
    private static class Consumer {

        long loss;
        IElectricNode node;
        ObjectList<IElectricCable> cables;

        /**
         * Creates instance of consumer.
         *
         * @param consumer The consumer node.
         * @param path The path to consumer data.
         */
        Consumer(IElectricNode consumer, Grid.Path<IElectricCable> path) {
            node = consumer;
            cables = path.getCross();
            for (IElectricCable cable : path.getFull()) {
                loss += cable.getLoss();
            }
        }

        /**
         * @param voltage The producer voltage.
         * @return Gets the packet required for consumer.
         */
        Packet getEnergyRequired(long voltage) {
            double required = node.getCapacity() - node.getPower();
            double energy = voltage - loss;
            double packs = Math.ceil(required / energy);
            double amperage = Math.min(packs, node.getInputAmperage());
            return new Packet(energy, voltage, amperage);
        }

        /**
         * @return Checks that consumer is able to receive energy.
         */
        boolean isValid() {
            return node.getPower() < node.getCapacity() && loss < node.getInputVoltage();
        }
    }

    /**
     * The Packet is a class that should wrap data.
     */
    private static class Packet {
        long send;
        long used;
        long amps;

        /**
         * Creates instance of packet.
         *
         * @param send The amount of energy with loss.
         * @param used The full amount of energy.
         * @param amps The needed amperage.
         */
        Packet(double send, double used, double amps) {
            this.send = (long) send;
            this.used = (long) used;
            this.amps = (long) amps;
        }

        /**
         * @param amperage The current provider amperage.
         * @return Gets new amps for the provider.
         */
        long get(long amperage) {
            long temp = amperage - amps;
            if (temp < 0) {
                amps = amperage;
                return 0;
            }
            return temp;
        }
    }
}
