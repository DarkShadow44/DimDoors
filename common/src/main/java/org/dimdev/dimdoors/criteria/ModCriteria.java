package org.dimdev.dimdoors.criteria;

import dev.architectury.registry.level.advancement.CriteriaTriggersRegistry;

public class ModCriteria {
	public static final RiftTrackedCriterion RIFT_TRACKED = CriteriaTriggersRegistry.register(new RiftTrackedCriterion());
	public static final TagBlockBreakCriteria TAG_BLOCK_BREAK = CriteriaTriggersRegistry.register(new TagBlockBreakCriteria());
	public static final PocketSpawnPointSetCondition POCKET_SPAWN_POINT_SET = CriteriaTriggersRegistry.register(new PocketSpawnPointSetCondition());

	public static void init() {
	}
}
