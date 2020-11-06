package xyz.harmonyapp.olympusblog.ui.main.article.state

import okhttp3.MultipartBody
import xyz.harmonyapp.olympusblog.utils.StateEvent

sealed class ArticleStateEvent : StateEvent {

    class ArticleSearchEvent(val clearLayoutManagerState: Boolean = true) : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error searching for articles."
        }

        override fun toString(): String {
            return "ArticleSearchEvent"
        }
    }

    class CheckAuthorOfArticle : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error checking if you are the author of this article post."
        }

        override fun toString(): String {
            return "CheckAuthorOfArticlePost"
        }
    }

    class DeleteArticleEvent : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting that article."
        }

        override fun toString(): String {
            return "DeleteArticlePostEvent"
        }
    }

    data class UpdateArticleEvent(
        val title: String,
        val description: String,
        val body: String,
        val image: MultipartBody.Part?
    ) : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error updating that article."
        }

        override fun toString(): String {
            return "UpdateArticlePostEvent"
        }

    }

    class None : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "None."
        }
    }
}