package de.dropall;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class DropAllClient implements ClientModInitializer {
	public static final String MOD_ID = "dropall";

	public static KeyBinding dropKey;
	public static KeyBinding settingsKey;

	@Override
	public void onInitializeClient() {
		DropConfig.load();

		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "general"));
		dropKey = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.dropall.drop", GLFW.GLFW_KEY_UNKNOWN, category));
		settingsKey = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.dropall.settings", GLFW.GLFW_KEY_K, category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			DropExecutor.tick(client);
			while (settingsKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new DropConfigScreen(null));
				}
			}
		});

		ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
			if (!(screen instanceof HandledScreen<?> handled) || screen instanceof CreativeInventoryScreen) {
				return;
			}
			DropPanel panel = new DropPanel(client, handled);
			ScreenEvents.beforeRender(screen).register((s, ctx, mx, my, delta) -> panel.update());
			ScreenKeyboardEvents.allowKeyPress(screen).register((s, input) -> panel.handleKey(input));
		});
	}
}
