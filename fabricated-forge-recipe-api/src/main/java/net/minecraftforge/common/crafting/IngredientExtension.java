package net.minecraftforge.common.crafting;

import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.atomic.AtomicInteger;

public interface IngredientExtension {
    AtomicInteger INVALIDATION_COUNTER = new AtomicInteger();
    boolean isVanilla();

    boolean isSimple();

    void invalidate();

    void markValid();

    boolean checkInvalidation();

    net.minecraftforge.common.crafting.IIngredientSerializer<? extends Ingredient> getSerializer();

    static void invalidateAll() {
        INVALIDATION_COUNTER.incrementAndGet();
    }
}
