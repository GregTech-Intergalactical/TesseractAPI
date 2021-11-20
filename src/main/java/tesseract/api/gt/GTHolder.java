package tesseract.api.gt;

/**
 * A class that imitates a holder of the packets that has passed thought cables.
 */
public class GTHolder {

    /**
     * Creates long with the packed holder.
     *
     * @param cable    The cable connector.
     * @param amperage The initial amperage.
     */
    protected static long create(IGTCable cable, long amperage) {
        return (long) cable.getAmps() << 32 | amperage;
    }

    /**
     * Adds a new amperage.
     *
     * @param holder   The long with the packed holder.
     * @param amperage The added amperage.
     */
    protected static long add(long holder, long amperage) {
        return (long) getMaxAmperage(holder) << 32 | getAmperage(holder) + amperage;
    }

    /**
     * @param holder The long with the packed holder.
     * @return Gets a current amperage.
     */
    protected static int getAmperage(long holder) {
        return (int) (holder);
    }

    /**
     * @param holder The long with the packed holder.
     * @return Gets a maximum amperage.
     */
    protected static int getMaxAmperage(long holder) {
        return (int) (holder >> 32);
    }

    /**
     * @param holder The long with the packed holder.
     * @return Checks that the holder is not able to handle it.
     */
    protected static boolean isOverAmperage(long holder) {
        return getMaxAmperage(holder) < getAmperage(holder);
    }
}
