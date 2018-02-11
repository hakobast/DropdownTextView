package hakobastvatsatryan.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import hakobastvatsatryan.dropdowntextview.DropdownTextView

typealias lp = LinearLayout.LayoutParams

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val firstDropdownTextView: DropdownTextView = findViewById(R.id.first_dropdown_text_view)

		val secondDropdownTextView: DropdownTextView = findViewById(R.id.second_dropdown_text_view)
		secondDropdownTextView.setTitleText(R.string.dropdown_title_text)
		secondDropdownTextView.setContentText(R.string.dropdown_content_text)

		val thirdDropdownTextViewSecond = DropdownTextView.Builder(this)
				.setTitleTextColorRes(R.color.third_drop_down_text_view_title)
				.setContentTextColorRes(R.color.third_drop_down_text_view_content)
				.setRegularBackgroundDrawableRes(R.drawable.bg_third_dropdown_text_view_regular)
				.setExpandedBackgroundDrawableRes(R.drawable.bg_third_dropdown_text_view_expanded)
				.setTitleTextRes(R.string.dropdown_title_text)
				.setContentTextRes(R.string.dropdown_content_text)
				.build()

		val root: ViewGroup = findViewById(R.id.root_view)
		root.addView(thirdDropdownTextViewSecond, lp(lp.MATCH_PARENT, lp.WRAP_CONTENT).apply {
			resources.getDimension(R.dimen.margin_10).toInt().apply {
				setMargins(this, this, this, this)
			}
		})
	}
}
