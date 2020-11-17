package xyz.harmonyapp.olympusblog.repository.main.comment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import xyz.harmonyapp.olympusblog.api.main.MainService
import xyz.harmonyapp.olympusblog.api.main.dto.CommentDTO
import xyz.harmonyapp.olympusblog.api.main.responses.CommentResponse
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.repository.safeApiCall
import xyz.harmonyapp.olympusblog.session.SessionManager
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.utils.*
import javax.inject.Inject

@MainScope
class CommentRepositoryImpl
@Inject
constructor(
    val mainService: MainService,
    val sessionManager: SessionManager
) : CommentRepository {

    override fun getArticleComments(
        slug: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(Dispatchers.IO) {
            mainService.getArticleComments(slug)
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, List<CommentResponse>>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: List<CommentResponse>
                ): DataState<ArticleViewState> {

                    return DataState.data(
                        response = null,
                        data = ArticleViewState(
                            viewArticleFields = ArticleViewState.ViewArticleFields(
                                commentList = resultObj
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

    override fun postComment(
        body: String,
        slug: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(Dispatchers.IO) {
            mainService.createComment(slug = slug, body = CommentDTO(body))
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, CommentResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: CommentResponse
                ): DataState<ArticleViewState> {

                    return DataState.data(
                        data = ArticleViewState(
                            viewCommentsFields = ArticleViewState.ViewCommentsFields(
                                comment = resultObj
                            )
                        ),
                        response = Response(
                            message = SuccessHandling.SUCCESS_COMMENT_DELETED,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

    override fun deleteComment(
        slug: String,
        id: Int,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(Dispatchers.IO) {
            mainService.deleteComment(slug, id)
        }
        emit(
            object : ApiResponseHandler<ArticleViewState, CommentResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(
                    resultObj: CommentResponse
                ): DataState<ArticleViewState> {

                    return DataState.data(
                        response = Response(
                            message = SuccessHandling.SUCCESS_COMMENT_DELETED,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

}