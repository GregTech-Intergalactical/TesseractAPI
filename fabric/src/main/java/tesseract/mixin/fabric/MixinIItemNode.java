package tesseract.mixin.fabric;

import io.github.fabricators_of_create.porting_lib.transfer.item.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import tesseract.api.item.IItemNode;

@Mixin(IItemNode.class)
public abstract class MixinIItemNode implements IItemHandler {
}
