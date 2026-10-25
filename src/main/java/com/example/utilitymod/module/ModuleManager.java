package com.example.utilitymod.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleManager {

    private static final List<Module> MODULES = new ArrayList<>();

    public static HudModule hud;

    public static void init() {
        hud = new HudModule();

        MODULES.add(new Fly());
        MODULES.add(new Speed());
        MODULES.add(new AutoSprint());
        MODULES.add(new Step());
        MODULES.add(new Fullbright());
        MODULES.add(new Zoom());
        MODULES.add(hud);

        hud.setEnabled(true);
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<Module> byCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module m : MODULES) {
            if (m.category == category) {
                result.add(m);
            }
        }
        return result;
    }

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        for (Module m : MODULES) {
            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }

    // ------------------------------------------------------------------
    // Движение
    // ------------------------------------------------------------------

    public static class Fly extends Module {
        private final NumberSetting speed = add(new NumberSetting("Скорость", 2, 1, 10, 1));

        public Fly() {
            super("Полёт", "Свободный полёт как в креативе", Category.MOVEMENT);
        }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            p.abilities.allowFlying = true;
            p.abilities.flying = true;
            p.abilities.setFlySpeed(0.05f * speed.getFloat());
        }

        @Override
        protected void onDisable() {
            ClientPlayerEntity p = mc.player;
            if (p == null) return;
            p.abilities.setFlySpeed(0.05f);
            if (!p.isCreative() && !p.isSpectator()) {
                p.abilities.allowFlying = false;
                p.abilities.flying = false;
            }
        }

        @Override
        public String hudLabel() {
            return name + " §7" + speed.display();
        }
    }

    public static class Speed extends Module {
        private final NumberSetting multiplier = add(new NumberSetting("Множитель", 1.2, 1.1, 2.0, 0.1));

        public Speed() {
            super("Ускорение", "Быстрее бегаешь по земле", Category.MOVEMENT);
        }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            boolean moving = p.input.movementForward != 0 || p.input.movementSideways != 0;
            if (moving && p.isOnGround()) {
                Vec3d v = p.getVelocity();
                double m = multiplier.get();
                p.setVelocity(v.x * m, v.y, v.z * m);
            }
        }

        @Override
        public String hudLabel() {
            return name + " §7x" + multiplier.display();
        }
    }

    public static class AutoSprint extends Module {
        public AutoSprint() {
            super("Автоспринт", "Бежишь всегда, когда идёшь вперёд", Category.MOVEMENT);
        }

        @Override
        public void onTick() {
            ClientPlayerEntity p = mc.player;
            if (p.input.movementForward > 0 && !p.horizontalCollision && !p.isUsingItem()) {
                p.setSprinting(true);
            }
        }
    }

    public static class Step extends Module {
        private final NumberSetting height = add(new NumberSetting("Высота", 1, 1, 2, 1));

        public Step() {
            super("Step", "Автоматически поднимаешься на блоки", Category.MOVEMENT);
        }

        @Override
        public void onTick() {
            mc.player.stepHeight = height.getFloat();
        }

        @Override
        protected void onDisable() {
            if (mc.player != null) {
                mc.player.stepHeight = 0.6f;
            }
        }

        @Override
        public String hudLabel() {
            return name + " §7" + height.display();
        }
    }

    // ------------------------------------------------------------------
    // Визуалы
    // ------------------------------------------------------------------

    public static class Fullbright extends Module {
        private double savedGamma = 1.0;

        public Fullbright() {
            super("Фуллбрайт", "Максимальная яркость, видно в темноте", Category.VISUAL);
        }

        @Override
        protected void onEnable() {
            savedGamma = mc.options.gamma;
            mc.options.gamma = 15.0;
        }

        @Override
        public void onTick() {
            mc.options.gamma = 15.0;
        }

        @Override
        protected void onDisable() {
            mc.options.gamma = savedGamma;
        }
    }

    public static class Zoom extends Module {
        private final NumberSetting factor = add(new NumberSetting("Приближение", 3, 1.5, 8, 0.5));
        private double savedFov = 70.0;

        public Zoom() {
            super("Зум", "Приближение камеры", Category.VISUAL);
        }

        @Override
        protected void onEnable() {
            savedFov = mc.options.fov;
        }

        @Override
        public void onTick() {
            mc.options.fov = Math.max(5.0, savedFov / factor.get());
        }

        @Override
        protected void onDisable() {
            mc.options.fov = savedFov;
        }

        @Override
        public String hudLabel() {
            return name + " §7x" + factor.display();
        }
    }

    public static class HudModule extends Module {
        public HudModule() {
            super("Интерфейс", "Ватермарк, FPS, координаты и список модулей", Category.VISUAL);
        }
    }
}
