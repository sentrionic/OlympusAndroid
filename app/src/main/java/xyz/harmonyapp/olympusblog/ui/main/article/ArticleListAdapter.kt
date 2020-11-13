package xyz.harmonyapp.olympusblog.ui.main.article

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import xyz.harmonyapp.olympusblog.R
import xyz.harmonyapp.olympusblog.databinding.LayoutArticleListItemBinding
import xyz.harmonyapp.olympusblog.databinding.LayoutFilterOptionsBinding
import xyz.harmonyapp.olympusblog.databinding.LayoutNoMoreResultsBinding
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.models.ArticleAuthor
import xyz.harmonyapp.olympusblog.ui.main.article.viewmodel.getDummyAuthor
import xyz.harmonyapp.olympusblog.ui.main.profile.state.ProfileStateEvent
import xyz.harmonyapp.olympusblog.utils.DateUtils
import xyz.harmonyapp.olympusblog.utils.GenericViewHolder

class ArticleListAdapter(
    private val requestManager: RequestManager,
    private val interaction: Interaction? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String = "AppDebug"
    private val NO_MORE_RESULTS = -2
    private val CHIP_ITEM = -1
    private val ARTICLE_ITEM = 0
    private val NO_MORE_RESULTS_ARTICLE_MARKER = Article(
        NO_MORE_RESULTS,
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        false,
        false,
        emptyList(),
        getDummyAuthor()
    )

    private val ARTICLE_CHIP_LIST = Article(
        CHIP_ITEM,
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        false,
        false,
        emptyList(),
        getDummyAuthor()
    )

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {

        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
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

            CHIP_ITEM -> {
                Log.e(TAG, "onCreateViewHolder: No more results...")
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutFilterOptionsBinding.inflate(inflater, parent, false)
                return ChipViewHolder(
                    binding = binding,
                    interaction = interaction
                )
            }

            ARTICLE_ITEM -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutArticleListItemBinding.inflate(inflater, parent, false)
                return ArticleViewHolder(
                    binding = binding,
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
            else -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutArticleListItemBinding.inflate(inflater, parent, false)
                return ArticleViewHolder(
                    binding = binding,
                    interaction = interaction,
                    requestManager = requestManager
                )
            }
        }
    }

    internal inner class ArticleRecyclerChangeCallback(
        private val adapter: ArticleListAdapter
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
            is ArticleViewHolder -> {
                holder.bind(differ.currentList[position])
            }
            is ChipViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].id > -1) {
            return ARTICLE_ITEM
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
        list: List<Article>
    ) {
        for (article in list) {
            requestManager
                .load(article.image)
                .preload()
        }
    }

    fun submitList(articleList: List<Article>?, isQueryExhausted: Boolean, isLoading: Boolean, isHome: Boolean = false) {
        val newList = articleList?.toMutableList()
        if (isHome) {
            newList?.add(0, ARTICLE_CHIP_LIST)
        }
        if (isQueryExhausted && !isLoading)
            newList?.add(NO_MORE_RESULTS_ARTICLE_MARKER)
        val commitCallback = Runnable {
            interaction?.restoreListPosition()
        }
        differ.submitList(newList, commitCallback)
    }

    class ArticleViewHolder
    constructor(
        val binding: LayoutArticleListItemBinding,
        val requestManager: RequestManager,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Article) = with(binding) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            requestManager
                .load(item.image)
                .transition(withCrossFade())
                .into(articleImage)

            articleTitle.text = item.title
            articleDescription.text = item.description
            articleCreatedAt.text = DateUtils.formatDate(item.createdAt)
            articleFavoritesCount.text = item.favoritesCount.toString()
            articleAuthor.text = item.author.username

            if (item.favorited) {
                articleFavorited.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_baseline_star_24
                    )
                )
            } else {
                articleFavorited.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_outline_star_outline_24
                    )
                )
            }

            if (item.bookmarked) {
                articleBookmark.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_baseline_bookmark_24
                    )
                )
            } else {
                articleBookmark.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_baseline_bookmark_border_24
                    )
                )
            }

            articleFavorited.setOnClickListener {
                interaction?.toggleFavorite(adapterPosition, item)
            }

            articleBookmark.setOnClickListener {
                interaction?.toggleBookmark(adapterPosition, item)
            }

            requestManager
                .load(item.author.image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(profilePhoto)
        }
    }

    class ChipViewHolder
    constructor(
        val binding: LayoutFilterOptionsBinding,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Article) = with(binding) {
            chipArticles.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) interaction?.onChipSelected(0)
            }

            chipFavorites.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) interaction?.onChipSelected(1)
            }

            chipBookmarked.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) interaction?.onChipSelected(2)
            }
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Article)
        fun restoreListPosition()
        fun toggleFavorite(position: Int, item: Article)
        fun toggleBookmark(position: Int, item: Article)
        fun onChipSelected(index: Int)
    }
}