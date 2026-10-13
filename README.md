# NITRO Client (Fabric, MC 1.21.4)

Build:  gradle wrapper && ./gradlew build   -> build/libs/nitro-client-1.0.0.jar
Dev:    ./gradlew runClient

Controls
- Right Shift: open/close ClickGUI (rebind in Controls)
- GUI: LMB toggle module, RMB open settings, MMB bind a key (Esc/Backspace clears), drag headers

Adding a module: extend Module, add settings with add(...), register it in ModuleManager.init().
If you change the Minecraft version, update gradle.properties (see https://fabricmc.net/develop).
