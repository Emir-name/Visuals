-- ════════════════════════════════════════════════════════════
-- ЧАСТЬ 6/6 — ОБЛАКА, СПАВН, АНИМАЦИЯ, ЗАПУСК
-- Скопируй ВЕСЬ текст этой части и вставляй части по порядку
-- в ОДИН и тот же LocalScript: 1 → 2 → 3 → 4 → 5 → 6.
-- ════════════════════════════════════════════════════════════

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
