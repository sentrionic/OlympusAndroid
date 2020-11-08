package xyz.harmonyapp.olympusblog.ui.main.article

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bumptech.glide.RequestManager
import xyz.harmonyapp.olympusblog.api.main.responses.CommentResponse
import xyz.harmonyapp.olympusblog.databinding.LayoutCommentListItemBinding
import xyz.harmonyapp.olympusblog.databinding.LayoutNoCommentsBinding
import xyz.harmonyapp.olympusblog.models.Author
import xyz.harmonyapp.olympusblog.utils.DateUtils
import xyz.harmonyapp.olympusblog.utils.GenericViewHolder

class CommentListAdapter(
    private val requestManager: RequestManager,
    private val userId: Int,
    private val interaction: CommentInteraction? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAG: String = "AppDebug"
    private val NO_MORE_RESULTS = -1
    private val COMMENT_ITEM = 0
    private val NO_MORE_RESULTS_COMMENT_MARKER = CommentResponse(
        NO_MORE_RESULTS,
        "",
        "",
        Author(-1, "", "", "", false, 0, 0)
    )

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CommentResponse>() {

        override fun areItemsTheSame(oldItem: CommentResponse, newItem: CommentResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CommentResponse,
            newItem: CommentResponse
        ): Boolean {
            return oldItem == newItem
        }

    }
    private val differ =
        AsyncListDiffer(
            CommentRecyclerChangeCallback(this),
            AsyncDifferConfig.Builder(DIFF_CALLBACK).build()
        )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {

            NO_MORE_RESULTS -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutNoCommentsBinding.inflate(inflater, parent, false)
                return GenericViewHolder(
                    binding = binding,
                )
            }

            COMMENT_ITEM -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutCommentListItemBinding.inflate(inflater, parent, false)
                return CommentViewHolder(
                    binding = binding,
                    interaction = interaction,
                    requestManager = requestManager,
                    userId = userId
                )
            }
            else -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = LayoutCommentListItemBinding.inflate(inflater, parent, false)
                return CommentViewHolder(
                    binding = binding,
                    interaction = interaction,
                    requestManager = requestManager,
                    userId = userId
                )
            }
        }
    }

    internal inner class CommentRecyclerChangeCallback(
        private val adapter: CommentListAdapter
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
            is CommentViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (differ.currentList[position].id > -1) {
            return COMMENT_ITEM
        }
        return differ.currentList[position].id
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(commentList: List<CommentResponse>?) {
        val newList = commentList?.toMutableList()
        if (newList?.size == 0)
            newList.add(NO_MORE_RESULTS_COMMENT_MARKER)
        differ.submitList(newList)
    }

    class CommentViewHolder
    constructor(
        val binding: LayoutCommentListItemBinding,
        val requestManager: RequestManager,
        val userId: Int,
        private val interaction: CommentInteraction?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CommentResponse) = with(binding) {
            requestManager
                .load(item.author.image)
                .into(commentProfileImage)

            commentUsername.text = item.author.username
            comment.text = item.body
            commentCreatedAt.text = DateUtils.formatDate(item.createdAt)

            if (userId == item.author.id) {
                deleteComment.visibility = View.VISIBLE
            }

            deleteComment.setOnClickListener {
                interaction?.onDeleteComment(adapterPosition, item)
            }
        }
    }

    interface CommentInteraction {
        fun onDeleteComment(position: Int, item: CommentResponse)
    }
}