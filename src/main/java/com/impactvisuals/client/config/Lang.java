package com.impactvisuals.client.config;

import java.util.HashMap;
import java.util.Map;

public class Lang {

    private static final Map<String, String> RU = new HashMap<>();

    static {
        RU.put("COMBAT FX", "БОЙ FX");
        RU.put("COMBAT+", "БОЙ+");
        RU.put("HUD INFO", "ХАД ИНФО");
        RU.put("HUD STATS", "ХАД СТАТЫ");
        RU.put("HUD EXTRA", "ХАД ДОП");
        RU.put("ENVIRONMENT", "ОКРУЖЕНИЕ");
        RU.put("COSMETIC", "КОСМЕТИКА");
        RU.put("STYLE", "СТИЛЬ");
        RU.put("SOUND", "ЗВУК");
        RU.put("THEME", "ТЕМА");
        RU.put("SKINS", "СКИНЫ");

        RU.put("Hit Particles", "Частицы удара");
        RU.put("Damage Numbers", "Цифры урона");
        RU.put("Critical Flash", "Крит-вспышка");
        RU.put("Hitmarker Flash", "Маркер попадания");
        RU.put("Damage Flash", "Вспышка урона");
        RU.put("Impact Punch", "Импакт-панч");

        RU.put("Trajectory Predict", "Траектория");
        RU.put("Kill Streak", "Килстрик");
        RU.put("Big Kill Burst", "Взрыв на киле");
        RU.put("Pulsing Vignette", "Пульс виньетка");
        RU.put("Sweep Trail", "След взмаха");
        RU.put("Heal Flash", "Вспышка лечения");

        RU.put("Target HUD", "ХАД цели");
        RU.put("Info HUD", "Инфо ХАД");
        RU.put("Coordinates", "Координаты");
        RU.put("Compass", "Компас");
        RU.put("Session Timer", "Таймер сессии");
        RU.put("K/D Counter", "Счётчик K/D");
        RU.put("Target HUD Range", "Радиус ХАД цели");

        RU.put("Sprint Indicator", "Индикатор спринта");
        RU.put("Health %", "HP %");
        RU.put("Hunger %", "Голод %");
        RU.put("XP %", "Опыт %");
        RU.put("Armor HUD", "ХАД брони");
        RU.put("Biome", "Биом");
        RU.put("Active Effects", "Активные эффекты");

        RU.put("Light Level", "Уровень света");
        RU.put("Held Item Name", "Имя предмета");
        RU.put("Offhand Item Name", "Имя во второй руке");
        RU.put("Total Playtime", "Общее время игры");
        RU.put("Zoom (hold C)", "Зум (держать C)");
        RU.put("Real Clock", "Реальные часы");

        RU.put("Purple Sky", "Фиолетовое небо");
        RU.put("Low HP Vignette", "Виньетка низкого HP");
        RU.put("Durability %", "Прочность %");
        RU.put("Cooldown Bar", "Полоса кулдауна");
        RU.put("Kill Feed", "Килфид");
        RU.put("Small Fire", "Маленький огонь");

        RU.put("Custom Handle", "Кастомный хэндл");
        RU.put("Rainbow Theme", "Радужная тема");
        RU.put("Sprint Trail", "След спринта");
        RU.put("Footstep Dust", "Пыль шагов");
        RU.put("Colored Trails", "Цветные следы");
        RU.put("Hand Glow", "Свечение руки");
        RU.put("Scale %", "Масштаб %");
        RU.put("Rotate X", "Поворот X");
        RU.put("Rotate Y", "Поворот Y");
        RU.put("Rotate Z", "Поворот Z");

        RU.put("Crosshair Style", "Стиль прицела");
        RU.put("Hit Particle Color", "Цвет частиц удара");

        RU.put("Hit Sound", "Звук удара");
        RU.put("Crit Sound", "Звук крита");
        RU.put("Kill Sound", "Звук килла");
        RU.put("Streak Sound", "Звук стрика");
        RU.put("Heartbeat Sound", "Звук сердцебиения");
        RU.put("Menu Sound", "Звук меню");
        RU.put("Footstep Sound", "Звук шагов");

        RU.put("Skin (self-view only)", "Скин (только у себя)");

        RU.put("RESET", "СБРОС");
        RU.put("DONE", "ГОТОВО");

        RU.put("Off", "Выкл");
        RU.put("Dot", "Точка");
        RU.put("Cross", "Крест");
        RU.put("Ring", "Кольцо");
        RU.put("Vanilla", "Ваниль");
        RU.put("Orange", "Оранжевый");
        RU.put("Purple", "Фиолетовый");
        RU.put("Blue", "Синий");
        RU.put("Green", "Зелёный");
        RU.put("Red", "Красный");
        RU.put("Cyan", "Голубой");
        RU.put("Default", "По умолчанию");
        RU.put("Preset 1", "Пресет 1");
        RU.put("Preset 2", "Пресет 2");
        RU.put("Preset 3", "Пресет 3");
        RU.put("Preset 4", "Пресет 4");
        RU.put("Preset 5", "Пресет 5");
        RU.put("Preset 6", "Пресет 6");
        RU.put("Preset 7", "Пресет 7");
        RU.put("Preset 8", "Пресет 8");
        RU.put("Custom", "Свой");

        RU.put("(tap to change)", "(тапни, чтобы сменить)");
    }

    public static String t(String key) {
        if (!ModConfig.get().russianLanguage) return key;
        return RU.getOrDefault(key, key);
    }
}
