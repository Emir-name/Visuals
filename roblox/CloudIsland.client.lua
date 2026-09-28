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

----------------------------------------------------------------
-- 2. ОСТРОВ
----------------------------------------------------------------
local function buildIsland()
	local topY = CONFIG.IslandPosition.Y
	local r = CONFIG.GrassRadius

	-- травяной «блин»
	makeCylinder(6, r * 2, CFrame.new(0, topY - 3, 0), COLOR.Grass, Enum.Material.Grass)

	-- слой земли
	makeCylinder(18, r * 2 - 10, CFrame.new(0, topY - 14, 0), COLOR.Dirt, Enum.Material.Ground)

	-- скальный шпиль вниз: ярусы всё уже и уже
	local y = topY - 22
	local radius = r - 10
	for _ = 1, 8 do
		local height = rng:NextNumber(10, 17)
		radius = radius * rng:NextNumber(0.76, 0.9)
		local offsetX = rng:NextNumber(-6, 6)
		local offsetZ = rng:NextNumber(-6, 6)
		makeCylinder(height, radius * 2, CFrame.new(offsetX, y - height / 2, offsetZ), COLOR.Rock, Enum.Material.Slate)
		y = y - height + 2.5 -- ярусы слегка заходят друг на друга
	end

	-- острый кончик шпиля
	makeBall(16, CFrame.new(0, y - 4, 0), COLOR.Rock, Enum.Material.Slate)

	-- каменные «карнизы», ломающие идеальный силуэт
	for _ = 1, 8 do
		local angle = rng:NextNumber(0, math.pi * 2)
		local shelfR = r * rng:NextNumber(0.86, 1.0)
		local size = rng:NextNumber(10, 20)
		makeBall(
			size,
			CFrame.new(math.cos(angle) * shelfR, topY - rng:NextNumber(8, 26), math.sin(angle) * shelfR),
			COLOR.Rock,
			Enum.Material.Slate
		)
	end
end

----------------------------------------------------------------
-- 3. ПРУД
----------------------------------------------------------------
local function buildPond()
	local topY = CONFIG.IslandPosition.Y
	local pos = Vector3.new(32, topY + 0.15, -14)

	local water = makeCylinder(1.2, 36, CFrame.new(pos), COLOR.Water, Enum.Material.Glass, 0.25)
	water.Reflectance = 0.08

	-- невидимая подложка с искрами над водой
	local glint = newPart({
		Size = Vector3.new(34, 0.2, 34),
		CFrame = CFrame.new(pos.X, pos.Y + 1, pos.Z),
		Transparency = 1,
		CanCollide = false,
		CanTouch = false,
		CanQuery = false,
	})

	local sparkles = Instance.new("ParticleEmitter")
	sparkles.Texture = "rbxasset://textures/particles/sparkles_main.dds"
	sparkles.LightEmission = 1
	sparkles.Size = NumberSequence.new(1.2)
	sparkles.Transparency = NumberSequence.new({
		NumberSequenceKeypoint.new(0, 1),
		NumberSequenceKeypoint.new(0.2, 0.2),
		NumberSequenceKeypoint.new(1, 1),
	})
	sparkles.Lifetime = NumberRange.new(1, 2)
	sparkles.Rate = 10
	sparkles.Speed = NumberRange.new(0.4, 1.2)
	sparkles.SpreadAngle = Vector2.new(180, 180)
	sparkles.Parent = glint
end

----------------------------------------------------------------
-- 4. ДЕРЕВЬЯ, КУСТЫ, ЦВЕТЫ, ВАЛУНЫ
----------------------------------------------------------------
local function buildTree(basePos, scale)
	local trunkH = 15 * scale
	makeCylinder(trunkH, 4.5 * scale, CFrame.new(basePos.X, basePos.Y + trunkH / 2, basePos.Z), COLOR.Trunk, Enum.Material.Wood)

	-- крона из перекрывающихся шаров
	for _ = 1, 8 do
		local d = rng:NextNumber(11, 19) * scale
		local offset = Vector3.new(
			rng:NextNumber(-8, 8) * scale,
			trunkH + rng:NextNumber(-3, 8) * scale,
			rng:NextNumber(-8, 8) * scale
		)
		local tint = COLOR.Leaf:Lerp(Color3.fromRGB(96, 158, 88), rng:NextNumber(0, 1))
		makeBall(d, CFrame.new(basePos + offset), tint, Enum.Material.Grass)
	end
end

local function buildTrees()
	local topY = CONFIG.IslandPosition.Y
	local spots = {
		Vector3.new(-26, topY, -20),
		Vector3.new(-34, topY, 18),
		Vector3.new(8, topY, 32),
	}
	for i, pos in ipairs(spots) do
		buildTree(pos, 0.95 + i * 0.12) -- каждый чуть другого размера
	end
end

local function buildFlowers()
	local topY = CONFIG.IslandPosition.Y
	for _ = 1, CONFIG.FlowerCount do
		local angle = rng:NextNumber(0, math.pi * 2)
		local radius = rng:NextNumber(8, CONFIG.GrassRadius * 0.82)
		local pos = Vector3.new(math.cos(angle) * radius, topY, math.sin(angle) * radius)

		-- стебель
		newPart({
			Size = Vector3.new(0.35, 2.2, 0.35),
			CFrame = CFrame.new(pos + Vector3.new(0, 1.1, 0)),
			Color = Color3.fromRGB(70, 130, 60),
			Material = Enum.Material.Grass,
			CanCollide = false,
		})

		-- головка
		local headColor = FLOWER_COLORS[rng:NextInteger(1, #FLOWER_COLORS)]
		makeBall(1.6, CFrame.new(pos + Vector3.new(0, 2.5, 0)), headColor, Enum.Material.SmoothPlastic).CanCollide = false
	end
end

local function buildBushes()
	local topY = CONFIG.IslandPosition.Y
	for _ = 1, CONFIG.BushCount do
		local angle = rng:NextNumber(0, math.pi * 2)
		local radius = rng:NextNumber(14, CONFIG.GrassRadius * 0.88)
		local pos = Vector3.new(math.cos(angle) * radius, topY, math.sin(angle) * radius)
		local puffs = rng:NextInteger(2, 3)
		for _ = 1, puffs do
			local d = rng:NextNumber(5, 9)
			local offset = Vector3.new(rng:NextNumber(-3, 3), rng:NextNumber(0, 2), rng:NextNumber(-3, 3))
			makeBall(d, CFrame.new(pos + offset), COLOR.Leaf, Enum.Material.Grass)
		end
	end
end

local function buildBoulders()
	local topY = CONFIG.IslandPosition.Y
	for _ = 1, CONFIG.BoulderCount do
		local angle = rng:NextNumber(0, math.pi * 2)
		local radius = rng:NextNumber(10, CONFIG.GrassRadius * 0.9)
		local d = rng:NextNumber(3, 8)
		local pos = Vector3.new(math.cos(angle) * radius, topY + d * 0.15, math.sin(angle) * radius)
		makeBall(d, CFrame.new(pos) * CFrame.Angles(rng:NextNumber(0, 3), rng:NextNumber(0, 3), 0), COLOR.Rock, Enum.Material.Slate)
	end
end

----------------------------------------------------------------
-- 5. КОСТЁР — живой источник света
----------------------------------------------------------------
local function buildCampfire()
	local basePos = Vector3.new(-14, CONFIG.IslandPosition.Y, 14)

	-- каменный круг
	for i = 1, 9 do
		local angle = (i / 9) * math.pi * 2
		makeBall(
			rng:NextNumber(2, 3.2),
			CFrame.new(basePos.X + math.cos(angle) * 4.2, basePos.Y + 0.8, basePos.Z + math.sin(angle) * 4.2),
			COLOR.Rock,
			Enum.Material.Slate
		)
	end

	-- поленья
	for i = 1, 3 do
		local angle = (i / 3) * math.pi
		newPart({
			Shape = Enum.PartType.Cylinder,
			Size = Vector3.new(6, 1.8, 1.8),
			CFrame = CFrame.new(basePos + Vector3.new(0, 1.4, 0)) * CFrame.Angles(0, angle, math.rad(12)),
			Color = COLOR.Trunk,
			Material = Enum.Material.Wood,
		})
	end

	-- ядро огня
	local flame = makeBall(3.6, CFrame.new(basePos + Vector3.new(0, 3, 0)), COLOR.Fire, Enum.Material.Neon, 0.15)

	-- тёплый свет
	local light = Instance.new("PointLight")
	light.Color = Color3.fromRGB(255, 170, 90)
	light.Brightness = 2
	light.Range = 38
	light.Shadows = true
	light.Parent = flame

	-- частицы пламени
	local fire = Instance.new("ParticleEmitter")
	fire.Texture = "rbxasset://textures/particles/fire_main.dds"
	fire.Color = ColorSequence.new(Color3.fromRGB(255, 150, 50), Color3.fromRGB(255, 230, 120))
	fire.LightEmission = 1
	fire.Size = NumberSequence.new({
		NumberSequenceKeypoint.new(0, 2.4),
		NumberSequenceKeypoint.new(1, 0.4),
	})
	fire.Transparency = NumberSequence.new({
		NumberSequenceKeypoint.new(0, 0.15),
		NumberSequenceKeypoint.new(1, 1),
	})
	fire.Lifetime = NumberRange.new(0.4, 0.8)
	fire.Rate = 28
	fire.Speed = NumberRange.new(2, 4)
	fire.SpreadAngle = Vector2.new(18, 18)
	fire.Acceleration = Vector3.new(0, 3, 0)
	fire.Parent = flame

	-- дым
	local smoke = Instance.new("ParticleEmitter")
	smoke.Texture = "rbxasset://textures/particles/smoke_main.dds"
	smoke.Color = ColorSequence.new(Color3.fromRGB(160, 150, 150))
	smoke.Size = NumberSequence.new({
		NumberSequenceKeypoint.new(0, 2),
		NumberSequenceKeypoint.new(1, 7),
	})
	smoke.Transparency = NumberSequence.new({
		NumberSequenceKeypoint.new(0, 0.55),
		NumberSequenceKeypoint.new(1, 1),
	})
	smoke.Lifetime = NumberRange.new(1.5, 2.5)
	smoke.Rate = 7
	smoke.Speed = NumberRange.new(3, 5)
	smoke.SpreadAngle = Vector2.new(25, 25)
	smoke.Acceleration = Vector3.new(1.5, 2, 0.5) -- лёгкий ветер
	smoke.Parent = flame

	return { flame = flame, light = light }
end

----------------------------------------------------------------
-- 6. ОБЛАКА И ПАРЯЩИЕ КАМНИ
----------------------------------------------------------------
local driftingClouds = {}
local floatingRocks = {}

local function buildClouds()
	local topY = CONFIG.IslandPosition.Y
	for _ = 1, CONFIG.CloudCount do
		local angle = rng:NextNumber(0, math.pi * 2)
		local radius = rng:NextNumber(110, 280)
		local y = topY + rng:NextNumber(-40, 25)

		local puffs = {}
		for _ = 1, rng:NextInteger(3, 5) do
			local d = rng:NextNumber(28, 58)
			local offset = Vector3.new(rng:NextNumber(-30, 30), rng:NextNumber(-10, 10), rng:NextNumber(-30, 30))
			local puff = makeBall(d, CFrame.new(math.cos(angle) * radius + offset.X, y + offset.Y, math.sin(angle) * radius + offset.Z), COLOR.Cloud, Enum.Material.SmoothPlastic, rng:NextNumber(0.35, 0.55))
			puff.CastShadow = false
			table.insert(puffs, { part = puff, offset = offset })
		end

		table.insert(driftingClouds, {
			angle = angle,
			radius = radius,
			y = y,
			speed = rng:NextNumber(0.008, 0.02),
			wobble = rng:NextNumber(0, math.pi * 2),
			puffs = puffs,
		})
	end
end

local function buildFloatingRocks()
	local topY = CONFIG.IslandPosition.Y
	for _ = 1, CONFIG.FloatingRockCount do
		local angle = rng:NextNumber(0, math.pi * 2)
		local radius = rng:NextNumber(40, 120)
		local d = rng:NextNumber(6, 14)
		local basePos = Vector3.new(
			math.cos(angle) * radius,
			topY - rng:NextNumber(110, 210),
			math.sin(angle) * radius
		)
		local rock = makeBall(d, CFrame.new(basePos) * CFrame.Angles(rng:NextNumber(0, 3), rng:NextNumber(0, 3), 0), COLOR.Rock, Enum.Material.Slate)
		table.insert(floatingRocks, {
			part = rock,
			basePos = basePos,
			rotation = rock.CFrame.Rotation, -- запоминаем поворот
			phase = rng:NextNumber(0, math.pi * 2),
			speed = rng:NextNumber(0.25, 0.5),
			amp = rng:NextNumber(2, 5),
		})
	end
end

----------------------------------------------------------------
-- 7. СПАВН ИГРОКА НА ОСТРОВЕ
----------------------------------------------------------------
local function placeCharacter(character)
	local root = character:WaitForChild("HumanoidRootPart", 5)
	if not root then
		return
	end
	task.wait(0.05) -- даём физике стартовать

	local target = CONFIG.IslandPosition + Vector3.new(-6, 4, 22)
	local lookAt = CONFIG.IslandPosition + Vector3.new(30, 6, -14) -- смотрим на пруд
	character:PivotTo(CFrame.lookAt(target, lookAt))
end

local function setupSpawn()
	-- чистим шаблон, чтобы под островом не висела Baseplate
	local baseplate = Workspace:FindFirstChild("Baseplate")
	if baseplate then
		baseplate:Destroy()
	end
	local spawnLocation = Workspace:FindFirstChildOfClass("SpawnLocation")
	if spawnLocation then
		spawnLocation:Destroy()
	end
	Workspace.FallenPartsDestroyHeight = -400

	player.CharacterAdded:Connect(placeCharacter)
	if player.Character then
		task.spawn(placeCharacter, player.Character)
	end
end

----------------------------------------------------------------
-- 8. АНИМАЦИЯ: облака плывут, камни покачиваются, огонь мерцает
----------------------------------------------------------------
local function startAnimation(campfire)
	local elapsed = 0

	RunService.Heartbeat:Connect(function(dt)
		elapsed = elapsed + dt

		-- облака медленно кружат вокруг острова
		for _, cloud in ipairs(driftingClouds) do
			cloud.angle = cloud.angle + cloud.speed * dt
			local base = Vector3.new(
				math.cos(cloud.angle) * cloud.radius,
				cloud.y + math.sin(elapsed * 0.3 + cloud.wobble) * 1.5,
				math.sin(cloud.angle) * cloud.radius
			)
			for _, puff in ipairs(cloud.puffs) do
				puff.part.CFrame = CFrame.new(base + puff.offset)
			end
		end

		-- парящие камни покачиваются
		for _, rock in ipairs(floatingRocks) do
			local rise = math.sin(elapsed * rock.speed + rock.phase) * rock.amp
			rock.part.CFrame = CFrame.new(rock.basePos + Vector3.new(0, rise, 0)) * rock.rotation
		end

		-- огонь живёт: свет и пламя мерцают
		if campfire and campfire.light and campfire.flame then
			local flicker = 0.5 + 0.5 * math.sin(elapsed * 11) * math.sin(elapsed * 7.3)
			campfire.light.Brightness = 1.6 + flicker * 0.9
			local s = 3.4 + flicker * 0.8
			campfire.flame.Size = Vector3.new(s, s, s)
		end
	end)
end

----------------------------------------------------------------
-- ЗАПУСК
----------------------------------------------------------------
setupLighting()
buildIsland()
buildPond()
buildTrees()
buildFlowers()
buildBushes()
buildBoulders()
buildClouds()
buildFloatingRocks()
setupSpawn()

local campfire = buildCampfire() -- один раз и отдаём в анимацию
startAnimation(campfire)
