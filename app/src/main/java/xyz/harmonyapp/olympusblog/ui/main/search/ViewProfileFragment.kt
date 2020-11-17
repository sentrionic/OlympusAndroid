package xyz.harmonyapp.olympusblog.ui.main.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentViewProfileBinding
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.ui.main.article.ArticleListAdapter
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.setArticle
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.updateProfileArticleListItem
import xyz.harmonyapp.olympusblog.ui.main.search.state.SearchStateEvent.*
import xyz.harmonyapp.olympusblog.utils.StateMessageCallback
import xyz.harmonyapp.olympusblog.utils.SuccessHandling
import xyz.harmonyapp.olympusblog.utils.TopSpacingItemDecoration
import javax.inject.Inject

@MainScope
class ViewProfileFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
) : BaseSearchFragment(viewModelFactory),
    ArticleListAdapter.Interaction {

    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerAdapter: ArticleListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentViewProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        initRecyclerView()
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        uiCommunicationListener.expandAppBar()
        viewModel.setStateEvent(GetAuthorArticlesEvent())
    }

    fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            if (viewState != null) {

                viewState.viewProfileFields.profile?.let { profile ->
                    setProfileProperties(profile)
                }

                recyclerAdapter.apply {
                    viewState.viewProfileFields.articleList?.let {
                        preloadGlideImages(
                            requestManager = requestManager,
                            list = it
                        )
                    }

                    submitList(
                        articleList = viewState.viewProfileFields.articleList,
                        isQueryExhausted = true,
                        isLoading = viewModel.areAnyJobsActive()
                    )
                }
            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer { jobCounter ->
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

            if (stateMessage?.response?.message.equals(SuccessHandling.SUCCESS_TOGGLE_FAVORITE)
                || stateMessage?.response?.message.equals(SuccessHandling.SUCCESS_TOGGLE_BOOKMARK)
            ) {
                viewModel.updateProfileArticleListItem()
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

        binding.profileArticlesRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@ViewProfileFragment.context)
            val topSpacingDecorator = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingDecorator) // does nothing if not applied already
            addItemDecoration(topSpacingDecorator)

            recyclerAdapter = ArticleListAdapter(
                requestManager,
                this@ViewProfileFragment
            )
            adapter = recyclerAdapter

            isNestedScrollingEnabled = false
        }
    }

    private fun setProfileProperties(profile: Author) {

        with(binding) {
            requestManager
                .load(profile.image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(authorImage)

            authorUsername.text = profile.username
            authorBio.text = profile.bio
            authorFollowee.text = "${profile.followee} Following"
            authorFollowers.text = "${profile.followers} Followers"

            if (viewModel.getCurrentUserId() == profile.id) {
                followAuthor.visibility = View.GONE
            }

            if (profile.bio.isBlank()) {
                authorBio.visibility = View.GONE
            }

            if (profile.following) {
                followAuthor.text = "Unfollow"
            } else {
                followAuthor.text = "Follow"
            }

            followAuthor.setOnClickListener {
                viewModel.setStateEvent(ToggleFollowEvent())
            }

            chipArticles.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.setStateEvent(GetAuthorArticlesEvent())
            }

            chipFeed.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) viewModel.setStateEvent(GetAuthorFavoritesEvent())
            }
        }

        (activity as AppCompatActivity).supportActionBar?.title = profile.username
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.profileArticlesRecyclerview.adapter = null
        _binding = null
    }

    override fun onItemSelected(position: Int, item: Article) {
        viewModel.setArticle(item)
        findNavController().navigate(R.id.action_viewProfileFragment_to_viewArticleFragment)
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