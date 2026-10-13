package de.dropall;

import de.dropall.mixin.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Buttons oben links am Inventarfenster (pro geoeffnetem Screen eine Instanz). */
final class DropPanel {
	private static final int GAP = 2;
	private static final int SIDE_W = 20;
	private static final int ROW2_H = 16;

	private final MinecraftClient client;
	private final HandledScreen<?> screen;
	private final HandledScreenAccessor accessor;
	private final ScreenHandler handler;
	private final boolean container;
	private final boolean shown;

	private final ButtonWidget main;
	private final ButtonWidget gear;
	private final ButtonWidget cancel;
	private final ButtonWidget[] modeButtons = new ButtonWidget[3];

	private boolean confirming;
	private DropPlan pending;

	DropPanel(MinecraftClient client, HandledScreen<?> screen) {
		this.client = client;
		this.screen = screen;
		this.accessor = (HandledScreenAccessor) screen;
		this.handler = screen.getScreenHandler();
		this.container = DropPlan.supportsContainer(handler);
		DropConfig cfg = DropConfig.get();
		this.shown = cfg.showButton;

		int w = cfg.buttonWidth;
		int h = cfg.buttonHeight;
		int rowW = w + GAP + SIDE_W;

		main = ButtonWidget.builder(Text.empty(), b -> onMain()).dimensions(0, 0, w, h).build();
		gear = ButtonWidget.builder(Text.literal("\u2699"), b -> openSettings())
			.dimensions(0, 0, SIDE_W, h)
			.tooltip(Tooltip.of(Text.translatable("dropall.tooltip.settings")))
			.build();
		cancel = ButtonWidget.builder(Text.literal("X").formatted(Formatting.RED), b -> cancelConfirm())
			.dimensions(0, 0, SIDE_W, h)
			.tooltip(Tooltip.of(Text.translatable("dropall.tooltip.cancel")))
			.build();

		int w0 = rowW * 2 / 5;
		int w1 = rowW * 3 / 10;
		int[] widths = {w0, w1, rowW - w0 - w1};
		DropMode[] modes = DropMode.values();
		for (int i = 0; i < 3; i++) {
			final DropMode m = modes[i];
			modeButtons[i] = ButtonWidget.builder(Text.translatable(m.shortKey()), b -> selectMode(m))
				.dimensions(0, 0, widths[i], ROW2_H)
				.tooltip(Tooltip.of(Text.translatable(m.tipKey())))
				.build();
		}

		if (shown) {
			Screens.getButtons(screen).add(main);
			Screens.getButtons(screen).add(gear);
			Screens.getButtons(screen).add(cancel);
			if (container) {
				for (ButtonWidget b : modeButtons) {
					Screens.getButtons(screen).add(b);
				}
			}
		}
		update();
	}

	private DropMode currentMode() {
		return container ? DropConfig.get().mode() : DropMode.INVENTORY;
	}

	private void selectMode(DropMode m) {
		DropConfig.get().lastMode = m.name();
		DropConfig.save();
	}

	private void openSettings() {
		if (client.player != null) {
			client.player.closeHandledScreen();
		}
		client.setScreen(new DropConfigScreen(null));
	}

	private void cancelConfirm() {
		confirming = false;
		pending = null;
	}

	/** Wird vom Hauptbutton und vom Hotkey aufgerufen. */
	void onMain() {
		if (DropExecutor.busy() || client.player == null) {
			return;
		}
		DropMode mode = currentMode();
		DropPlan fresh = DropPlan.build(client.player, handler, mode);

		if (confirming) {
			if (pending == null || !fresh.slotIds().equals(pending.slotIds())) {
				// Inventar hat sich seit der Abfrage geaendert -> erneut bestaetigen
				pending = fresh.isEmpty() ? null : fresh;
				confirming = pending != null;
				return;
			}
			confirming = false;
			pending = null;
			DropExecutor.start(client, fresh, handler);
			return;
		}

		if (fresh.isEmpty()) {
			DropExecutor.msg(client, Text.translatable("dropall.msg.nothing").formatted(Formatting.YELLOW));
			return;
		}
		if (DropConfig.get().confirm) {
			pending = fresh;
			confirming = true;
		} else {
			DropExecutor.start(client, fresh, handler);
		}
	}

	boolean handleKey(KeyInput input) {
		if (DropAllClient.dropKey != null && !DropAllClient.dropKey.isUnbound()
			&& DropAllClient.dropKey.matchesKey(input)) {
			onMain();
			return false;
		}
		return true;
	}

	/** Position und Zustand der Buttons aktualisieren (jeden Frame). */
	void update() {
		if (!shown) {
			return;
		}
		DropConfig cfg = DropConfig.get();
		DropMode mode = currentMode();
		boolean busy = DropExecutor.busy();

		// Zustand
		if (busy) {
			main.setMessage(Text.translatable("dropall.button.busy"));
			main.active = false;
		} else if (confirming && pending != null) {
			main.setMessage(Text.translatable("dropall.button.confirm", pending.stacks(), pending.items())
				.formatted(Formatting.RED));
			main.active = true;
		} else {
			main.setMessage(Text.translatable(mode.buttonKey()));
			main.active = true;
		}
		gear.visible = !confirming;
		gear.active = !busy;
		cancel.visible = confirming;
		DropMode[] modes = DropMode.values();
		for (int i = 0; i < 3; i++) {
			modeButtons[i].visible = container;
			modeButtons[i].active = !confirming && !busy && modes[i] != mode;
		}

		// Position: bevorzugt ueber dem Fenster, linksbuendig
		int w = cfg.buttonWidth;
		int h = cfg.buttonHeight;
		int panelW = w + GAP + SIDE_W;
		int panelH = h + (container ? GAP + ROW2_H : 0);
		int gx = accessor.dropall$getX();
		int gy = accessor.dropall$getY();
		int gw = accessor.dropall$getBackgroundWidth();
		int sw = screen.width;
		int sh = screen.height;

		int x;
		int y;
		if (gy - panelH - GAP >= 0) {
			x = gx;
			y = gy - panelH - GAP;
		} else if (gx - panelW - GAP >= 0) {
			x = gx - panelW - GAP;
			y = gy;
		} else if (gx + gw + GAP + panelW <= sw) {
			x = gx + gw + GAP;
			y = gy;
		} else {
			x = 2;
			y = 2;
		}
		x += cfg.offsetX;
		y += cfg.offsetY;
		x = Math.max(0, Math.min(x, Math.max(0, sw - panelW)));
		y = Math.max(0, Math.min(y, Math.max(0, sh - panelH)));

		main.setX(x);
		main.setY(y);
		gear.setX(x + w + GAP);
		gear.setY(y);
		cancel.setX(x + w + GAP);
		cancel.setY(y);
		int mx = x;
		int my = y + h + GAP;
		for (ButtonWidget b : modeButtons) {
			b.setX(mx);
			b.setY(my);
			mx += b.getWidth();
		}
	}
}
