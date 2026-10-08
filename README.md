# NordPets 1.1.0

Protects tamed animals from other players on Paper 26.2 and Folia 26.2. One JAR supports both platforms on Java 25.

## Protection

NordPets blocks damage from another player's direct or sweeping attack, arrows, tridents, potions and other projectiles. It also checks player-lit TNT, lingering potion clouds, player-attributed evoker fangs and attacks by another player's tamed animal.

Owners can damage their own pets. Wild mobs and environmental damage—fire, lava, drowning and falls—are unchanged. Damage without a player attribution is not blocked.

The plugin has no external runtime plugin dependency, database, telemetry, update checker or network requests.

## Permissions

| Permission | Allows | Default |
| --- | --- | --- |
| `nordpets.bypass` | Damage another player's protected pet | Nobody, including operators |
| `nordpets.admin` | `/nordpets reload` | Operators |

Grant bypass separately if a staff member needs it. The admin permission does not grant bypass.

## Configuration

Edit `plugins/NordPets/config.yml`, then run `/nordpets reload`.

## Build and installation

Use Maven 3.9+ and JDK 25. Run `./build.ps1` or `mvn clean verify`; the release JAR is `target/NordPets-1.1.0.jar`.

Stop the server before replacing the JAR. See [BUILDING.md](BUILDING.md) and [FOLIA.md](FOLIA.md). Historical server-library builds and their `build/NordPets-1.0.0.jar` output are not the current release build.

