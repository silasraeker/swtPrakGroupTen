# Gameclient

Der Gameclient ist Teil des Amazonenspiels (SWTPRA SS26). Er nimmt Anfragen vom Frontend über HTTP entgegen und kommuniziert mit dem Gameserver. Eine integrierte KI ermöglicht das Spielen gegen den Computer.

## Tech Stack

- Java SE 21
- Maven
- Jackson (JSON)
- Log4j2 + SLF4J (Logging)
- JUnit 5 + Mockito (Tests)

## Projektstruktur

- `ai/` – KI-Strategien (AlphaBeta, MCTS, Random) und Simulator
- `client/` – HTTP-Server, Handler, Services und Spielmodell
- `src/test/` – Unit-Tests und Beispiel-JSONs

## Build & Run

Bauen:

```bash
mvn clean install
```

TUI starten:

```bash
mvn compile exec:java -Dexec.mainClass="client.tui.GameclientTUI"
```

Default-Adressen:

- Gameclient: `http://localhost:6070`
- Gameserver: `http://localhost:8080`

Tests:

```bash
mvn test
```

## Erstes Ausprobieren

1. Gameserver starten (siehe Gameserver-README), er sollte auf `http://localhost:8080` erreichbar sein.
2. TUI starten (siehe oben).
3. In der TUI `start` eingeben, um den Gameclient hochzufahren.
4. Mit `exit` wieder beenden.

## TUI-Befehle

| Befehl | Beschreibung |
|--------|--------------|
| `start` | Startet den Gameclient (optional mit eigenen Adressen) |
| `get <url>` | Schickt einen GET-Request |
| `post <url> <datei>` | Schickt einen POST-Request mit JSON-Datei |
| `alias <name> <wert>` | Setzt einen Alias |
| `exit` | Schließt die TUI |

Voreingestellte Aliase: `server`, `client`, `game_start_example`, `game_move_WHITE_1_example`.
