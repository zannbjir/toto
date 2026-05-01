package org.koitharu.kotatsu.backups.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koitharu.kotatsu.explore.data.SourcePresetEntity

@Serializable
class SourcePresetBackup(
	@SerialName("preset_id") val presetId: Long,
	@SerialName("title") val title: String,
	@SerialName("languages") val languages: String,
	@SerialName("sources") val sources: String,
	@SerialName("created_at") val createdAt: Long,
	@SerialName("sort_key") val sortKey: Int,
) {

	constructor(entity: SourcePresetEntity) : this(
		presetId = entity.presetId,
		title = entity.title,
		languages = entity.languages,
		sources = entity.sources,
		createdAt = entity.createdAt,
		sortKey = entity.sortKey,
	)

	fun toEntity() = SourcePresetEntity(
		presetId = 0,
		title = title,
		languages = languages,
		sources = sources,
		createdAt = createdAt,
		sortKey = sortKey,
		deletedAt = 0L,
	)
}