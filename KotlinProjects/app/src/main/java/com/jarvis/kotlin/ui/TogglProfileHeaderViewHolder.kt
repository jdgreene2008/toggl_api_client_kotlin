import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.jarvis.kotlin.R

class TogglProfileHeaderViewHolder : RecyclerView.ViewHolder {
    val fullname: TextView
    val email: TextView
    val todaysDate: TextView
    val blogPost: TextView


    constructor(view: View) : super(view) {
        fullname = view.findViewById(R.id.user_fullname)
        email = view.findViewById(R.id.user_email)
        todaysDate = view.findViewById(R.id.todays_date)
        blogPost = view.findViewById(R.id.blog_post)
    }
}
