package xyz.harmonyapp.olympusblog.ui.main.search

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import xyz.harmonyapp.olympusblog.databinding.LayoutNoMoreResultsBinding
import xyz.harmonyapp.olympusblog.databinding.LayoutProfileListItemBinding
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.utils.GenericViewHolder

class ProfileListAdapter(
    private val requestManager: RequestManager,
    private val interaction: Interaction? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String = "AppDebug"
    private val NO_MORE_RESULTS = -1
    private val PROFILE_ITEM = 0
    private val NO_MORE_RESULTS_PROFILES_MARKER = Author(
        NO_MORE_RESULTS,
        "",
        "",
        "",
        false,
        0,
        0,
    )

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Author>() {

        override fun areItemsTheSame(oldItem: Author, newItem: Author): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Author, newItem: Author): Boolean {
            return oldItem == newItem
        }

    }
    private val differ =
        AsyncListDiffer(
            ArticleRecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
        )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {

            NO_MORE_RESULTS -> {
                Log.e(TAG, "onCreateViewHolder: No more results...")
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutNoMoreResultsBinding.inflate(inflater, parent, false)
                return GenericViewHolder(
                    binding = binding,
                )
            }

            PROFILE_ITEM -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutProfileListItemBinding.inflate(inflater, parent, false)
                return ProfileViewHolder(
                    binding = binding,
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
            else -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutProfileListItemBinding.inflate(inflater, parent, false)
                return ProfileViewHolder(
                    binding = binding,
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
        }
    }

    internal inner class ArticleRecyclerChangeCallback(
        private val adapter: ProfileListAdapter
    ) : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            adapter.notifyItemRangeChanged(position, count, payload)
        }

        override fun onInserted(position: Int, count: Int) {
            adapter.notifyItemRangeChanged(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            adapter.notifyDataSetChanged()
        }

        override fun onRemoved(position: Int, count: Int) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProfileViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].id > -1) {
            return PROFILE_ITEM
        }
        return differ.currentList[position].id
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    // Prepare the images that will be displayed in the RecyclerView.
    // This also ensures if the network connection is lost, they will be in the cache
    fun preloadGlideImages(
        requestManager: RequestManager,
        list: List<Author>
    ) {
        for (author in list) {
            requestManager
                .load(author.image)
                .preload()
        }
    }

    fun submitList(profileList: List<Author>?) {
        val newList = profileList?.toMutableList()
        if (newList?.size == 0) {
            newList.add(NO_MORE_RESULTS_PROFILES_MARKER)
        }
        differ.submitList(newList)
    }

    class ProfileViewHolder
    constructor(
        val binding: LayoutProfileListItemBinding,
        val requestManager: RequestManager,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Author) = with(binding) {
            itemView.setOnClickListener {
                interaction?.onProfileSelected(bindingAdapterPosition, item)
            }

            requestManager
                .load(item.image)
                .into(authorImage)

            authorUsername.text = item.username
            authorBio.text = item.bio
        }
    }

    interface Interaction {
        fun onProfileSelected(position: Int, item: Author)
    }
}