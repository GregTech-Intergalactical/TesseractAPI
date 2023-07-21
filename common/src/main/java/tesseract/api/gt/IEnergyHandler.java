package tesseract.api.gt;


import tesseract.api.rf.IEnergyHandlerRF;

public interface IEnergyHandler extends IGTNode, IEnergyHandlerRF {
    @Override
    default IEnergyHandler getEnergyHandler(){
        return this;
    }
}
