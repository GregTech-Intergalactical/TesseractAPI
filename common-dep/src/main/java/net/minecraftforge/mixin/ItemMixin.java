package net.minecraftforge.mixin;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.capabilities.ICapabilityItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class ItemMixin implements ICapabilityItem {
}
