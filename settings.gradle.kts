pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // PREFER_SETTINGS：依赖一律由下方仓库解析；本项目不再声明任何第三方镜像，
    // 保证 clone 下来的构建行为与 CI 一致（Init 脚本注入的仓库会被忽略而不报错）。
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CurrencyCalculator"
include(":app")
