package xyz.harmonyapp.olympusblog.repository.main.create

import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleViewState
import xyz.harmonyapp.olympusblog.utils.DataState
import xyz.harmonyapp.olympusblog.utils.StateEvent


@MainScope
interface CreateArticleRepository {
    fun createNewArticle(
        title: RequestBody,
        description: RequestBody,
        body: RequestBody,
        tagList: List<String>,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ): Flow<DataState<CreateArticleViewState>>
}