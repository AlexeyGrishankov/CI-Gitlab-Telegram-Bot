package ru.grishankov.ci_bot.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Projects : IntIdTable("Projects") {
    val name: Column<String> = varchar("name", length = 100)
    val token: Column<String> = varchar("token", length = 1000)
    val isActive: Column<Boolean> = bool("is_active").default(true)
    val gitId: Column<Int> = integer("git_id")
}

class Project(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Project>(Projects)
    var name by Projects.name
    var token by Projects.token
    var isActive by Projects.isActive
    var gitId by Projects.gitId
}
