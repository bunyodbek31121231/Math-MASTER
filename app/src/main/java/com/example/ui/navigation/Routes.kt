package com.example.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val HOME = "home"
    const val PRACTICE = "practice?topicId={topicId}&difficulty={difficulty}"
    const val TOPICS = "topics"
    const val TOPIC_DETAIL = "topic_detail/{topicId}"
    const val TEST_SETUP = "test_setup"
    const val ACTIVE_TEST = "active_test?topicId={topicId}&difficulty={difficulty}&count={count}&isMaxsus={isMaxsus}"
    const val TEST_RESULT = "test_result/{testId}"
    const val TEST_HISTORY = "test_history"
    const val PROGRESS = "progress"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val MAXSUS_GATE = "maxsus_gate"
    const val MAXSUS_HOME = "maxsus_home"
    const val MAXSUS_TOPIC = "maxsus_topic/{topicId}"
    const val DEV_GENERATOR = "dev_generator"

    // Admin Panel Routes
    const val ADMIN_LOGIN = "admin_login"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_IMPORT = "admin_import"
    const val ADMIN_PREVIEW = "admin_preview"
    const val ADMIN_HISTORY = "admin_history"
    const val ADMIN_SEARCH = "admin_search"
    const val ADMIN_EDITOR = "admin_editor/{materialId}"

    fun practiceRoute(topicId: String? = null, difficulty: String? = null): String {
        val tParam = topicId ?: ""
        val dParam = difficulty ?: ""
        return "practice?topicId=$tParam&difficulty=$dParam"
    }

    fun activeTestRoute(
        topicId: String? = null,
        difficulty: String = "MEDIUM",
        count: Int = 10,
        isMaxsus: Boolean = false
    ): String {
        val tParam = topicId ?: ""
        return "active_test?topicId=$tParam&difficulty=$difficulty&count=$count&isMaxsus=$isMaxsus"
    }

    fun testResultRoute(testId: String): String = "test_result/$testId"
    fun topicDetailRoute(topicId: String): String = "topic_detail/$topicId"
    fun maxsusTopicRoute(topicId: String): String = "maxsus_topic/$topicId"
}
