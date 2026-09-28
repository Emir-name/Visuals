-- ════════════════════════════════════════════════════════════
-- ЧАСТЬ 3/6 — ОСТРОВ И ПРУД
-- Скопируй ВЕСЬ текст этой части и вставляй части по порядку
-- в ОДИН и тот же LocalScript: 1 → 2 → 3 → 4 → 5 → 6.
-- ════════════════════════════════════════════════════════════

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

