package xyz.harmonyapp.olympusblog.ui.main.search

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentSearchBinding
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.persistence.ArticleQueryUtils
import xyz.harmonyapp.olympusblog.ui.main.article.ArticleListAdapter
import xyz.harmonyapp.olympusblog.ui.main.article.ArticleListAdapter.Interaction
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.*
import xyz.harmonyapp.olympusblog.utils.StateMessageCallback
import xyz.harmonyapp.olympusblog.utils.SuccessHandling
import xyz.harmonyapp.olympusblog.utils.TopSpacingItemDecoration
import javax.inject.Inject

@MainScope
class SearchFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestOptions: RequestOptions
) : BaseSearchFragment(viewModelFactory),
    ProfileListAdapter.Interaction,
    Interaction,
    SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var articleListAdapter: ArticleListAdapter
    private lateinit var profileListAdapter: ProfileListAdapter

    private lateinit var searchView: SearchView
    private var requestManager: RequestManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.swipeRefresh.setOnRefreshListener(this)

        setupGlide()
        initRecyclerView()
        subscribeObservers()
        setupTabs()
    }

    private fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            if (viewState != null) {
                if (viewModel.getSearchQuery().length > 2) {
                    profileListAdapter.apply {
                        viewState.searchFields.profileList?.let {
                            preloadGlideImages(
                                requestManager = requestManager as RequestManager,
                                list = it
                            )
                        }

                        submitList(
                            profileList = viewState.searchFields.profileList,
                        )
                    }

                    articleListAdapter.apply {
                        viewState.articleFields.articleList?.let {
                            preloadGlideImages(
                                requestManager = requestManager as RequestManager,
                                list = it
                            )
                        }

                        submitList(
                            articleList = viewState.articleFields.articleList,
                            isQueryExhausted = viewState.searchFields.isQueryExhausted ?: true,
                            isLoading = viewModel.areAnyJobsActive()
                        )
                    }
                } else {
                    articleListAdapter.displayInfoText()
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

        binding.searchRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@SearchFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            profileListAdapter = ProfileListAdapter(
                requestManager as RequestManager,
                this@SearchFragment
            )

            articleListAdapter = ArticleListAdapter(
                requestManager as RequestManager,
                this@SearchFragment
            )
            adapter = articleListAdapter
        }

    }

    private fun setupTabs() {
        with(binding) {
            chipArticles.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    searchRecyclerview.adapter = articleListAdapter
                    viewModel.loadFirstSearchPage().let {
                        resetUI()
                    }
                }
            }

            chipProfiles.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    searchRecyclerview.adapter = profileListAdapter
                    viewModel.loadProfiles().let {
                        resetUI()
                    }
                }
            }

            chipTag.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    searchRecyclerview.adapter = articleListAdapter
                    viewModel.loadByTags().let {
                        resetUI()
                    }
                }
            }
        }
    }

    private fun initSearchView(menu: Menu) {
        activity?.apply {
            val searchManager: SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(false)
            searchView.isSubmitButtonEnabled = true
            searchView.queryHint = "Search..."
        }

        // ENTER ON COMPUTER KEYBOARD OR ARROW ON VIRTUAL KEYBOARD
        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { v, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                || actionId == EditorInfo.IME_ACTION_SEARCH
            ) {
                val searchQuery = v.text.toString()
                Log.e(
                    TAG,
                    "SearchView: (keyboard or arrow) executing search...: ${searchQuery}"
                )
                viewModel.setQuery(searchQuery).let {
                    initSearch()
                }
            }
            true
        }

        // SEARCH BUTTON CLICKED (in toolbar)
        val searchButton = searchView.findViewById(R.id.search_go_btn) as View
        searchButton.setOnClickListener {
            val searchQuery = searchPlate.text.toString()
            Log.e(TAG, "SearchView: (button) executing search...: ${searchQuery}")
            viewModel.setQuery(searchQuery).let {
                initSearch()
            }
        }
    }

    private fun showFilterDialog() {

        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_article_filter)

            val view = dialog.getCustomView()

            var order = viewModel.getOrder()

            when (order) {
                ArticleQueryUtils.ARTICLES_DESC -> {
                    view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_desc)
                }
                ArticleQueryUtils.ARTICLES_ASC -> {
                    view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_asc)
                }
                else -> {
                    view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_top)
                }
            }

            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: apply filter.")

                val selectedOrder = dialog.getCustomView().findViewById<RadioButton>(
                    dialog.getCustomView()
                        .findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId
                )

                order = selectedOrder.text.toString()

                viewModel.saveFilterOptions(order).let {
                    viewModel.setArticleOrder(order)
                    initSearch()
                }
                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: cancelling filter.")
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun initSearch() {
        with(binding) {
            if (chipArticles.isChecked) {
                searchRecyclerview.adapter = articleListAdapter
                viewModel.loadFirstSearchPage().let {
                    resetUI()
                }
            }

            if (chipProfiles.isChecked) {
                searchRecyclerview.adapter = profileListAdapter
                viewModel.loadProfiles().let {
                    resetUI()
                }
            }

            if (chipTag.isChecked) {
                searchRecyclerview.adapter = articleListAdapter
                viewModel.loadByTags().let {
                    resetUI()
                }
            }
        }
    }

    private fun resetUI() {
        binding.searchRecyclerview.smoothScrollToPosition(0)
        uiCommunicationListener.hideSoftKeyboard()
        binding.focusableView.requestFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_filter_settings -> {
                showFilterDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onProfileSelected(position: Int, item: Author) {
        viewModel.setProfile(item)
        findNavController().navigate(R.id.action_searchFragment_to_viewProfileFragment)
    }


    override fun onRefresh() {
        initSearch()
        binding.swipeRefresh.isRefreshing = false
    }

    private fun setupGlide() {
        activity?.let {
            requestManager = Glide.with(it)
                .applyDefaultRequestOptions(requestOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchRecyclerview.adapter = null
        requestManager = null
        _binding = null
    }

    override fun onItemSelected(position: Int, item: Article) {
        viewModel.setArticle(item)
        findNavController().navigate(R.id.action_searchFragment_to_viewArticleFragment)
    }

    override fun toggleFavorite(position: Int, item: Article) {
        viewModel.setArticle(item)
        viewModel.setStateEvent(ArticleStateEvent.ToggleFavoriteEvent())
    }

    override fun toggleBookmark(position: Int, item: Article) {
        viewModel.setArticle(item)
        viewModel.setStateEvent(ArticleStateEvent.ToggleBookmarkEvent())
    }

    override fun restoreListPosition() {
        return
    }
}