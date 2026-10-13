package de.dropall.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Liest Position und Größe des Inventarfensters (Felder sind in HandledScreen protected). */
@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
	@Accessor("x")
	int dropall$getX();

	@Accessor("y")
	int dropall$getY();

	@Accessor("backgroundWidth")
	int dropall$getBackgroundWidth();

	@Accessor("backgroundHeight")
	int dropall$getBackgroundHeight();
}
