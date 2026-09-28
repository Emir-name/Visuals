-- ════════════════════════════════════════════════════════════
-- ЧАСТЬ 2/6 — СВЕТ И АТМОСФЕРА
-- Скопируй ВЕСЬ текст этой части и вставляй части по порядку
-- в ОДИН и тот же LocalScript: 1 → 2 → 3 → 4 → 5 → 6.
-- ════════════════════════════════════════════════════════════

----------------------------------------------------------------
-- 1. СВЕТ И АТМОСФЕРА — главная красота
----------------------------------------------------------------
local function setupLighting()
	-- убираем старые эффекты, чтобы не задваивались
	for _, child in ipairs(Lighting:GetChildren()) do
		if child:IsA("PostEffect") or child:IsA("Atmosphere") or child:IsA("Sky") then
			child:Destroy()
		end
	end

	-- ── Базовое освещение: тёплый закат ──
	Lighting.ClockTime = CONFIG.ClockTime
	Lighting.GeographicLatitude = 41.7
	Lighting.Brightness = CONFIG.Brightness
	Lighting.ExposureCompensation = CONFIG.ExposureCompensation

	-- тени — прохладно-фиолетовые, отражённый свет — тёплый
	Lighting.Ambient = Color3.fromRGB(84, 76, 96)
	Lighting.OutdoorAmbient = Color3.fromRGB(148, 134, 142)
	Lighting.ColorShift_Top = Color3.fromRGB(255, 188, 130)
	Lighting.ColorShift_Bottom = Color3.fromRGB(118, 128, 172)

	Lighting.GlobalShadows = true
	Lighting.ShadowSoftness = 0.35
	Lighting.EnvironmentDiffuseScale = 0.5
	Lighting.EnvironmentSpecularScale = 0.7

	-- ── Небо ──
	local sky = Instance.new("Sky")
	sky.StarCount = 3000
	sky.SunAngularSize = 16
	sky.MoonAngularSize = 12
	sky.CelestialBodiesShown = true
	sky.Parent = Lighting

	-- ── Дымка между островом и горизонтом ──
	-- Color — тёплая пыль вблизи, Decay — синева вдали
	local atmosphere = Instance.new("Atmosphere")
	atmosphere.Density = 0.34
	atmosphere.Offset = 0.25
	atmosphere.Color = Color3.fromRGB(205, 180, 155)
	atmosphere.Decay = Color3.fromRGB(92, 104, 150)
	atmosphere.Glare = 0.22
	atmosphere.Haze = 1.7
	atmosphere.Parent = Lighting

	-- ── Свечение контрового света ──
	local bloom = Instance.new("BloomEffect")
	bloom.Intensity = 0.7
	bloom.Size = 32
	bloom.Threshold = 1.05
	bloom.Parent = Lighting

	-- ── Лучи солнца ──
	local sunRays = Instance.new("SunRaysEffect")
	sunRays.Intensity = 0.14
	sunRays.Spread = 0.72
	sunRays.Parent = Lighting

	-- ── Финальная «плёночная» подкрутка цвета ──
	local grade = Instance.new("ColorCorrectionEffect")
	grade.Name = "WarmGrade"
	grade.Brightness = 0.015
	grade.Contrast = 0.1
	grade.Saturation = 0.14
	grade.TintColor = Color3.fromRGB(255, 246, 235)
	grade.Parent = Lighting

	-- ── Глобальный слой облаков под островом ──
	local clouds = Instance.new("Clouds")
	clouds.Cover = 0.4
	clouds.Density = 0.45
	clouds.Parent = Workspace.Terrain
end

