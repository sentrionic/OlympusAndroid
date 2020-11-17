package xyz.harmonyapp.olympusblog.ui.main.article

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentArticleBinding
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.ui.main.article.state.ARTICLE_VIEW_STATE_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.*
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.*
import xyz.harmonyapp.olympusblog.utils.StateMessageCallback
import xyz.harmonyapp.olympusblog.utils.SuccessHandling
import xyz.harmonyapp.olympusblog.utils.TopSpacingItemDecoration
import javax.inject.Inject

@MainScope
class ArticleFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestOptions: RequestOptions
) : BaseArticleFragment(viewModelFactory),
    ArticleListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerAdapter: ArticleListAdapter
    private var requestManager: RequestManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore state after process death
        if (savedInstanceState != null) {
            savedInstanceState.let { inState ->
                Log.d(TAG, "onCreate: savedInstanceState")
                (inState[ARTICLE_VIEW_STATE_BUNDLE_KEY] as ArticleViewState?)?.let { viewState ->
                    viewModel.setViewState(viewState)
                }
            }
        } else if (isConnectedToTheInternet()) {
            viewModel.setStateEvent(CleanDBEvent())
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
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.swipeRefresh.setOnRefreshListener(this)
        setupGlide()
        initRecyclerView()
        subscribeObservers()
        setupTabs()
    }

    private fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            if (viewState != null) {
                recyclerAdapter.apply {
                    viewState.articleFields.articleList?.let {
                        preloadGlideImages(
                            requestManager = requestManager as RequestManager,
                            list = it
                        )
                    }

                    submitList(
                        articleList = viewState.articleFields.articleList,
                        isQueryExhausted = viewState.articleFields.isQueryExhausted ?: true,
                        isLoading = viewModel.areAnyJobsActive(),
                    )
                }
            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer {
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

            if (stateMessage?.response?.message.equals(SuccessHandling.SUCCESS_TOGGLE_FAVORITE)
                || stateMessage?.response?.message.equals(SuccessHandling.SUCCESS_TOGGLE_BOOKMARK)
            ) {
                viewModel.updateListItem()
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

        with(binding) {
            articleRecyclerview.apply {
                layoutManager = LinearLayoutManager(this@ArticleFragment.context)
                val topSpacingDecorator = TopSpacingItemDecoration(30)
                removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
                addItemDecoration(topSpacingDecorator)

                recyclerAdapter = ArticleListAdapter(
                    requestManager as RequestManager,
                    this@ArticleFragment
                )
                addOnScrollListener(object : RecyclerView.OnScrollListener() {

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val lastPosition = layoutManager.findLastVisibleItemPosition()
                        if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                            viewModel.nextPage()
                        }
                    }
                })
                adapter = recyclerAdapter
            }

            scrollUpButton.setOnClickListener {
                articleRecyclerview.layoutManager?.smoothScrollToPosition(
                    articleRecyclerview,
                    null,
                    0
                )
            }
        }
    }

    private fun onArticleSearchOrFilter() {
        if (binding.chipArticles.isChecked) {
            viewModel.loadFirstPage().let {
                resetUI()
            }
        }

        if (binding.chipFeed.isChecked) {
            viewModel.loadFirstFeedPage().let {
                resetUI()
            }
        }

        if (binding.chipBookmarked.isChecked) {
            viewModel.loadFirstBookmarkPage().let {
                resetUI()
            }
        }

    }

    private fun resetUI() {
        binding.articleRecyclerview.smoothScrollToPosition(0)
        uiCommunicationListener.hideSoftKeyboard()
        binding.focusableView.requestFocus()
    }

    override fun onItemSelected(position: Int, item: Article) {
        viewModel.setArticle(item)
        findNavController().navigate(R.id.action_articleFragment_to_viewArticleFragment)
    }

    override fun toggleFavorite(position: Int, item: Article) {
        viewModel.setArticle(item)
        viewModel.setStateEvent(ToggleFavoriteEvent())
    }

    override fun toggleBookmark(position: Int, item: Article) {
        viewModel.setArticle(item)
        viewModel.setStateEvent(ToggleBookmarkEvent())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.create_menu, menu)
    }

    override fun onRefresh() {
        onArticleSearchOrFilter()
        binding.swipeRefresh.isRefreshing = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_create -> {
                findNavController().navigate(R.id.action_articleFragment_to_createArticleFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun restoreListPosition() {
        viewModel.viewState.value?.articleFields?.layoutManagerState?.let { lmState ->
            if (_binding != null) {
                binding.articleRecyclerview.layoutManager?.onRestoreInstanceState(lmState)
            }
        }
    }

    private fun setupTabs() {
        with(binding) {
            chipArticles.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.loadFirstPage().let {
                        resetUI()
                    }
                }
            }

            chipFeed.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.loadFirstFeedPage().let {
                        resetUI()
                    }
                }
            }

            chipBookmarked.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.loadFirstBookmarkPage().let {
                        resetUI()
                    }
                }
            }
        }
    }

    private fun setupGlide() {
        activity?.let {
            requestManager = Glide.with(it)
                .applyDefaultRequestOptions(requestOptions)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshFromCache()
        (activity as AppCompatActivity).supportActionBar?.title = "OlympusBlog"
    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
    }

    private fun saveLayoutManagerState() {
        binding.articleRecyclerview.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.articleRecyclerview.adapter = null
        requestManager = null
        _binding = null
    }

    private fun isConnectedToTheInternet(): Boolean {
        (requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }
}