package xyz.harmonyapp.olympusblog.repository.main.create

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import xyz.harmonyapp.olympusblog.api.main.MainService
import xyz.harmonyapp.olympusblog.api.main.responses.ArticleResponse
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.persistence.ArticlesDao
import xyz.harmonyapp.olympusblog.repository.safeApiCall
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.main.create.state.CreateArticleViewState
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_ARTICLE_CREATED
import javax.inject.Inject

@MainScope
class CreateArticleRepositoryImpl
@Inject
constructor(
    val mainService: MainService,
    val articlesDao: ArticlesDao,
    val sessionManager: SessionManager
) : CreateArticleRepository {

    private val TAG: String = "AppDebug"

    override fun createNewArticle(
        title: RequestBody,
        description: RequestBody,
        body: RequestBody,
        tagList: List<String>,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow {

        val apiResult = safeApiCall(IO) {
            mainService.createArticle(
                title = title,
                description = description,
                body = body,
                tagList = tagList,
                image = image
            )
        }

        emit(
            object : ApiResponseHandler<CreateArticleViewState, ArticleResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: ArticleResponse): DataState<CreateArticleViewState> {

                    // If they don't have a paid membership account it will still return a 200
                    // Need to account for that

                    val article = resultObj.toArticle()
                    articlesDao.insert(article)

                    return DataState.data(
                        response = Response(
                            message = SUCCESS_ARTICLE_CREATED,
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }
}
