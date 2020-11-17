package xyz.harmonyapp.olympusblog.ui.main.article.state

import okhttp3.MultipartBody
import xyz.harmonyapp.olympusblog.utils.StateEvent

sealed class ArticleStateEvent : StateEvent {

    class GetArticlesEvent(val clearLayoutManagerState: Boolean = true) : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error retrieving articles."
        }

        override fun toString(): String {
            return "GetArticlesEvent"
        }
    }

    class ArticleSearchEvent(val clearLayoutManagerState: Boolean = true) : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error searching for articles."
        }

        override fun toString(): String {
            return "ArticleSearchEvent"
        }
    }

    class ArticleFeedEvent(val clearLayoutManagerState: Boolean = true) : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error retrieving your feed."
        }

        override fun toString(): String {
            return "ArticleFeedEvent"
        }
    }

    class ArticleBookmarkEvent(val clearLayoutManagerState: Boolean = true) : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error retrieving your bookmarked articles."
        }

        override fun toString(): String {
            return "ArticleBookmarkEvent"
        }
    }

    class ArticlesByTagEvent(val clearLayoutManagerState: Boolean = true) : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error retrieving articles."
        }

        override fun toString(): String {
            return "ArticlesByTagEvent"
        }
    }

    class CleanDBEvent : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting database."
        }

        override fun toString(): String {
            return "CleanDBEvent"
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

    data class CreateNewArticleEvent(
        val title: String,
        val description: String,
        val body: String,
        val tags: String,
        val image: MultipartBody.Part?
    ) : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Unable to create a new article."
        }

        override fun toString(): String {
            return "CreateNewArticleEvent"
        }
    }

    data class UpdateArticleEvent(
        val title: String,
        val description: String,
        val body: String,
        val tags: String,
        val image: MultipartBody.Part?
    ) : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error updating that article."
        }

        override fun toString(): String {
            return "UpdateArticlePostEvent"
        }

    }

    class ToggleFavoriteEvent : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error changing favorites status."
        }

        override fun toString(): String {
            return "ToggleFavoriteEvent"
        }
    }

    class ToggleBookmarkEvent : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error changing bookmark status."
        }

        override fun toString(): String {
            return "ToggleBookmarkEvent"
        }
    }

    class GetArticleCommentsEvent : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error getting article comments."
        }

        override fun toString(): String {
            return "GetArticleComments"
        }
    }

    data class PostCommentEvent(val body: String) : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error posting article comment."
        }

        override fun toString(): String {
            return "PostCommentEvent"
        }
    }

    class DeleteCommentEvent : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "Error deleting article comment."
        }

        override fun toString(): String {
            return "DeleteCommentEvent"
        }
    }


    class None : ArticleStateEvent() {
        override fun errorInfo(): String {
            return "None."
        }
    }
}