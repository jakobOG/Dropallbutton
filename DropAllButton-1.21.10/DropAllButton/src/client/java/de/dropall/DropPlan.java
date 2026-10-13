package de.dropall;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * Liste der Slot-IDs (ScreenHandler-Slots), die gedroppt werden sollen.
 * Es werden nur Slots des aktuell geoeffneten ScreenHandlers beruecksichtigt.
 */
public record DropPlan(DropMode mode, List<Integer> slotIds, int stacks, int items, int blocked) {

	public boolean isEmpty() {
		return slotIds.isEmpty();
	}

	/** Container-Typen, bei denen der Kisten-Modus angeboten wird. */
	public static boolean supportsContainer(ScreenHandler handler) {
		return handler instanceof GenericContainerScreenHandler // Kisten, Enderkisten, Fass
			|| handler instanceof ShulkerBoxScreenHandler
			|| handler instanceof HopperScreenHandler
			|| handler instanceof Generic3x3ContainerScreenHandler; // Werfer, Spender
	}

	public static DropPlan build(ClientPlayerEntity player, ScreenHandler handler, DropMode mode) {
		DropConfig cfg = DropConfig.get();
		boolean containerOk = mode.includesContainer() && supportsContainer(handler);
		List<Integer> ids = new ArrayList<>();
		int items = 0;
		int blocked = 0;

		for (Slot slot : handler.slots) {
			boolean isPlayerSlot = slot.inventory == player.getInventory();

			if (isPlayerSlot) {
				if (!mode.includesInventory()) {
					continue;
				}
				int idx = slot.getIndex();
				// 0-8 Hotbar, 9-35 Inventar, 36-39 Ruestung, 40 Offhand
				if (idx < 0 || idx > 40) {
					continue;
				}
				if (idx >= 36 && idx <= 39 && !cfg.includeArmor) {
					continue;
				}
				if (idx == 40 && !cfg.includeOffhand) {
					continue;
				}
			} else if (!containerOk) {
				// Crafting-Raster, Ergebnis-Slots usw. werden nie angefasst
				continue;
			}

			if (!slot.hasStack()) {
				continue;
			}
			if (!slot.canTakeItems(player)) {
				blocked++;
				continue;
			}
			ids.add(slot.id);
			items += slot.getStack().getCount();
		}
		return new DropPlan(mode, List.copyOf(ids), ids.size(), items, blocked);
	}
}
