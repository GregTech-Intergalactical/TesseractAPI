package tesseract.mixin.forge;

import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import tesseract.api.item.IItemNode;

@Mixin(IItemNode.class)
public interface MixinIItemNode extends IItemHandler {
}
