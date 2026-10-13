package de.dropall;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Fuehrt einen DropPlan ueber normale Container-Klicks aus (SlotActionType.THROW, Button 1 =
 * ganzer Stack, entspricht Strg+Q ueber dem Slot). Der Server entscheidet, ob gedroppt wird.
 * Die Klicks werden auf mehrere Ticks verteilt. Nach dem Ende wird geprueft, ob der Server
 * Slots abgelehnt hat (dann stellt er den Inhalt wieder her).
 */
public final class DropExecutor {
	private static final int VERIFY_TICKS = 20;

	private static final ArrayDeque<Integer> queue = new ArrayDeque<>();
	private static List<Integer> allSlots = List.of();
	private static int syncId = -1;
	private static int sent;
	private static boolean verifying;
	private static int waitTicks;

	private DropExecutor() {
	}

	public static boolean busy() {
		return syncId != -1;
	}

	public static void start(MinecraftClient mc, DropPlan plan, ScreenHandler handler) {
		ClientPlayerEntity player = mc.player;
		if (busy() || player == null || mc.interactionManager == null) {
			return;
		}
		if (handler != player.currentScreenHandler) {
			msg(mc, Text.translatable("dropall.msg.aborted", 0, plan.stacks()).formatted(Formatting.RED));
			return;
		}
		if (!handler.getCursorStack().isEmpty()) {
			msg(mc, Text.translatable("dropall.msg.cursor").formatted(Formatting.RED));
			return;
		}
		if (plan.isEmpty()) {
			msg(mc, Text.translatable("dropall.msg.nothing").formatted(Formatting.YELLOW));
			return;
		}
		queue.clear();
		queue.addAll(plan.slotIds());
		allSlots = plan.slotIds();
		syncId = handler.syncId;
		sent = 0;
		verifying = false;
		waitTicks = 0;
	}

	public static void tick(MinecraftClient mc) {
		if (!busy()) {
			return;
		}
		ClientPlayerEntity player = mc.player;
		if (player == null || mc.interactionManager == null) {
			reset();
			return;
		}
		ScreenHandler handler = player.currentScreenHandler;
		if (handler.syncId != syncId || !(mc.currentScreen instanceof HandledScreen<?>)) {
			msg(mc, Text.translatable("dropall.msg.aborted", sent, allSlots.size()).formatted(Formatting.YELLOW));
			reset();
			return;
		}

		if (!verifying) {
			int n = 0;
			int limit = DropConfig.get().dropsPerTick;
			while (n < limit && !queue.isEmpty()) {
				int id = queue.poll();
				if (id < 0 || id >= handler.slots.size()) {
					continue;
				}
				Slot slot = handler.getSlot(id);
				if (!slot.hasStack() || !slot.canTakeItems(player)) {
					continue;
				}
				if (!handler.getCursorStack().isEmpty()) {
					msg(mc, Text.translatable("dropall.msg.cursor").formatted(Formatting.RED));
					msg(mc, Text.translatable("dropall.msg.aborted", sent, allSlots.size()).formatted(Formatting.YELLOW));
					reset();
					return;
				}
				mc.interactionManager.clickSlot(syncId, id, 1, SlotActionType.THROW, player);
				sent++;
				n++;
			}
			if (queue.isEmpty()) {
				verifying = true;
				waitTicks = VERIFY_TICKS;
			}
		} else if (--waitTicks <= 0) {
			int remaining = 0;
			for (int id : allSlots) {
				if (id >= 0 && id < handler.slots.size() && handler.getSlot(id).hasStack()) {
					remaining++;
				}
			}
			if (remaining > 0) {
				msg(mc, Text.translatable("dropall.msg.partial", remaining).formatted(Formatting.YELLOW));
			} else {
				msg(mc, Text.translatable("dropall.msg.done", sent).formatted(Formatting.GREEN));
			}
			reset();
		}
	}

	private static void reset() {
		queue.clear();
		allSlots = List.of();
		syncId = -1;
		sent = 0;
		verifying = false;
		waitTicks = 0;
	}

	static void msg(MinecraftClient mc, Text text) {
		if (mc.player != null) {
			mc.player.sendMessage(Text.translatable("dropall.prefix").append(text), false);
		}
	}
}
