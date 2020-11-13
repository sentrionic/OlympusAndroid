package xyz.harmonyapp.olympusblog.ui.main.article

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.marginEnd
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.chip.Chip
import io.noties.markwon.Markwon
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentViewArticleBinding
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.ui.AreYouSureCallback
import xyz.harmonyapp.olympusblog.ui.main.article.state.ARTICLE_VIEW_STATE_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.*
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.*
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_ARTICLE_DELETED
import javax.inject.Inject

@MainScope
class ViewArticleFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
    private val markwon: Markwon
) : BaseArticleFragment(viewModelFactory) {

    private var _binding: FragmentViewArticleBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentViewArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        checkIsAuthor()
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        uiCommunicationListener.expandAppBar()
    }

    fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.viewArticleFields.article?.let { article ->
                setArticleProperties(article)
            }

            if (viewState.viewArticleFields.isAuthorOfArticle == true) {
                adaptViewToAuthorMode()
            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer { jobCounter ->
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

            if (stateMessage?.response?.message.equals(SUCCESS_ARTICLE_DELETED)) {
                viewModel.removeDeletedArticle()
                findNavController().popBackStack()
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

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
    }

    private fun setArticleProperties(article: Article) {

        with(binding) {
            requestManager
                .load(article.image)
                .into(articleImage)

            requestManager
                .load(article.author.image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(profilePhoto)

            requestManager
                .load(article.author.image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(authorImage)

            articleTitle.text = article.title
            articleUsername.text = article.author.username
            articleDescription.text = article.description
            articleCreatedAt.text = DateUtils.formatDate(article.createdAt)
            articleFavoritesCount.text = article.favoritesCount.toString()

            authorUsername.text = article.author.username
            authorBio.text = article.author.bio

            markwon.setMarkdown(articleBody, article.body)

            tagsLayout.removeAllViews()

            article.tagList.forEachIndexed { i, tag ->
                val chip = Chip(requireContext()).apply {
                    text = tag
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = 20
                    }
                }
                tagsLayout.addView(chip)
            }

            if (article.favorited) {
                articleFavorited.setImageDrawable(
                    ContextCompat.getDrawable(
                        root.context,
                        R.drawable.ic_baseline_star_24
                    )
                )
            } else {
                articleFavorited.setImageDrawable(
                    ContextCompat.getDrawable(
                        root.context,
                        R.drawable.ic_outline_star_outline_24
                    )
                )
            }

            articleFavorited.setOnClickListener {
                viewModel.setStateEvent(ToggleFavoriteEvent())
            }

            articleBookmark.setOnClickListener {
                viewModel.setStateEvent(ToggleBookmarkEvent())
            }

            articleComments.setOnClickListener {
                findNavController().navigate(R.id.action_viewArticleFragment_to_commentFragment)
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
                viewModel.getArticle().tagList.joinToString(),
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