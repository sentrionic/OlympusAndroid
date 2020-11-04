package xyz.harmonyapp.olympusblog.ui.main.article.state

import okhttp3.MultipartBody

sealed class ArticleStateEvent {

    class ArticleSearchEvent : ArticleStateEvent()

    class RestoreArticleListFromCache: ArticleStateEvent()

    class CheckAuthorOfArticle: ArticleStateEvent()

    class DeleteArticleEvent: ArticleStateEvent()

    data class UpdateArticleEvent(
        val title: String,
        val description: String,
        val body: String,
        val image: MultipartBody.Part?
    ): ArticleStateEvent()

    class None : ArticleStateEvent()
}