package xyz.harmonyapp.olympusblog.ui.main.create

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.repository.main.create.CreateArticleRepositoryImpl
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.BaseViewModel
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleStateEvent.CreateNewArticleEvent
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleViewState.NewArticleFields
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.ErrorHandling.Companion.INVALID_STATE_EVENT
import javax.inject.Inject

@MainScope
class CreateArticleViewModel
@Inject
constructor(
    val createArticleRepository: CreateArticleRepositoryImpl,
    val sessionManager: SessionManager
) : BaseViewModel<CreateArticleViewState>() {

    override fun handleNewData(data: CreateArticleViewState) {

        setNewArticleFields(
            data.articleFields.newArticleTitle,
            data.articleFields.newArticleDescription,
            data.articleFields.newArticleBody,
            data.articleFields.newArticleTags,
            data.articleFields.newImageUri
        )
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        val job: Flow<DataState<CreateArticleViewState>> = when (stateEvent) {

            is CreateNewArticleEvent -> {
                val title = stateEvent.title.toRequestBody("text/plain".toMediaTypeOrNull())
                val description =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), stateEvent.description)
                val body = stateEvent.body.toRequestBody("text/plain".toMediaTypeOrNull())
                val tags = stateEvent.tags.split(",")

                createArticleRepository.createNewArticle(
                    title,
                    description,
                    body,
                    tags,
                    stateEvent.image,
                    stateEvent = stateEvent
                )
            }

            else -> {
                flow {
                    emit(
                        DataState.error<CreateArticleViewState>(
                            response = Response(
                                message = INVALID_STATE_EVENT,
                                uiComponentType = UIComponentType.None(),
                                messageType = MessageType.Error()
                            ),
                            stateEvent = stateEvent
                        )
                    )
                }
            }
        }
        launchJob(stateEvent, job)
    }

    override fun initNewViewState(): CreateArticleViewState {
        return CreateArticleViewState()
    }

    fun clearNewArticleFields() {
        val update = getCurrentViewStateOrNew()
        update.articleFields = NewArticleFields()
        setViewState(update)
    }

    fun setNewArticleFields(
        title: String?,
        description: String?,
        body: String?,
        tags: String?,
        uri: Uri?
    ) {
        val update = getCurrentViewStateOrNew()
        val newArticleFields = update.articleFields
        title?.let { newArticleFields.newArticleTitle = it }
        body?.let { newArticleFields.newArticleBody = it }
        description?.let { newArticleFields.newArticleDescription = it }
        tags?.let { newArticleFields.newArticleTags = it }
        uri?.let { newArticleFields.newImageUri = it }
        update.articleFields = newArticleFields
        setViewState(update)
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}
