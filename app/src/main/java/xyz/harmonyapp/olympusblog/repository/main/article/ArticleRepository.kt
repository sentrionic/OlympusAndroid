package xyz.harmonyapp.olympusblog.repository.main.article

import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.ArticleEntity
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.utils.DataState
import xyz.harmonyapp.olympusblog.utils.StateEvent

@MainScope
interface ArticleRepository {

    fun searchArticles(
        query: String,
        order: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>

    fun getFeed(
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>

    fun getBookmarkedArticles(
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>

    fun dropDatabase(stateEvent: StateEvent): Flow<DataState<ArticleViewState>>

    fun restoreArticleListFromCache(
        query: String,
        order: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>

    fun isAuthorOfArticle(
        id: Int,
        slug: String,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>

    fun deleteArticle(
        article: Article,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>

    fun updateArticle(
        slug: String,
        title: RequestBody,
        description: RequestBody,
        body: RequestBody,
        tags: List<String>,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>

    fun toggleFavorite(
        article: Article,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>

    fun toggleBookmark(
        article: Article,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>

    fun getArticleComments(slug: String, stateEvent: StateEvent): Flow<DataState<ArticleViewState>>

    fun postComment(body: String, slug: String, stateEvent: StateEvent): Flow<DataState<ArticleViewState>>

    fun deleteComment(slug: String, id: Int, stateEvent: StateEvent): Flow<DataState<ArticleViewState>>
}
