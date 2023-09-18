package tesseract;

import tesseract.api.GraphWrapper;
import tesseract.api.fluid.FluidDataHolder;
import tesseract.api.fluid.FluidTransaction;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.api.gt.GTDataHolder;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IGTCable;
import tesseract.api.gt.IGTNode;
import tesseract.api.heat.HeatController;
import tesseract.api.heat.HeatTransaction;
import tesseract.api.heat.IHeatNode;
import tesseract.api.heat.IHeatPipe;
import tesseract.api.item.*;
import tesseract.api.rf.IRFCable;
import tesseract.api.rf.IRFNode;
import tesseract.api.rf.RFController;
import tesseract.api.rf.RFTransaction;
import tesseract.controller.Energy;
import tesseract.controller.Fluid;
import tesseract.graph.INode;

public class TesseractGraphWrappers {
    public static final GraphWrapper<HeatTransaction, IHeatPipe, IHeatNode> HEAT_CONTROLLER = new GraphWrapper<>(HeatController::new, IHeatNode.GETTER);
    public static final GraphWrapper<FluidDataHolder, IFluidPipe, IFluidNode> FLUID = new GraphWrapper<>(Fluid::new, IFluidNode.GETTER);
    public static final GraphWrapper<ItemDataHolder, IItemPipe, IItemNode> ITEM = new GraphWrapper<>(ItemController::new, IItemNode.GETTER);

    public static final GraphWrapper<RFTransaction, IRFCable, IRFNode> RF = new GraphWrapper<>(RFController::new, IRFNode.GETTER);
    public static long dropletMultiplier = TesseractPlatformUtils.isForge() ? 1L : 81L;
    //public static GraphWrapper<Integer, IRFCable, IRFNode> FE_ENERGY = new GraphWrapper<>(RFController::new);
    public static GraphWrapper<GTDataHolder, IGTCable, IGTNode> GT_ENERGY = new GraphWrapper<>(Energy::new, IGTNode.GT_GETTER);

    public static void init(){}
}
