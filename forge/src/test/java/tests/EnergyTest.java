package tests;


import org.junit.Test;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import net.minecraft.core.Direction;
import tesseract.api.gt.GTConsumer.State;
import tesseract.api.gt.GTTransaction.TransferData;
import tesseract.controller.Energy;
import tesseract.util.Pos;
import tesseract.api.GraphWrapper;
import tesseract.api.GraphWrapper.ICapabilityGetter;
import tests.GraphTest.TestGraph;
import tesseract.Tesseract;
import tesseract.api.gt.GTHolder;
import tesseract.api.gt.IGTCable;
import tesseract.api.gt.IGTNode;
import tesseract.api.gt.GTTransaction;

public class EnergyTest {

    private Function<Long2BooleanMap, ICapabilityGetter<IGTNode>> GETTER = a -> (lev, pos, side, cb) -> {
        if (a.get(pos))
            return new TestEnergyNode(32, 1);
        return null;
    };

    private TestEnergyConnctor defaultConnector() {
        return new TestEnergyConnctor(1, 1, 32);
    }

    private void setup() {
        Tesseract.TEST = true;
    }

    @Test
    public void testAddAndRemoveEnergyNode() {
        setup();
        Long2BooleanMap map = new Long2BooleanOpenHashMap();
        map.put(1, true);
        GraphWrapper<GTTransaction, IGTCable, IGTNode> GT_ENERGY = new GraphWrapper<>(Energy::new, GETTER.apply(map));
        GT_ENERGY.registerConnector(null, 0, defaultConnector(), true);
        assertEquals(GT_ENERGY.getGraph(null).size(), 2);
        map.remove(1);
        GT_ENERGY.remove(null, 0);
        GT_ENERGY.registerConnector(null, 0, defaultConnector(), true);
        assertEquals(GT_ENERGY.getGraph(null).size(), 1);
    }

    @Test
    public void testSendEnergy() {
        setup();
        Long2ObjectMap<IGTNode> map = new Long2ObjectOpenHashMap<>();
        map.put(Pos.packAll(-1, 0, 0), new TestEnergyNode(32, 1));
        map.put(Pos.packAll(1, 0, 0), new TestEnergyNode(32, 1, 32));
        GraphWrapper<GTTransaction, IGTCable, IGTNode> GT_ENERGY = new GraphWrapper<>(Energy::new, (a,b,c,d) -> map.get(b));
        GT_ENERGY.registerConnector(null, 0, defaultConnector(), true);
        assertEquals(GT_ENERGY.getGraph(null).size(), 3);
        var transaction = map.get(Pos.packAll(1, 0, 0)).extract(GTTransaction.Mode.TRANSMIT);
        GT_ENERGY.getController(null, 0).insert(0, Pos.subToDir(0, Pos.packAll(1, 0, 0)), transaction, null);
        transaction.commit();
        assertEquals(map.get(Pos.packAll(-1, 0, 0)).getEnergy(), 0);
        assertEquals(map.get(Pos.packAll(1, 0, 0)).getEnergy(), 31);
    }

    public class TestEnergyConnctor extends TestGraph.TestConnector implements IGTCable  {

        public final int loss;
        public final int amps;
        public final int voltage;

        private long holder;

        public TestEnergyConnctor(int loss, int amps, int voltage) {
            this.loss = loss;
            this.amps = amps;
            this.voltage = voltage;
            this.holder = GTHolder.create(this, amps);
        }
        
        @Override
        public int getLoss() {
            return loss;
        }

        @Override
        public int getAmps() {
            return amps;
        }

        @Override
        public int getVoltage() {
            return voltage;
        }

        @Override
        public boolean insulated() {
            return false;
        }

        @Override
        public long getHolder() {
            return holder;
        }

        @Override
        public void setHolder(long holder) {
            this.holder = holder;
        }
    }

    public class TestEnergyNode implements IGTNode {
        
        public final long voltage;
        public final long amps;

        public long energy;

        public final State state = new State(this);

        public TestEnergyNode(long voltage, long amps) {
            this.voltage = voltage;
            this.amps = amps;
        }

        public TestEnergyNode(long voltage, long amps, long energy) {
            this(voltage, amps);
            this.energy = energy;
        }

        @Override
        public boolean extractEnergy(TransferData data) {
            this.energy -= data.extractForNode(this);
            return true;
        }

        @Override
        public boolean addEnergy(TransferData data) {
            this.energy += data.consumeForNode(this);
            return true;
        }

        @Override
        public long getEnergy() {
            return this.energy;
        }

        @Override
        public long getCapacity() {
            return Long.MAX_VALUE;
        }

        @Override
        public long getOutputAmperage() {
            return amps;
        }

        @Override
        public long getOutputVoltage() {
            return voltage;
        }

        @Override
        public long getInputAmperage() {
            return amps;
        }

        @Override
        public long getInputVoltage() {
            return voltage;
        }

        @Override
        public boolean canOutput() {
            return true;
        }

        @Override
        public boolean canInput() {
            return true;
        }

        @Override
        public boolean canInput(Direction direction) {
            return true;
        }

        @Override
        public boolean canOutput(Direction direction) {
            return true;
        }

        @Override
        public State getState() {
            return state;
        }

    }
}
