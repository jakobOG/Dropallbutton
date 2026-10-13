# Drop All Button (Fabric-Mod)

Ein Button oben links im Inventar und in Kisten, mit dem du dein Inventar und/oder den Kisteninhalt
mit einem Klick droppst. Die Items werden **regulär über den Server** gedroppt (derselbe Vorgang wie
Strg + Q über einem Slot), es werden also keine Serverregeln umgangen.

> **Ehrlicher Teststatus:** Dieses Projekt wurde ohne Minecraft-Installation erstellt. Es wurde
> **weder kompiliert noch im Spiel getestet**. Alle verwendeten Klassen- und Methodennamen wurden gegen die
> offiziellen Yarn-Mappings 1.21.10 und den Quellcode der Fabric API (Branch 1.21.10) geprüft, aber ob
> der Build bei dir durchläuft, sehe ich erst, wenn du ihn ausführst. Eine fertige JAR liegt deshalb **nicht** bei.
> Bitte schick mir eine eventuelle Fehlermeldung, ich korrigiere sie dann.

## Unterstützte Minecraft-Versionen

| Version | Status |
|---|---|
| **1.21.10** | Ziel dieses Projekts (Fabric Loader ≥ 0.19.5, Fabric API 0.138.4+1.21.10) – ungetestet |
| andere (1.21 – 1.21.9, 1.21.11, 26.x) | **nicht enthalten.** Mappings und APIs unterscheiden sich, dafür wären getrennte Builds nötig |

## Installation (Windows)

1. **Java 21 (JDK)** installieren: https://adoptium.net/ (beim Installieren „JAVA_HOME setzen“ aktivieren).
2. ZIP entpacken, **`build.bat` doppelklicken**. Beim ersten Mal werden Minecraft und Bibliotheken
   heruntergeladen (Internet nötig, dauert einige Minuten).
3. Danach öffnet sich der Ordner **`fertig`** mit der Datei `dropall-1.0.0.jar`.
4. Kopiere die JAR in deinen Mods-Ordner: **`%APPDATA%\.minecraft\mods`**
   (Windows-Taste + R, Pfad einfügen, Enter).
5. In diesem Ordner müssen außerdem liegen: **Fabric API 0.138.4+1.21.10** (oder neuer für 1.21.10).
   Starte Minecraft 1.21.10 mit dem Fabric-Loader-Profil.

## Bedienung

* **Spielerinventar (E):** Button „Inventar droppen“ über dem Fenster.
* **Kisten, Enderkisten, Fässer, Shulkerkisten, Trichter, Werfer/Spender:** zusätzlich drei kleine
  Buttons **Inventar / Kiste / Beides**. Die Auswahl wird gespeichert; der Hauptbutton zeigt die gewählte Aktion.
* **Bestätigung:** Nach dem Klick wird der Hauptbutton rot („Ja: N Stacks droppen“). Nochmal klicken
  bestätigt, **X** oder Fenster schließen bricht ab. Ändert sich das Inventar dazwischen, wird neu gefragt.
* **Rüstung und Offhand** bleiben geschützt, solange du sie nicht in den Einstellungen einschließt.
  Die Hotbar ist immer dabei. Crafting-Felder werden nie angefasst.
* **Zahnrad (⚙)** am Button oder Taste **K** öffnet die Einstellungen.
* Hotkey für die Drop-Aktion: unter *Optionen → Steuerung → Drop All Button* belegen (standardmäßig frei).

## Einstellungen (gespeichert in `config/dropall.json`)

Bestätigung EIN/AUS, Rüstung einschließen, Offhand einschließen, Button anzeigen, Verschiebung X/Y,
Button-Breite/-Höhe, Drops pro Tick (Standard 4; niedriger = sanfter für den Server).

## Sicherheit / Verhalten

* Es wird nur der **aktuell geöffnete** Container und dein eigenes Inventar verwendet.
* Gedroppt wird per Container-Klick (`THROW`), der Server entscheidet. Lehnt er ab, stellt er den
  Slot wieder her; die Mod meldet dann „X Slots konnten nicht gedroppt werden“.
* Wird das Fenster während des Droppens geschlossen, bricht die Mod ab und meldet, wie viele Stacks schon gedroppt wurden.
* Hältst du ein Item am Mauszeiger, startet nichts.
* **Hinweis:** Manche Server/Anti-Cheat-Plugins werten viele schnelle Drops als Spam. Dann „Drops pro Tick“ senken.

## Fehlerbehebung

* *„Java wurde nicht gefunden / falsche Version“*: JDK 21 installieren, Eingabeaufforderung neu öffnen.
* *Build bricht beim Download ab*: Internet/Firewall prüfen, `build.bat` erneut starten.
* *Button erscheint nicht*: Einstellung „Button anzeigen“ prüfen; im Kreativinventar gibt es den Button absichtlich nicht.

## Projektstruktur

`src/client/java/de/dropall/` Code, `src/main/resources` Mod-Metadaten und Übersetzungen (de/en),
`build.bat` Windows-Build, `gradlew(.bat)` Gradle-Wrapper (Gradle 9.7.1 laut Fabric-Vorlage).
