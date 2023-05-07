package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import earth.terrarium.botarium.api.fluid.FluidHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidPlatformUtils {
    @ExpectPlatform
    public static FluidHolder createFluidStack(Fluid fluid, long amount){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceLocation getStillTexture(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceLocation getFlowingTexture(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceLocation getFluidId(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getFluidTemperature(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFluidGaseous(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getFluidColor(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SoundEvent getFluidSound(Fluid fluid, boolean fill){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Component getFluidDisplayName(FluidHolder fluid){
        throw new AssertionError();
    }

    /*@ExpectPlatform
    public static FluidStack tryFluidTransfer(IFluidHandler fluidDestination, IFluidHandler fluidSource, long maxAmount, boolean doTransfer){
        throw new AssertionError();
    }*/

    /**
     * Fill a container from the given fluidSource.
     *
     * @param container   The container to be filled. Will not be modified.
     *                    Separate handling must be done to reduce the stack size, stow containers, etc, on success.
     *                    See {@link #tryFillContainerAndStow(ItemStack, IFluidHandler, IItemHandler, int, Player, boolean)}.
     * @param fluidSource The fluid handler to be drained.
     * @param maxAmount   The largest amount of fluid that should be transferred.
     * @param player      The player to make the filling noise. Pass null for no noise.
     * @param doFill      true if the container should actually be filled, false if it should be simulated.
     * @return a {@link FluidActionResult} holding the filled container if successful.
     */
    /*@Nonnull
    public static FluidActionResult tryFillContainer(@Nonnull ItemStack container, IFluidHandler fluidSource, long maxAmount, @Nullable Player player, boolean doFill)
    {
        ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1); // do not modify the input
        return TesseractCapUtils.getFluidHandlerItem(containerCopy)
                .map(containerFluidHandler -> {
                    long realMaxAmount = maxAmount == -1 ? containerFluidHandler.getTankCapacityInDroplets(0) : maxAmount;
                    FluidStack simulatedTransfer = tryFluidTransfer(containerFluidHandler, fluidSource, realMaxAmount, false);
                    if (!simulatedTransfer.isEmpty())
                    {
                        if (doFill)
                        {
                            tryFluidTransfer(containerFluidHandler, fluidSource, realMaxAmount, true);
                            if (player != null)
                            {
                                SoundEvent soundevent = getFluidSound(simulatedTransfer.getFluid(), true);
                                player.level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                        }
                        else
                        {
                            containerFluidHandler.fillDroplets(simulatedTransfer, IFluidHandler.FluidAction.EXECUTE);
                        }

                        ItemStack resultContainer = containerFluidHandler.getContainer();
                        return new FluidActionResult(resultContainer);
                    }
                    return FluidActionResult.FAILURE;
                })
                .orElse(FluidActionResult.FAILURE);
    }*/

    /**
     * Takes a filled container and tries to empty it into the given tank.
     *
     * @param container        The filled container. Will not be modified.
     *                         Separate handling must be done to reduce the stack size, stow containers, etc, on success.
     *                         See {@link #tryEmptyContainerAndStow(ItemStack, IFluidHandler, IItemHandler, int, Player, boolean)}.
     * @param fluidDestination The fluid handler to be filled by the container.
     * @param maxAmount        The largest amount of fluid that should be transferred.
     * @param player           Player for making the bucket drained sound. Pass null for no noise.
     * @param doDrain          true if the container should actually be drained, false if it should be simulated.
     * @return a {@link FluidActionResult} holding the empty container if the fluid handler was filled.
     *         NOTE If the container is consumable, the empty container will be null on success.
     */
    /*@Nonnull
    public static FluidActionResult tryEmptyContainer(@Nonnull ItemStack container, IFluidHandler fluidDestination, long maxAmount, @Nullable Player player, boolean doDrain)
    {
        ItemStack containerCopy = ItemHandlerHelper.copyStackWithSize(container, 1); // do not modify the input
        return TesseractCapUtils.getFluidHandlerItem(containerCopy)
                .map(containerFluidHandler -> {
                    long realMaxAmount = maxAmount == -1 ? containerFluidHandler.getTankCapacityInDroplets(0) : maxAmount;
                    // We are acting on a COPY of the stack, so performing changes is acceptable even if we are simulating.
                    FluidStack transfer = tryFluidTransfer(fluidDestination, containerFluidHandler, realMaxAmount, doDrain);
                    if (transfer.isEmpty())
                        return FluidActionResult.FAILURE;

                    if (doDrain && player != null)
                    {
                        SoundEvent soundevent = getFluidSound(transfer.getFluid(), false);
                        player.level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                    } else {
                        containerFluidHandler.drain(transfer.getRealAmount(), IFluidHandler.FluidAction.EXECUTE);
                    }
                    ItemStack resultContainer = containerFluidHandler.getContainer();
                    return new FluidActionResult(resultContainer);
                })
                .orElse(FluidActionResult.FAILURE);
    }*/
}
