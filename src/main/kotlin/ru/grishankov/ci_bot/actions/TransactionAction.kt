package ru.grishankov.ci_bot.actions

import ru.grishankov.ci_bot.features.settingsProject.BranchType

sealed interface TransactionAction {
    data object None : TransactionAction
    data object CreateProject : TransactionAction
    data class BuildBranch(val projectId: Int) : TransactionAction
    data class WorkBranch(val branchId: Int, val type: BranchType) : TransactionAction
}
