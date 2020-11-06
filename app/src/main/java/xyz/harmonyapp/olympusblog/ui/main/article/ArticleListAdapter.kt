package xyz.harmonyapp.olympusblog.ui.main.article

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import xyz.harmonyapp.olympusblog.databinding.LayoutArticleListItemBinding
import xyz.harmonyapp.olympusblog.databinding.LayoutNoMoreResultsBinding
import xyz.harmonyapp.olympusblog.models.Article
import xyz.harmonyapp.olympusblog.utils.DateUtils
import xyz.harmonyapp.olympusblog.utils.GenericViewHolder

class ArticleListAdapter(
    private val requestManager: RequestManager,
    private val interaction: Interaction? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String = "AppDebug"
    private val NO_MORE_RESULTS = -1
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
        "",
        "",
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

    fun submitList(articleList: List<Article>?, isQueryExhausted: Boolean) {
        val newList = articleList?.toMutableList()
        if (isQueryExhausted)
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
            articleAuthor.text = item.username

            if (item.favorited) {
                articleFavorited.visibility = View.VISIBLE
                articleNotFavorited.visibility = View.GONE
            } else {
                articleFavorited.visibility = View.GONE
                articleNotFavorited.visibility = View.VISIBLE
            }

            requestManager
                .load(item.profileImage)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(profilePhoto)
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Article)
        fun restoreListPosition()
    }
}