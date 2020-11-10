package xyz.harmonyapp.olympusblog.ui.main.profile

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.noties.markwon.Markwon
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.FragmentViewArticleBinding
import xyz.harmonyapp.olympusblog.databinding.FragmentViewProfileBinding
import xyz.harmonyapp.olympusblog.di.main.MainScope
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.ArticleAuthor
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.ui.AreYouSureCallback
import xyz.harmonyapp.olympusblog.ui.main.article.state.ARTICLE_VIEW_STATE_BUNDLE_KEY
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleStateEvent.*
import xyz.harmonyapp.olympusblog.ui.main.article.state.ArticleViewState
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.*
import xyz.harmonyapp.olympusblog.ui.main.profile.BaseProfileFragment
import xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileStateEvent
import xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileStateEvent.ToggleFollowEvent
import xyz.harmonyapp.olympusblog.utils.*
import xyz.harmonyapp.olympusblog.utils.SuccessHandling.Companion.SUCCESS_ARTICLE_DELETED
import javax.inject.Inject

@MainScope
class ViewProfileFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager,
) : BaseProfileFragment(viewModelFactory) {

    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!

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
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        uiCommunicationListener.expandAppBar()
    }

    fun subscribeObservers() {

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.viewProfileFields.profile?.let { profile ->
                setProfileProperties(profile)
            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer { jobCounter ->
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

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

            if (profile.following) {
                followAuthor.text = "Unfollow"
            } else {
                followAuthor.text = "Follow"
            }

            followAuthor.setOnClickListener {
                viewModel.setStateEvent(ToggleFollowEvent())
            }

        }

        (activity as AppCompatActivity).supportActionBar?.title = profile.username
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}