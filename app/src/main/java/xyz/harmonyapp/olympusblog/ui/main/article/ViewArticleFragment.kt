package xyz.harmonyapp.olympusblog.ui.main.article

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.noties.markwon.Markwon
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentViewArticleBinding
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.ui.AreYouSureCallback
import xyz.harmonyapp.olympusblog.ui.UIMessage
import xyz.harmonyapp.olympusblog.ui.UIMessageType
import xyz.harmonyapp.olympusblog.ui.main.article.state.ARTICLE_VIEW_STATE_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.CheckAuthorOfArticle
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.DeleteArticleEvent
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.*
import xyz.harmonyapp.olympusblog.utils.DateUtils
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_ARTICLE_DELETED
import javax.inject.Inject

@MainScope
class ViewArticleFragment
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val markwon: Markwon
) : BaseArticleFragment() {

    private var _binding: FragmentViewArticleBinding? = null
    private val binding get() = _binding!!

    val viewModel: ArticleViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cancelActiveJobs()
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

        outState.putParcelable(
            ARTICLE_VIEW_STATE_BUNDLE_KEY,
            viewState
        )
        super.onSaveInstanceState(outState)
    }

    override fun cancelActiveJobs() {
        viewModel.cancelActiveJobs()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentViewArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        checkIsAuthor()
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        stateChangeListener.expandAppBar()
    }

    fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, { dataState ->
            if (dataState != null) {
                stateChangeListener.onDataStateChange(dataState)

                dataState.data?.let { data ->
                    data.data?.getContentIfNotHandled()?.let { viewState ->
                        viewModel.setIsAuthorOfArticle(
                            viewState.viewArticleFields.isAuthorOfArticle
                        )
                    }
                    data.response?.peekContent()?.let { response ->
                        if (response.message.equals(SUCCESS_ARTICLE_DELETED)) {
                            viewModel.removeDeletedArticle()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, { viewState ->
            viewState.viewArticleFields.article?.let { article ->
                setArticleProperties(article)
            }

            if (viewState.viewArticleFields.isAuthorOfArticle) {
                adaptViewToAuthorMode()
            }
        })
    }

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
    }

    private fun setArticleProperties(article: Article) {

        with(binding) {
            requestManager
                .load(article.image)
                .into(articleImage)

            requestManager
                .load(article.profileImage)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(profilePhoto)

            articleTitle.text = article.title
            articleUsername.text = article.username
            articleDescription.text = article.description
            articleCreatedAt.text = DateUtils.formatDate(article.createdAt)
            articleFavoritesCount.text = article.favoritesCount.toString()

            markwon.setMarkdown(binding.articleBody, article.body)

            if (article.favorited) {
                articleFavorited.visibility = View.VISIBLE
                articleNotFavorited.visibility = View.GONE
            } else {
                articleFavorited.visibility = View.GONE
                articleNotFavorited.visibility = View.VISIBLE
            }
        }

        (activity as AppCompatActivity).supportActionBar?.title = article.title
    }

    private fun checkIsAuthor() {
        viewModel.setIsAuthorOfArticle(false)
        viewModel.setStateEvent(CheckAuthorOfArticle())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (viewModel.isAuthor()) {
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel.isAuthor()) {
            when (item.itemId) {
                R.id.edit -> {
                    navUpdateArticleFragment()
                    return true
                }
                R.id.delete -> {
                    confirmDeleteRequest()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navUpdateArticleFragment() {
        try {
            // prep for next fragment
            viewModel.setUpdatedArticleFields(
                viewModel.getArticle().title,
                viewModel.getArticle().description,
                viewModel.getArticle().body,
                viewModel.getArticle().image.toUri()
            )
            findNavController().navigate(R.id.action_viewArticleFragment_to_updateArticleFragment)
        } catch (e: Exception) {
            // send error report or something. These fields should never be null. Not possible
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    private fun confirmDeleteRequest() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {

            override fun proceed() {
                deleteArticle()
            }

            override fun cancel() {
                // ignore
            }

        }
        uiCommunicationListener.onUIMessageReceived(
            UIMessage(
                getString(R.string.are_you_sure_delete),
                UIMessageType.AreYouSureDialog(callback)
            )
        )
    }

    fun deleteArticle() {
        viewModel.setStateEvent(
            DeleteArticleEvent()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}