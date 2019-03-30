package zap.electric.event;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import zap.electric.api.IElectricNode;

public class ElectricNodeEvent extends Event {
	private BlockPos position;

	private ElectricNodeEvent(BlockPos position) {
		this.position = position;
	}

	public BlockPos getPosition() {
		return position;
	}

	public static class Update extends ElectricNodeEvent {
		private IElectricNode node;

		public Update(BlockPos position, IElectricNode node) {
			super(position);

			this.node = node;
		}

		public IElectricNode getNode() {
			return node;
		}
	}

	public static class Remove extends ElectricNodeEvent {
		public Remove(BlockPos position) {
			super(position);
		}
	}
}
