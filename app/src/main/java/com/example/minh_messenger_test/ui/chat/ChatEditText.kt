package com.example.minh_messenger_test.ui.chat
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.ViewCompat

// Định nghĩa một kiểu alias để tạo hàm callback khi ảnh được thêm
typealias OnImageAddedListener = (contentUri: Uri, mimeType: String, label: String) -> Unit

// Mảng định nghĩa các MIME types (định dạng tệp) được hỗ trợ
private val SupportMimeType = arrayOf(
    "image/jpg", "image/png", "image/gif" // Các định dạng ảnh được hỗ trợ
)

// Định nghĩa lớp ChatEditText, mở rộng từ AppCompatEditText
// Lớp này cho phép chỉnh sửa văn bản với khả năng nhận ảnh từ hệ thống
class ChatEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle // Sử dụng style mặc định của EditText
) : AppCompatEditText(context, attrs, defStyleAttr) {

    // Biến lưu callback được thiết lập để xử lý sự kiện khi ảnh được thêm
    private var onImageAddedListener: OnImageAddedListener? = null

    // Khối `init` được chạy khi lớp được khởi tạo
    init {
        // Đặt listener nhận nội dung (ảnh) vào View thông qua ViewCompat
        ViewCompat.setOnReceiveContentListener(this, SupportMimeType) { _, payload ->
            // Phân tách payload thành phần có nội dung hợp lệ và phần còn lại
            val (content, remaining) = payload.partition { it.uri != null }

            if (content != null) { // Nếu có nội dung hợp lệ
                val clip = content.clip // Lấy clip dữ liệu từ payload
                val mimeType = SupportMimeType.find { // Tìm MIME type hợp lệ trong mảng hỗ trợ
                    clip.description.hasMimeType(it)
                }

                // Nếu MIME type hợp lệ và clip có ít nhất một mục
                if (mimeType != null && clip.itemCount > 0) {
                    // Gọi callback để xử lý nội dung ảnh được thêm
                    onImageAddedListener?.invoke(
                        clip.getItemAt(0).uri, // URI của ảnh
                        mimeType,              // MIME type của ảnh
                        clip.description.label.toString() // Nhãn của clip
                    )
                }
            }
            remaining // Trả về phần nội dung còn lại chưa xử lý
        }
    }

    // Hàm public để thiết lập listener cho sự kiện thêm ảnh
    fun setOnImageAddedListener(listener: OnImageAddedListener?) {
        onImageAddedListener = listener // Lưu listener vào biến của lớp
    }
}
