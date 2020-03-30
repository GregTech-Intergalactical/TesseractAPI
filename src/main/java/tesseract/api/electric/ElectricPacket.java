package tesseract.api.electric;

/**
 * A class that acts as a container for an energy packet.
 */
public class ElectricPacket {

    private long send;
    private long used;
    private long amps;

    /**
     * Creates instance of the packet.
     *
     * @param send The amount of energy with loss.
     * @param used The full amount of energy.
     * @param amps The needed amperage.
     */
    protected ElectricPacket(double send, double used, double amps) {
        this.send = (long) send;
        this.used = (long) used;
        this.amps = (long) amps;
    }

    /**
     * @param amperage The current provider amperage.
     * @return Gets new amps for the provider.
     */
    public long update(long amperage) {
        long temp = amperage - amps;
        if (temp < 0) {
            amps = amperage;
            return 0;
        }
        return temp;
    }

    /**
     * @return Gets full amperage amount.
     */
    public long getAmps() {
        return amps;
    }

    /**
     * @return Gets energy amount to send.
     */
    public long getSend() {
        return send;
    }

    /**
     * @return Gets energy amount to used.
     */
    public long getUsed() {
        return used;
    }
}