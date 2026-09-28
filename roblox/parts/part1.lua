-- ════════════════════════════════════════════════════════════
-- ЧАСТЬ 1/6 — СЕРВИСЫ, НАСТРОЙКИ, ХЕЛПЕРЫ
-- Скопируй ВЕСЬ текст этой части и вставляй части по порядку
-- в ОДИН и тот же LocalScript: 1 → 2 → 3 → 4 → 5 → 6.
-- ════════════════════════════════════════════════════════════

--[[
	══════════════════════════════════════════════════════════════════
	☁  ОБЛАЧНЫЙ ОСТРОВ — вся мини-игра в одном LocalScript
	══════════════════════════════════════════════════════════════════

	Что внутри:
	  • Остров в небе: трава, земля, скальный шпиль внизу
	  • Деревья, пруд, цветы, кусты, валуны
	  • Костёр с живым огнём и тёплым PointLight
	  • Плывущие облака вокруг острова и парящие камни
	  • Закатный свет: Atmosphere + Bloom + SunRays + ColorCorrection

	Как запустить:
	  1. Roblox Studio → шаблон «Baseplate»
	  2. Explorer → StarterPlayer → StarterPlayerScripts
	     → Insert Object → LocalScript
	  3. Вставь туда этот код целиком
	  4. Play (F5) — остров и свет создадутся сами

	Всё строится на клиенте (LocalScript), сервер не нужен.
	Во время Play ищи объекты в Workspace → CloudIsland.
]]

----------------------------------------------------------------
-- СЕРВИСЫ
----------------------------------------------------------------
local Players = game:GetService("Players")
local Lighting = game:GetService("Lighting")
local RunService = game:GetService("RunService")
local Workspace = game:GetService("Workspace")

local player = Players.LocalPlayer
local rng = Random.new(28092026) -- фиксированный seed: остров всегда одинаковый

----------------------------------------------------------------
-- КОНФИГ — всё, что обычно хочется покрутить
----------------------------------------------------------------
local CONFIG = {
	-- ── Остров ──
	IslandPosition = Vector3.new(0, 300, 0), -- центр острова (Y = высота в небе)
	GrassRadius = 70,                       -- радиус травяного «блина»
	FlowerCount = 22,
	BushCount = 7,
	BoulderCount = 10,

	-- ── Небо и свет ──
	ClockTime = 16.3,            -- время суток: 6 рассвет, 12 полдень, 18 закат
	Brightness = 2.8,            -- яркость солнца
	ExposureCompensation = 0.15, -- экспозиция

	-- ── Облака ──
	CloudCount = 14,
	FloatingRockCount = 6,
}

----------------------------------------------------------------
-- ПАЛИТРА
----------------------------------------------------------------
local COLOR = {
	Grass = Color3.fromRGB(94, 156, 84),
	Dirt  = Color3.fromRGB(126, 92, 64),
	Rock  = Color3.fromRGB(110, 106, 104),
	Trunk = Color3.fromRGB(102, 72, 52),
	Leaf  = Color3.fromRGB(64, 124, 66),
	Water = Color3.fromRGB(94, 162, 214),
	Cloud = Color3.fromRGB(255, 255, 255),
	Fire  = Color3.fromRGB(255, 150, 60),
}

local FLOWER_COLORS = {
	Color3.fromRGB(235, 110, 140),
	Color3.fromRGB(240, 200, 90),
	Color3.fromRGB(220, 150, 235),
	Color3.fromRGB(240, 240, 240),
	Color3.fromRGB(250, 130, 80),
}

----------------------------------------------------------------
-- ПАПКА ДЛЯ ВСЕХ ОБЪЕКТОВ ИГРЫ
----------------------------------------------------------------
local world = Instance.new("Folder")
world.Name = "CloudIsland"
world.Parent = Workspace

----------------------------------------------------------------
-- ХЕЛПЕРЫ ДЕТАЛЕЙ
----------------------------------------------------------------
local function newPart(props)
	local p = Instance.new("Part")
	p.Anchored = true
	p.TopSurface = Enum.SurfaceType.Smooth
	p.BottomSurface = Enum.SurfaceType.Smooth
	p.Material = Enum.Material.SmoothPlastic
	for key, value in pairs(props) do
		p[key] = value
	end
	p.Parent = world
	return p
end

-- Вертикальный цилиндр: height — высота, diameter — диаметр, cf — центр
local function makeCylinder(height, diameter, cf, color, material, transparency)
	return newPart({
		Shape = Enum.PartType.Cylinder,
		Size = Vector3.new(height, diameter, diameter),
		CFrame = cf * CFrame.Angles(0, 0, math.rad(90)), -- ось X -> вверх
		Color = color,
		Material = material,
		Transparency = transparency or 0,
	})
end

local function makeBall(diameter, cf, color, material, transparency)
	return newPart({
		Shape = Enum.PartType.Ball,
		Size = Vector3.new(diameter, diameter, diameter),
		CFrame = cf,
		Color = color,
		Material = material,
		Transparency = transparency or 0,
	})
end

