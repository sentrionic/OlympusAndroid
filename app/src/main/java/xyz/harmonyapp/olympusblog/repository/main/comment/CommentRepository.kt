package xyz.harmonyapp.olympusblog.repository.main.comment

import kotlinx.coroutines.flow.Flow
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.utils.DataState
import xyz.harmonyapp.olympusblog.utils.StateEvent

@MainScope
interface CommentRepository {

    fun getArticleComments(slug: String, stateEvent: StateEvent): Flow<DataState<ArticleViewState>>

    fun postComment(
        body: String,
        slug: String,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>

    fun deleteComment(
        slug: String,
        id: Int,
        stateEvent: StateEvent
    ): Flow<DataState<ArticleViewState>>
}