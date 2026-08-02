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
        RU.put("Build Helper", "Помощник стройки");
        RU.put("Jump Ring", "Кольцо при прыжке");
        RU.put("Emir Config (enable all)", "Emir Config (включить всё)");
        RU.put("Focus Target Highlight", "Подсветка цели по нику");
        RU.put("Focus Target Name", "Ник цели");
        RU.put("Target HUD Debug", "Отладка ХАД цели");
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
        RU.put("Auto Jump", "Авто прыжок");
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
        RU.put("Cape (self-view only)", "Плащ (только у себя)");
        RU.put("Elytra (self-view only)", "Элитры (только у себя)");
        RU.put("Arm Model (self-view only)", "Модель рук (только у себя)");

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
        RU.put("Search", "Поиск");
        RU.put("No results", "Ничего не найдено");
        RU.put("FRIENDS", "ДРУЗЬЯ");
        RU.put("Nickname", "Ник");
        RU.put("Add", "Добавить");
        RU.put("Offline", "Не в сети");
        RU.put("Online", "В сети");
        RU.put("Singleplayer", "Одиночная игра");
        RU.put("No friends added yet", "Друзья ещё не добавлены");
    }

    private static final Map<String, String> DESC_EN = new HashMap<>();
    private static final Map<String, String> DESC_RU = new HashMap<>();

    static {
        put(DESC_EN, DESC_RU, "Hit Particles", "Particle burst when you land a hit", "Частицы при попадании по цели");
        put(DESC_EN, DESC_RU, "Damage Numbers", "Floating numbers showing damage dealt", "Всплывающие цифры нанесённого урона");
        put(DESC_EN, DESC_RU, "Critical Flash", "Screen flash on a critical hit", "Вспышка на экране при крите");
        put(DESC_EN, DESC_RU, "Hitmarker Flash", "Marker flash over the crosshair on hit", "Вспышка маркера попадания на прицеле");
        put(DESC_EN, DESC_RU, "Damage Flash", "Red flash when you take damage", "Красная вспышка при получении урона");
        put(DESC_EN, DESC_RU, "Impact Punch", "Camera punch/shake on impact", "Тряска камеры при ударе");

        put(DESC_EN, DESC_RU, "Trajectory Predict", "Predicted path line for thrown projectiles", "Линия предсказания траектории снаряда");
        put(DESC_EN, DESC_RU, "Kill Streak", "On-screen notice for kill streaks", "Уведомление о серии убийств");
        put(DESC_EN, DESC_RU, "Big Kill Burst", "Bigger particle burst on a kill", "Крупный эффект частиц при убийстве");
        put(DESC_EN, DESC_RU, "Kill Laser", "Tall pink beam of light shoots up on a kill", "Высокий розовый луч света бьёт вверх при убийстве");
        put(DESC_EN, DESC_RU, "Pulsing Vignette", "Screen edges pulse during combat", "Пульсирующее затемнение краёв экрана");
        put(DESC_EN, DESC_RU, "Sweep Trail", "Motion trail behind weapon swings", "След движения за взмахом оружия");
        put(DESC_EN, DESC_RU, "Heal Flash", "Green flash when you're healed", "Зелёная вспышка при лечении");

        put(DESC_EN, DESC_RU, "Target HUD", "Info card for the entity under your crosshair", "Карточка с инфо о цели под прицелом");
        put(DESC_EN, DESC_RU, "Build Helper", "Preview outline + distance/coords for block placement", "Превью блока + расстояние/координаты при постройке");
        put(DESC_EN, DESC_RU, "Jump Ring", "Glowing particle ring under you when you jump (local only)", "Светящееся кольцо под ногами при прыжке (только у себя)");
        put(DESC_EN, DESC_RU, "Emir Config (enable all)", "Turns on every feature toggle in the mod at once", "Включает разом все настройки мода");
        put(DESC_EN, DESC_RU, "Focus Target Highlight", "Outlines the named player when they're actually visible", "Обводит рамкой указанного игрока, когда он реально виден");
        put(DESC_EN, DESC_RU, "Target HUD Debug", "Dumps nearby entities/nametags to chat for diagnosing wrong HP", "Выводит в чат сущности рядом с целью для диагностики ХП");
        put(DESC_EN, DESC_RU, "Info HUD", "Top-right badge: name, FPS, frame time", "Плашка в углу: ник, FPS, время кадра");
        put(DESC_EN, DESC_RU, "Coordinates", "Show your current X/Y/Z", "Показывать текущие координаты");
        put(DESC_EN, DESC_RU, "Compass", "On-screen heading compass", "Компас направления на экране");
        put(DESC_EN, DESC_RU, "Session Timer", "How long you've played this session", "Сколько играете в текущей сессии");
        put(DESC_EN, DESC_RU, "K/D Counter", "Track kills and deaths", "Счётчик убийств и смертей");

        put(DESC_EN, DESC_RU, "Sprint Indicator", "Shows when you're sprinting", "Показывает, когда вы бежите");
        put(DESC_EN, DESC_RU, "Health %", "Health shown as a percentage", "Здоровье в процентах");
        put(DESC_EN, DESC_RU, "Hunger %", "Hunger shown as a percentage", "Голод в процентах");
        put(DESC_EN, DESC_RU, "XP %", "Progress to next level as a percentage", "Прогресс до следующего уровня в процентах");
        put(DESC_EN, DESC_RU, "Armor HUD", "Current armor value", "Текущее значение брони");
        put(DESC_EN, DESC_RU, "Biome", "Name of the biome you're standing in", "Название биома, в котором вы находитесь");
        put(DESC_EN, DESC_RU, "Active Effects", "List of active potion effects", "Список активных эффектов зелий");

        put(DESC_EN, DESC_RU, "Light Level", "Light level at your position", "Уровень освещения в вашей позиции");
        put(DESC_EN, DESC_RU, "Held Item Name", "Name of the item in your main hand", "Название предмета в основной руке");
        put(DESC_EN, DESC_RU, "Offhand Item Name", "Name of the item in your off hand", "Название предмета во второй руке");
        put(DESC_EN, DESC_RU, "Total Playtime", "Total time played across sessions", "Общее время игры за все сессии");
        put(DESC_EN, DESC_RU, "Zoom (hold C)", "Hold C to zoom in", "Зажмите C, чтобы приблизить обзор");
        put(DESC_EN, DESC_RU, "Auto Jump", "Automatically jumps over blocks while moving", "Автоматически перепрыгивает блоки при движении");
        put(DESC_EN, DESC_RU, "Real Clock", "Show your device's real-world time", "Показывать реальное время устройства");

        put(DESC_EN, DESC_RU, "Purple Sky", "Recolor the sky and fog purple", "Перекрашивает небо и туман в фиолетовый");
        put(DESC_EN, DESC_RU, "Low HP Vignette", "Red screen edges at low health", "Красное затемнение краёв при малом HP");
        put(DESC_EN, DESC_RU, "Durability %", "Equipment durability as a percentage", "Прочность экипировки в процентах");
        put(DESC_EN, DESC_RU, "Cooldown Bar", "Item cooldown progress bar", "Полоса перезарядки предмета");
        put(DESC_EN, DESC_RU, "Kill Feed", "Feed of recent kills", "Лента последних убийств");
        put(DESC_EN, DESC_RU, "Small Fire", "Shrinks the full-screen fire overlay", "Уменьшает оверлей огня на весь экран");

        put(DESC_EN, DESC_RU, "Custom Handle", "Custom held-item position and rotation", "Своя позиция и поворот предмета в руке");
        put(DESC_EN, DESC_RU, "Rainbow Theme", "Cycles the menu accent through colors", "Циклический перебор акцентных цветов меню");
        put(DESC_EN, DESC_RU, "Sprint Trail", "Particle trail while sprinting", "След из частиц во время спринта");
        put(DESC_EN, DESC_RU, "Footstep Dust", "Dust puffs under your feet", "Пыль под ногами при ходьбе");
        put(DESC_EN, DESC_RU, "Colored Trails", "Colored motion trail", "Цветной след движения");
        put(DESC_EN, DESC_RU, "Hand Glow", "Glow around your held item", "Свечение вокруг предмета в руке");

        put(DESC_EN, DESC_RU, "Hit Sound", "Sound played on a hit", "Звук при попадании");
        put(DESC_EN, DESC_RU, "Crit Sound", "Sound played on a critical hit", "Звук при критическом ударе");
        put(DESC_EN, DESC_RU, "Kill Sound", "Sound played on a kill", "Звук при убийстве");
        put(DESC_EN, DESC_RU, "Streak Sound", "Sound played on a kill streak", "Звук при серии убийств");
        put(DESC_EN, DESC_RU, "Heartbeat Sound", "Heartbeat sound at low health", "Звук сердцебиения при малом HP");
        put(DESC_EN, DESC_RU, "Menu Sound", "UI sound when using the menu", "Звук интерфейса при работе с меню");
        put(DESC_EN, DESC_RU, "Footstep Sound", "Custom footstep sound", "Свой звук шагов");
        put(DESC_EN, DESC_RU, "Friends Feature", "Share your server with friends you add below", "Показывать друзьям, добавленным ниже, ваш сервер");
    }

    private static void put(Map<String, String> en, Map<String, String> ru, String key, String enVal, String ruVal) {
        en.put(key, enVal);
        ru.put(key, ruVal);
    }

    /** Short description shown under a card title in the settings UI. */
    public static String desc(String key) {
        Map<String, String> map = ModConfig.get().russianLanguage ? DESC_RU : DESC_EN;
        return map.getOrDefault(key, "");
    }

    public static String t(String key) {
        if (!ModConfig.get().russianLanguage) return key;
        return RU.getOrDefault(key, key);
    }
}
