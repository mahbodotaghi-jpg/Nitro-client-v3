package dev.nitro.module;

import dev.nitro.module.modules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        modules.add(new KillAura());
        modules.add(new AutoTotem());
        modules.add(new Sprint());
        modules.add(new Flight());
        modules.add(new Fullbright());
        modules.add(new StashFinder());
    }

    public List<Module> getAll() { return modules; }

    public List<Module> getByCategory(Category c) {
        return modules.stream().filter(m -> m.getCategory() == c).toList();
    }

    public List<Module> getEnabled() {
        return modules.stream().filter(Module::isEnabled).toList();
    }

    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        for (Module m : modules) {
            if (m.getKey() >= 0) {
                boolean down = InputUtil.isKeyPressed(mc.getWindow().getHandle(), m.getKey());
                if (down && !m.wasDown && mc.currentScreen == null) m.toggle();
                m.wasDown = down;
            }
            if (m.isEnabled()) m.onTick();
        }
    }
}
