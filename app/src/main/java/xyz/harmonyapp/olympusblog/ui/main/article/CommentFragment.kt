package xyz.harmonyapp.olympusblog.ui.main.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.bumptech.glide.RequestManager
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.api.main.responses.CommentResponse
import xyz.harmonyapp.olympusblog.databinding.FragmentCommentBinding
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.ui.AreYouSureCallback
import xyz.harmonyapp.olympusblog.ui.main.article.state.ARTICLE_VIEW_STATE_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.*
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.removeDeletedComment
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.setComment
import xyz.harmonyapp.olympusblog.utils.*
import javax.inject.Inject

@MainScope
class CommentFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
) : BaseArticleFragment(viewModelFactory),
    SwipeRefreshLayout.OnRefreshListener,
    CommentListAdapter.CommentInteraction {

    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerAdapter: CommentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[ARTICLE_VIEW_STATE_BUNDLE_KEY] as ArticleViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.viewState.value

        //clear the list. Don't want to save a large list to bundle.
        viewState?.articleFields?.articleList = ArrayList()
        viewState?.searchFields?.profileList = ArrayList()

        outState.putParcelable(
            ARTICLE_VIEW_STATE_BUNDLE_KEY,
            viewState
        )
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding.swipeRefresh.setOnRefreshListener(this)

        initRecyclerView()
        subscribeObservers()

        binding.addComment.setOnClickListener {
            openAddCommentDialog()
        }
    }

    private fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            if (viewState != null) {
                recyclerAdapter.apply {
                    submitList(
                        commentList = viewState.viewArticleFields.commentList,
                    )
                }

            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer {
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

            if (stateMessage?.response?.message.equals(SuccessHandling.SUCCESS_COMMENT_DELETED)) {
                viewModel.removeDeletedComment()
            }

            stateMessage?.let {
                uiCommunicationListener.onResponseReceived(
                    response = it.response,
                    stateMessageCallback = object : StateMessageCallback {
                        override fun removeMessageFromStack() {
                            viewModel.clearStateMessage()
                        }
                    }
                )
            }
        })
    }

    private fun initRecyclerView() {

        binding.commentRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@CommentFragment.context)
            recyclerAdapter = CommentListAdapter(
                requestManager,
                viewModel.getCurrentUserId(),
                this@CommentFragment
            )
            adapter = recyclerAdapter
        }

    }

    private fun openAddCommentDialog() {

        activity?.let {
            MaterialDialog(it).show {
                input(hint = "Enter your comment", maxLength = 250) { dialog, text ->
                    dialog.dismiss()
                    viewModel.setStateEvent(PostCommentEvent(text.toString()))
                }
                positiveButton(R.string.submit)
            }
        }
    }

    private fun refreshComments() {
        if (!viewModel.isJobAlreadyActive(GetArticleCommentsEvent())) {
            viewModel.setStateEvent(GetArticleCommentsEvent())
            resetUI()
        }
    }

    override fun onDeleteComment(position: Int, item: CommentResponse) {
        val callback: AreYouSureCallback = object : AreYouSureCallback {

            override fun proceed() {
                viewModel.setComment(item)
                viewModel.setStateEvent(DeleteCommentEvent())
            }

            override fun cancel() {
                // ignore
            }

        }
        uiCommunicationListener.onResponseReceived(
            response = Response(
                message = getString(R.string.are_you_sure_delete),
                uiComponentType = UIComponentType.AreYouSureDialog(callback),
                messageType = MessageType.Info()
            ),
            stateMessageCallback = object : StateMessageCallback {
                override fun removeMessageFromStack() {
                    viewModel.clearStateMessage()
                }
            }
        )
    }

    private fun resetUI() {
        binding.commentRecyclerview.smoothScrollToPosition(0)
        uiCommunicationListener.hideSoftKeyboard()
        binding.focusableView.requestFocus()
    }


    override fun onRefresh() {
        refreshComments()
        binding.swipeRefresh.isRefreshing = false
    }


    override fun onResume() {
        super.onResume()
        viewModel.setStateEvent(GetArticleCommentsEvent())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.commentRecyclerview.adapter = null
        _binding = null
    }
}