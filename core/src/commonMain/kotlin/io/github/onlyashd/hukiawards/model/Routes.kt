package io.github.onlyashd.hukiawards.model

enum class Routes(val path: String) {
    Server("http://localhost:8080"),
    Api("/api"),
    Admin("/admin"),
    CallbackDiscord("/callback/discord"),
    Categories("/categories"),
    Search("/search"),
    LoginDiscord("/login/discord"),
    Logout("/logout"),
    Share("/share"),
    Top10("/top10"),
    Profile("/profile"),
    Vote("/vote"),
    Votes("/votes"),
    Users("/users"),
    Settings("/settings"),
    Admins("/admins"),
    ReorderCategories("/categories/reorder"),
    ExportVotes("/votes/export"),
    Stats("/stats"),
    Audit("/audit");

    fun subPath(sub: Routes) = this.path + sub.path
    fun byId(id: String) = "${this.path}/$id"
    fun byId() = "${this.path}/{id}"
    fun params(params: String) = "${this.path}?$params"

    companion object {
        fun String.subPath(sub: Routes) = "${this}/${sub.path}"
    }
}