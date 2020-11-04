package xyz.harmonyapp.olympusblog.repository.main

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.api.main.MainService
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleResponse
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.persistence.ArticlesDao
import xyz.harmonyapp.olympusblog.repository.JobManager
import xyz.harmonyapp.olympusblog.repository.NetworkBoundResource
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.DataState
import xyz.harmonyapp.olympusblog.ui.Response
import xyz.harmonyapp.olympusblog.ui.ResponseType
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleViewState
import xyz.harmonyapp.olympusblog.utils.AbsentLiveData
import xyz.harmonyapp.olympusblog.utils.ApiSuccessResponse
import xyz.harmonyapp.olympusblog.utils.GenericApiResponse
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_ARTICLE_CREATED
import javax.inject.Inject

class CreateArticleRepository
@Inject
constructor(
    val mainService: MainService,
    val articlesDao: ArticlesDao,
    val sessionManager: SessionManager
) : JobManager("CreateArticleRepository") {

    private val TAG: String = "AppDebug"

    fun createArticle(
        title: RequestBody,
        description: RequestBody,
        body: RequestBody,
        tagList: List<String>,
        image: MultipartBody.Part?
    ): LiveData<DataState<CreateArticleViewState>> {
        return object :
            NetworkBoundResource<ArticleResponse, Article, CreateArticleViewState>(
                sessionManager.isConnectedToTheInternet(),
                true,
                true,
                false
            ) {

            // not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ArticleResponse>) {

                updateLocalDb(response.body.toArticle())

                withContext(Dispatchers.Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(SUCCESS_ARTICLE_CREATED, ResponseType.Dialog())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<ArticleResponse>> {
                return mainService.createArticle(
                    title,
                    description,
                    tagList,
                    body,
                    image
                )
            }

            // not applicable
            override fun loadFromCache(): LiveData<CreateArticleViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Article?) {
                cacheObject?.let {
                    articlesDao.insert(it)
                }
            }

            override fun setJob(job: Job) {
                addJob("createNewArticle", job)
            }

        }.asLiveData()
    }
}
