package zap.graph;

import net.minecraft.util.EnumFacing;

public class Connectivity {
	public static byte with(byte connectivity, EnumFacing side) {
		return (byte)(connectivity | (1 << side.ordinal()));
	}
	
	public static byte of(IConnectable connectable) {
		byte connectivity = 0;
		
		for(EnumFacing facing: EnumFacing.VALUES) {
			if(connectable.connects(facing)) {
				connectivity = Connectivity.with(connectivity, facing);
			}
		}
		
		return connectivity;
	}
	
	public static boolean has(byte connectivity, EnumFacing side) {
		return (connectivity & (1 << side.ordinal())) > 0;
	}

	public static class Cache<C extends IConnectable> {
		byte connectivity;
		C value;

		private Cache() {}

		public static <C extends IConnectable> Cache<C> of(C value) {
			Cache<C> cache = new Cache<>();

			cache.value = value;
			cache.connectivity = Connectivity.of(value);

			return cache;
		}

		public boolean connects(EnumFacing facing) {
			return Connectivity.has(connectivity, facing);
		}

		public C value() {
			return value;
		}
	}
}
