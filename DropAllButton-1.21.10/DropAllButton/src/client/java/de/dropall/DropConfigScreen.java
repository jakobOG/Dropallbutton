package de.dropall;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Einfaches Einstellungsmenue (ohne externe Bibliotheken). */
public final class DropConfigScreen extends Screen {
	private final Screen parent;

	public DropConfigScreen(Screen parent) {
		super(Text.translatable("dropall.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		DropConfig c = DropConfig.get();
		int cx = width / 2;
		int rowH = height >= 300 ? 24 : 21;
		int y = 28;

		toggle(cx, y, "dropall.config.confirm", () -> c.confirm, v -> c.confirm = v);
		y += rowH;
		toggle(cx, y, "dropall.config.armor", () -> c.includeArmor, v -> c.includeArmor = v);
		y += rowH;
		toggle(cx, y, "dropall.config.offhand", () -> c.includeOffhand, v -> c.includeOffhand = v);
		y += rowH;
		toggle(cx, y, "dropall.config.show", () -> c.showButton, v -> c.showButton = v);
		y += rowH;
		stepper(cx, y, "dropall.config.offsetx", -300, 300, 5, () -> c.offsetX, v -> c.offsetX = v);
		y += rowH;
		stepper(cx, y, "dropall.config.offsety", -300, 300, 5, () -> c.offsetY, v -> c.offsetY = v);
		y += rowH;
		stepper(cx, y, "dropall.config.width", 60, 160, 4, () -> c.buttonWidth, v -> c.buttonWidth = v);
		y += rowH;
		stepper(cx, y, "dropall.config.height", 16, 24, 2, () -> c.buttonHeight, v -> c.buttonHeight = v);
		y += rowH;
		stepper(cx, y, "dropall.config.speed", 1, 20, 1, () -> c.dropsPerTick, v -> c.dropsPerTick = v);
		y += rowH + 6;

		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> close())
			.dimensions(cx - 100, Math.min(y, height - 26), 200, 20).build());
	}

	private void toggle(int cx, int y, String key, BooleanSupplier get, Consumer<Boolean> set) {
		addDrawableChild(ButtonWidget.builder(toggleLabel(key, get.getAsBoolean()), b -> {
			set.accept(!get.getAsBoolean());
			DropConfig.save();
			b.setMessage(toggleLabel(key, get.getAsBoolean()));
		}).dimensions(cx - 100, y, 200, 20).build());
	}

	private static Text toggleLabel(String key, boolean on) {
		return Text.translatable(key).append(": ").append(Text.translatable(on ? "options.on" : "options.off"));
	}

	private void stepper(int cx, int y, String key, int min, int max, int step, IntSupplier get, IntConsumer set) {
		ButtonWidget value = ButtonWidget.builder(valueLabel(key, get.getAsInt()), b -> { })
			.dimensions(cx - 78, y, 156, 20).build();
		value.active = false;
		addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> {
			set.accept(Math.max(min, get.getAsInt() - step));
			DropConfig.save();
			value.setMessage(valueLabel(key, get.getAsInt()));
		}).dimensions(cx - 100, y, 20, 20).build());
		addDrawableChild(value);
		addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
			set.accept(Math.min(max, get.getAsInt() + step));
			DropConfig.save();
			value.setMessage(valueLabel(key, get.getAsInt()));
		}).dimensions(cx + 80, y, 20, 20).build());
	}

	private static Text valueLabel(String key, int v) {
		return Text.translatable(key).append(": " + v);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFFFF);
	}

	@Override
	public void close() {
		DropConfig.save();
		if (client != null) {
			client.setScreen(parent);
		}
	}
}
