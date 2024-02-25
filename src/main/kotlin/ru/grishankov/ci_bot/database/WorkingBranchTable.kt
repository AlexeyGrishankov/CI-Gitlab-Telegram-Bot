package ru.grishankov.ci_bot.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object WorkingBranches : IntIdTable("working_branches") {
    val idProject = reference("project_id", Projects)
    val prodBranch = varchar("prod_branch", length = 100).default("main")
    val demoBranch = varchar("demo_branch", length = 100).default("develop")
}

class WorkingBranch(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WorkingBranch>(WorkingBranches)
    var project by Project referencedOn WorkingBranches.idProject
    var prodBranch by WorkingBranches.prodBranch
    var demoBranch by WorkingBranches.demoBranch
}
