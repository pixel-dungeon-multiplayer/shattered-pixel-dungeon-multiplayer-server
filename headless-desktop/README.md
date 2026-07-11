# Headless desktop server (experimental)

This module starts the complete game server with libGDX's headless backend. It
does not create an LWJGL window or require a display server.

Run with the saved server settings:

```powershell
.\gradlew.bat :headless-desktop:run
```

For an isolated test configuration, pass JVM properties through
`JAVA_TOOL_OPTIONS`:

```powershell
$env:JAVA_TOOL_OPTIONS = '-Dspd.port=7777 -Dspd.onlineMode=false -Dspd.serverName=HeadlessTest -Dspd.maxPlayers=4'
.\gradlew.bat :headless-desktop:run
```

The server stores its runtime state under `headless-data/` in the current
working directory. Its editable configuration is `headless-data/config.json`.
Use `spd.dataDir` to relocate all server data or `spd.configFile` to override
only the configuration path.

Supported properties are `spd.port`, `spd.onlineMode`, `spd.serverName`,
`spd.serverUuid`, `spd.maxPlayers`, `spd.motd`, `spd.dataDir`, and
`spd.headless.ups`. The virtual layout resolution defaults to `720x400` and can
be changed with `spd.virtualWidth` and `spd.virtualHeight`. Game data defaults
to `headless-data/`.
