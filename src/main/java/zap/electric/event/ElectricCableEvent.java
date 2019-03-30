package zap.electric.event;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import zap.electric.api.IElectricCable;

public class ElectricCableEvent extends Event {
	private BlockPos position;

	private ElectricCableEvent(BlockPos position) {
		this.position = position;
	}

	public BlockPos getPosition() {
		return position;
	}
	
	public static class Update extends ElectricCableEvent {
		private IElectricCable cable;
		private byte connections;
		
		public Update(BlockPos position, IElectricCable cable, byte connections) {
			super(position);

			this.cable = cable;
			this.connections = connections;
		}

		public IElectricCable getCable() {
			return cable;
		}
	}

	public static class Remove extends ElectricCableEvent {
		public Remove(BlockPos position) {
			super(position);
		}
	}
}
