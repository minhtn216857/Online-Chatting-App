package com.example.minh_messenger_test.ui.chat

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.isWideGamut
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.minh_messenger_test.R
import com.example.minh_messenger_test.data.model.Mesagge
import com.example.minh_messenger_test.databinding.ItemMessageBinding

class ChatAdapter(
    context: Context,
    private val imageUrl: String?,
    private val onPhotoClicked: (photo: Uri) -> Unit

): ListAdapter<Mesagge, ChatAdapter.MessageViewHolder>(DIFF_CALLBACK) {

    private val tint = object {
        val incoming: ColorStateList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.blue_400)
        )
        val outgoing: ColorStateList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.deep_purple_300)
        )
    }

    private val padding = object {
        val vertical: Int = context.resources.getDimensionPixelSize(
            R.dimen.message_padding_vertical
        )
        val horizontalShort: Int = context.resources.getDimensionPixelSize(
            R.dimen.message_padding_horizontal_short
        )
        val horizontalLong: Int = context.resources.getDimensionPixelSize(
            R.dimen.message_padding_horizontal_long
        )
    }

    private val photoSize = context.resources.getDimensionPixelSize(R.dimen.photo_size)

    class MessageViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
    ){
        val binding: ItemMessageBinding = ItemMessageBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val holder = MessageViewHolder(parent)
        holder.binding.textMessage.setOnClickListener{
            val photo = it.getTag(R.id.image_photo) as Uri?
            if(photo != null){
                onPhotoClicked(photo)
            }
        }
        return holder
    }

    // Gán dữ liệu vào ViewHolder khi RecyclerView cần hiển thị một item
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        val layoutParam =holder.binding.textMessage.layoutParams as FrameLayout.LayoutParams

        if(message.isIncoming){
            holder.binding.textMessage.run {
                setBackgroundResource(R.drawable.message_incoming)
                ViewCompat.setBackgroundTintList(this, tint.incoming)

                setPadding(
                    padding.horizontalLong, padding.vertical,
                    padding.horizontalShort, padding.vertical
                )

                layoutParams =layoutParam.apply {
                    gravity = Gravity.START
                }

                Glide.with(holder.binding.imageMessageItem)
                    .load(imageUrl)
                    .circleCrop()
                    .error(R.drawable.one_piece)
                    .into(holder.binding.imageMessageItem)

            }
        }else{
            holder.binding.textMessage.run {
                setBackgroundResource(R.drawable.message_outgoing)
                ViewCompat.setBackgroundTintList(this, tint.outgoing)
                setPadding(
                    padding.horizontalShort, padding.vertical,
                    padding.horizontalLong, padding.vertical
                )
                layoutParams = layoutParam.apply {
                    gravity = Gravity.END
                }
                holder.binding.imageMessageItem.visibility = View.GONE
            }
//

        }

        if(message.data.photoUrl != null){
            val photoUri =message.data.photoUrl

            holder.binding.textMessage.setTag(R.id.tag_photo, photoUri)
            Glide.with(holder.binding.textMessage)
                .load(imageUrl)
                .error(R.drawable.miss)
                .into(ComposeBottomTarget(holder.binding.textMessage, photoSize, photoSize))
        }else{
            holder.binding.textMessage.setTag(R.id.tag_photo, null)
            holder.binding.textMessage.setCompoundDrawables(null, null, null, null)

        }

        holder.binding.textMessage.text = message.data.text
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

}

// Định nghĩa một đối tượng DIFF_CALLBACK sử dụng DiffUtil.ItemCallback
// để tối ưu hóa việc cập nhật danh sách dữ liệu trong RecyclerView
private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Mesagge>() {

    // Hàm kiểm tra xem hai mục (items) trong danh sách có cùng danh tính hay không
    override fun areItemsTheSame(oldItem: Mesagge, newItem: Mesagge): Boolean {
        // So sánh hai đối tượng bằng timestamp để xác định chúng có đại diện cùng một dữ liệu không
        return oldItem.timestamp == newItem.timestamp
    }

    // Hàm kiểm tra xem nội dung của hai mục có giống nhau không
    override fun areContentsTheSame(oldItem: Mesagge, newItem: Mesagge): Boolean {
        // So sánh hai đối tượng bằng tham chiếu (===), tức là chúng trỏ tới cùng một vị trí trong bộ nhớ
        return oldItem == newItem
    }
}

private class ComposeBottomTarget(
    private val view:TextView,
    width: Int,
    height: Int
):CustomTarget<Drawable>(width, height){
    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            null,
            resource
        )
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            null,
            placeholder
        )
    }

}




