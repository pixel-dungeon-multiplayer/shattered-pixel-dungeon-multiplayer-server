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

Supported properties are `spd.port`, `spd.onlineMode`, `spd.serverName`,
`spd.serverUuid`, `spd.maxPlayers`, `spd.motd`, `spd.dataDir`, and
`spd.headless.ups`. The virtual layout resolution defaults to `720x400` and can
be changed with `spd.virtualWidth` and `spd.virtualHeight`. Persistent preferences are stored by libGDX under the
usual user preferences directory; game data defaults to `headless-data/`.
