package ru.grishankov.ci_bot.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object ProjectAndroidVersions : IntIdTable("android_versions") {
    val idProject = reference("project_id", Projects)
    val increment: Column<Int> = integer("increment")
}

class ProjectAndroidVersion(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProjectAndroidVersion>(ProjectAndroidVersions)
    var project by Project referencedOn ProjectAndroidVersions.idProject
    var increment by ProjectAndroidVersions.increment
}
