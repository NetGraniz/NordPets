# NordPets

Small Paper 26.2 plugin for Nord Fjell. It prevents players from damaging tameable
animals owned by somebody else.

Protected attack sources:

- direct player attacks and sweeping attacks;
- arrows, tridents, thrown potions and other player projectiles;
- TNT ignited by a player;
- lingering area-effect clouds;
- evoker fangs attributed to a player;
- attacks by another player's tamed animal.

The owner may still damage their own pet. Damage from wild mobs, the environment,
fire, lava, drowning, falling and other unattributed sources is unchanged.

NordPets has no database, metrics, update checker, network calls, PacketEvents,
ProtocolLib, Vault or other external dependencies.

## Permissions

- `nordpets.bypass` — damage other players' pets; disabled for everyone by default.
- `nordpets.admin` — `/nordpets reload`; operators only by default.

## Build

Run `build.ps1`. It compiles against the Paper API already installed in
`Z:\Minecraft server` and creates `build\NordPets-1.0.0.jar`.

