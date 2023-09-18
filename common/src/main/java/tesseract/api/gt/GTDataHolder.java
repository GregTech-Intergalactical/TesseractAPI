package tesseract.api.gt;

import net.minecraft.util.Tuple;
import tesseract.api.DataHolder;

public class GTDataHolder extends DataHolder<Tuple<Long, Long>, Long> {
    public GTDataHolder(Tuple<Long, Long> immutableData, Long data) {
        super(immutableData, data);
    }
}
