-- ════════════════════════════════════════════════════════════
-- ЧАСТЬ 5/6 — КОСТЁР
-- Скопируй ВЕСЬ текст этой части и вставляй части по порядку
-- в ОДИН и тот же LocalScript: 1 → 2 → 3 → 4 → 5 → 6.
-- ════════════════════════════════════════════════════════════

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

