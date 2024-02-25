package ru.grishankov.ci_bot.navigation

enum class ChainNavigation {

    ListProject,
    CreateProject,
    RemoveProject, RemoveSelectProject,
    DetailProject, BuildAndroidProject, BuildIosProject,
    AndroidBuild, IosBuild,
    SettingsProject, SettingsProdBranch, SettingsDemoBranch,
    Menu,
}
