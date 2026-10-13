package de.dropall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Lokale Einstellungen, gespeichert in config/dropall.json. */
public final class DropConfig {
	private static final Logger LOG = LoggerFactory.getLogger("dropall");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static DropConfig instance = new DropConfig();

	public boolean confirm = true;
	public boolean includeArmor = false;
	public boolean includeOffhand = false;
	public boolean showButton = true;
	public int offsetX = 0;
	public int offsetY = 0;
	public int buttonWidth = 120;
	public int buttonHeight = 20;
	public int dropsPerTick = 4;
	public String lastMode = DropMode.INVENTORY.name();

	public static DropConfig get() {
		return instance;
	}

	public DropMode mode() {
		return DropMode.fromName(lastMode);
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("dropall.json");
	}

	private void clamp() {
		offsetX = Math.max(-300, Math.min(300, offsetX));
		offsetY = Math.max(-300, Math.min(300, offsetY));
		buttonWidth = Math.max(60, Math.min(160, buttonWidth));
		buttonHeight = Math.max(16, Math.min(24, buttonHeight));
		dropsPerTick = Math.max(1, Math.min(20, dropsPerTick));
		lastMode = DropMode.fromName(lastMode).name();
	}

	public static void load() {
		try {
			Path f = file();
			if (Files.exists(f)) {
				try (Reader r = Files.newBufferedReader(f)) {
					DropConfig c = GSON.fromJson(r, DropConfig.class);
					if (c != null) {
						instance = c;
					}
				}
			}
		} catch (Exception e) {
			LOG.warn("Konfiguration konnte nicht gelesen werden, nutze Standardwerte", e);
			instance = new DropConfig();
		}
		instance.clamp();
		save();
	}

	public static void save() {
		try {
			instance.clamp();
			Path f = file();
			Files.createDirectories(f.getParent());
			try (Writer w = Files.newBufferedWriter(f)) {
				GSON.toJson(instance, w);
			}
		} catch (Exception e) {
			LOG.warn("Konfiguration konnte nicht gespeichert werden", e);
		}
	}
}
