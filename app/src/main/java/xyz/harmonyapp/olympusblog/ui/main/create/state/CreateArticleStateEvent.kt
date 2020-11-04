package xyz.harmonyapp.olympusblog.ui.main.create.state

import okhttp3.MultipartBody

sealed class CreateArticleStateEvent {

    data class CreateNewArticleEvent(
        val title: String,
        val description: String,
        val body: String,
        val tags: String,
        val image: MultipartBody.Part
    ): CreateArticleStateEvent()

    class None: CreateArticleStateEvent()
} 