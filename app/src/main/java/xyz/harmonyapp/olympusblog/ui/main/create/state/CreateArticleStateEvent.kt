package xyz.harmonyapp.olympusblog.ui.main.create.state

import okhttp3.MultipartBody
import xyz.harmonyapp.olympusblog.utils.StateEvent

sealed class CreateArticleStateEvent : StateEvent {

    data class CreateNewArticleEvent(
        val title: String,
        val description: String,
        val body: String,
        val tags: String,
        val image: MultipartBody.Part
    ) : CreateArticleStateEvent() {
        override fun errorInfo(): String {
            return "Unable to create a new article."
        }

        override fun toString(): String {
            return "CreateNewArticleEvent"
        }
    }

    class None : CreateArticleStateEvent() {
        override fun errorInfo(): String {
            return "None."
        }
    }
} 