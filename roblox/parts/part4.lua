-- ════════════════════════════════════════════════════════════
-- ЧАСТЬ 4/6 — ДЕРЕВЬЯ, ЦВЕТЫ, КУСТЫ, ВАЛУНЫ
-- Скопируй ВЕСЬ текст этой части и вставляй части по порядку
-- в ОДИН и тот же LocalScript: 1 → 2 → 3 → 4 → 5 → 6.
-- ════════════════════════════════════════════════════════════

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

