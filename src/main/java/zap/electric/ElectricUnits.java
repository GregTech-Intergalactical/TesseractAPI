package zap.electric;

public final class ElectricUnits {
	public static final long MILLI_EU = 1;
	public static final long EU = MILLI_EU * 1000;
	public static final long KILO_EU = EU * 1000;
	public static final long MEGA_EU = KILO_EU * 1000;
	public static final long GIGA_EU = MEGA_EU * 1000;
	public static final long TERA_EU = GIGA_EU * 1000;
	public static final long PETA_EU = TERA_EU * 1000;

	public static long fromParts(long whole, long milli) {
		return whole * 1000 + milli;
	}

	/**
	 * Removes the decimal (thousandths) part of the given energy.
	 * @param energy The energy quantity to be floored
	 * @return The highest energy quantity less than or equal the input that is divisible by one EU.
	 */
	public static long floor(long energy) {
		return energy - energy % EU;
	}
}
