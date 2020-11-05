package xyz.harmonyapp.olympusblog.ui.main.create

import android.net.Uri
import androidx.lifecycle.LiveData
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.repository.main.CreateArticleRepository
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.BaseViewModel
import xyz.harmonyapp.olympusblog.ui.DataState
import xyz.harmonyapp.olympusblog.ui.Loading
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleStateEvent
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleStateEvent.CreateNewArticleEvent
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleStateEvent.None
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleViewState.NewArticleFields
import javax.inject.Inject

@MainScope
class CreateArticleViewModel
@Inject
constructor(
    val createArticleRepository: CreateArticleRepository,
    val sessionManager: SessionManager
) : BaseViewModel<CreateArticleStateEvent, CreateArticleViewState>() {

    override fun handleStateEvent(
        stateEvent: CreateArticleStateEvent
    ): LiveData<DataState<CreateArticleViewState>> {

        when (stateEvent) {

            is CreateNewArticleEvent -> {
                val title = RequestBody.create(MediaType.parse("text/plain"), stateEvent.title)
                val description =
                    RequestBody.create(MediaType.parse("text/plain"), stateEvent.description)
                val body = RequestBody.create(MediaType.parse("text/plain"), stateEvent.body)
                val tags = stateEvent.tags.split(",")

                return createArticleRepository.createArticle(
                    title,
                    description,
                    body,
                    tags,
                    stateEvent.image
                )
            }

            is None -> {
                return object : LiveData<DataState<CreateArticleViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            null,
                            Loading(false),
                            null
                        )
                    }
                }
            }
        }
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


    fun cancelActiveJobs() {
        createArticleRepository.cancelActiveJobs()
        handlePendingData()
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}
