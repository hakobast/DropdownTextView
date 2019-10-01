package hakobastvatsatryan

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.os.Bundle
import android.os.Parcelable
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import androidx.annotation.*
import hakobastvatsatryan.dropdowntextview.R


/**
 * Created by hakob on 12/6/16.
 */

class DropdownTextView : LinearLayout {

	private lateinit var panelView: View
	private lateinit var titleTextView: TextView
	private lateinit var contentTextView: TextView
	private lateinit var arrowView: View

	private var isExpanded: Boolean = false
	private var titleText: String? = null
	private var contentText: String? = null
	private var expandDuration: Int = -1
	private var titleTextColor: Int? = null
	private var titleTextColorExpanded: Int? = null
	private var titleTextSizeRes: Int = -1
	private var titleFontRes: Int = -1
	private var contentTextColor: Int? = null
	private var contentTextSizeRes: Int = -1
	private var contentFontRes: Int = -1
	private var rawHtmlContent: String? = null
	private var spannableHtmlContent: String? = null
	private var linkHandler: ((url: String) -> Unit)? = null
	private var linkTextColor: Int? = null
	private var isLinkUnderline: Boolean = true
	private var arrowDrawableRes: Int = -1
	private var bgRegularDrawableRes: Int = -1
	private var bgExpandedDrawableRes: Int = -1
	private var panelPaddingRes: Int = -1
	private var contentPaddingRes: Int = -1

	private constructor(context: Context, builder: Builder) : super(context) {
		readBuilder(builder)
		initialize()
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		if (!isInEditMode) {
			readAttributes(attrs)
			initialize()
		}
	}

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
			context,
			attrs,
			defStyle
	) {
		if (!isInEditMode) {
			readAttributes(attrs)
			initialize()
		}
	}

	fun expand(animate: Boolean) {
		if (isExpanded) {
			return
		}

		expandInternal(animate)
	}

	fun collapse(animate: Boolean) {
		if (!isExpanded) {
			return
		}

		collapseInternal(animate)
	}

	fun setTitleText(text: String) {
		titleTextView.text = text
	}

	fun setTitleText(@StringRes stringRes: Int) {
		titleTextView.setText(stringRes)
	}

	fun setContentText(text: String) {
		contentTextView.text = text
	}

	fun setContentText(@StringRes stringRes: Int) {
		contentTextView.setText(stringRes)
	}

	fun setHtmlContent(text: String) {
		contentTextView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE)
		contentTextView.movementMethod = LinkMovementMethod.getInstance()
	}

	fun setHtmlContent(text: String, clickHandler: (url: String) -> Unit) {
		val html = Html.fromHtml(text)
		val spannableBuilder = SpannableStringBuilder(html)
		val urlSPans = spannableBuilder.getSpans(0, html.length, URLSpan::class.java)
		urlSPans.forEach {
			makeClickable(spannableBuilder, it, isLinkUnderline) { url ->
				clickHandler(url)
			}
		}

		contentTextView.text = spannableBuilder
		contentTextView.movementMethod = LinkMovementMethod.getInstance()
	}

	private fun makeClickable(spannableBuilder: SpannableStringBuilder,
							  span: URLSpan,
							  makeUnderline: Boolean,
							  clickHandler: (url: String) -> Unit
	) {
		val clickableSpan = object : ClickableSpan() {
			override fun onClick(widget: View) {
				clickHandler(span.url)
			}

			override fun updateDrawState(ds: TextPaint) {
				super.updateDrawState(ds)
				ds.isUnderlineText = makeUnderline
			}
		}
		spannableBuilder.setSpan(
				clickableSpan,
				spannableBuilder.getSpanStart(span),
				spannableBuilder.getSpanEnd(span),
				spannableBuilder.getSpanFlags(span)
		)
		spannableBuilder.removeSpan(span)
	}

	fun getTitleTextView(): TextView {
		return titleTextView
	}

	fun getContentTextView(): TextView {
		return contentTextView
	}

	override fun onSaveInstanceState(): Parcelable? {
		val bundle = Bundle()
		bundle.putParcelable("superState", super.onSaveInstanceState())
		bundle.putBoolean("expanded", this.isExpanded)
		return bundle
	}

	override fun onRestoreInstanceState(state: Parcelable?) {
		var superState: Parcelable? = null
		if (state is Bundle) {
			isExpanded = state.getBoolean("expanded")
			superState = state.getParcelable("superState")
		}

		super.onRestoreInstanceState(superState)
	}

	private fun readBuilder(builder: Builder) {
		titleText = resources.getString(builder.titleTextRes)
		titleTextColor = ContextCompat.getColor(context, builder.titleTextColorRes)
		titleTextColorExpanded = ContextCompat.getColor(context, builder.titleTextColorExpandedRes)
		titleTextSizeRes = builder.titleTextSizeRes
		titleFontRes = builder.titleFontRes

		contentText = if (builder.contentTextRes > 0) resources.getString(builder.contentTextRes) else null
		contentTextColor = ContextCompat.getColor(context, builder.contentTextColorRes)
		contentTextSizeRes = builder.contentTextSizeRes

		contentFontRes = builder.contentFontRes
		rawHtmlContent = builder.rawHtmlContent
		spannableHtmlContent = builder.spannableHtmlContent
		linkHandler = builder.linkHandler

		linkTextColor = if(builder.linkTextColorRes > 0) ContextCompat.getColor(context, builder.linkTextColorRes) else null
		isLinkUnderline = builder.isLinkUnderline

		bgRegularDrawableRes = builder.bgRegularDrawableRes
		bgExpandedDrawableRes = builder.bgExpandedDrawableRes

		panelPaddingRes = if (builder.panelPaddingRes == -1) R.dimen.panel_default_padding else builder.panelPaddingRes
		contentPaddingRes = if (builder.contentPaddingRes == -1) R.dimen.content_default_padding else builder.contentPaddingRes

		arrowDrawableRes = if (builder.arrowDrawableRes == -1) R.drawable.ic_arrow else builder.arrowDrawableRes

		expandDuration = if (builder.expandDuration == -1) 300 else builder.expandDuration
	}

	private fun readAttributes(attrs: AttributeSet) {
		val a = context.theme.obtainStyledAttributes(
				attrs,
				R.styleable.DropdownTextView,
				0, 0
		)

		val typeValue = TypedValue()

		a.getValue(R.styleable.DropdownTextView_title_text, typeValue)
		titleText = when (typeValue.type) {
			TypedValue.TYPE_STRING -> typeValue.string as String?
			TypedValue.TYPE_REFERENCE -> resources.getString(typeValue.resourceId)
			else -> null
		}

		a.getValue(R.styleable.DropdownTextView_title_text_color, typeValue)
		titleTextColor = when (typeValue.type) {
			TypedValue.TYPE_REFERENCE -> ContextCompat.getColor(context, typeValue.resourceId)
			else -> typeValue.data
		}

		a.getValue(R.styleable.DropdownTextView_title_text_color_expanded, typeValue)
		titleTextColorExpanded = when (typeValue.type) {
			TypedValue.TYPE_REFERENCE -> ContextCompat.getColor(context, typeValue.resourceId)
			else -> typeValue.data
		}

		titleTextSizeRes = a.getResourceId(R.styleable.DropdownTextView_title_text_size, -1)
		titleFontRes = a.getResourceId(R.styleable.DropdownTextView_title_font, -1)

		a.getValue(R.styleable.DropdownTextView_content_text, typeValue)
		contentText = when (typeValue.type) {
			TypedValue.TYPE_STRING -> typeValue.string as String?
			TypedValue.TYPE_REFERENCE -> resources.getString(typeValue.resourceId)
			else -> null
		}

		a.getValue(R.styleable.DropdownTextView_content_text_color, typeValue)
		contentTextColor = when (typeValue.type) {
			TypedValue.TYPE_REFERENCE -> ContextCompat.getColor(context, typeValue.resourceId)
			else -> typeValue.data
		}

		a.getValue(R.styleable.DropdownTextView_link_text_color, typeValue)
		linkTextColor = when (typeValue.type) {
			TypedValue.TYPE_REFERENCE -> ContextCompat.getColor(context, typeValue.resourceId)
			else -> typeValue.data
		}

		a.getValue(R.styleable.DropdownTextView_underline_link, typeValue)
		isLinkUnderline = when (typeValue.type) {
			TypedValue.TYPE_INT_BOOLEAN -> typeValue.data == 1
			else -> true
		}

		contentTextSizeRes = a.getResourceId(R.styleable.DropdownTextView_content_text_size, -1)
		contentFontRes = a.getResourceId(R.styleable.DropdownTextView_content_font, -1)

		bgRegularDrawableRes = a.getResourceId(R.styleable.DropdownTextView_bg_drawable_regular, -1)
		bgExpandedDrawableRes = a.getResourceId(
				R.styleable.DropdownTextView_bg_drawable_expanded,
				-1
		)

		panelPaddingRes = a.getResourceId(
				R.styleable.DropdownTextView_panel_padding,
				R.dimen.panel_default_padding
		)
		contentPaddingRes = a.getResourceId(
				R.styleable.DropdownTextView_content_padding,
				R.dimen.content_default_padding
		)

		arrowDrawableRes = a.getResourceId(
				R.styleable.DropdownTextView_arrow_drawable,
				R.drawable.ic_arrow
		)

		expandDuration = a.getInteger(R.styleable.DropdownTextView_expand_duration, 300)
	}

	private fun initialize() {
		inflateView()
		bindView()
		setResources()

		post {
			if (isExpanded) {
				expandInternal(false)
			} else {
				collapseInternal(false)
			}
			setArrowViewState(isExpanded, false)
			setBackgroundState(isExpanded)
		}
	}

	private fun bindView() {
		panelView = findViewById(R.id.panel_view)
		titleTextView = findViewById(R.id.title_text_view)
		contentTextView = findViewById(R.id.content_text_view)
		arrowView = findViewById(R.id.arrow_view)

		panelView.setOnClickListener {
			if (isExpanded) {
				collapse(true)
			} else {
				expand(true)
			}
		}
	}

	private fun setResources() {
		arrowView.setBackgroundResource(arrowDrawableRes)
		titleTextView.text = titleText
		contentTextView.text = contentText

		titleTextColor?.let { titleTextView.setTextColor(it) }
		if (titleTextSizeRes != -1) {
			titleTextView.setTextSize(
					TypedValue.COMPLEX_UNIT_PX,
					context.resources.getDimension(titleTextSizeRes)
			)
		}
		if (titleFontRes != -1) {
			titleTextView.typeface = ResourcesCompat.getFont(context, titleFontRes)
		}

		contentTextColor?.let { contentTextView.setTextColor(it) }
		if (contentTextSizeRes != -1) {
			contentTextView.setTextSize(
					TypedValue.COMPLEX_UNIT_PX,
					context.resources.getDimension(contentTextSizeRes)
			)
		}
		if (contentFontRes != -1) {
			contentTextView.typeface = ResourcesCompat.getFont(context, contentFontRes)
		}

		rawHtmlContent?.let {
			setHtmlContent(it)
		}

		spannableHtmlContent?.let {
			setHtmlContent(it, linkHandler!!)
		}

		linkTextColor?.let {
			contentTextView.setLinkTextColor(it)
		}

		context.resources.getDimension(panelPaddingRes).toInt().apply {
			panelView.setPadding(this, this, this, this)
		}
		context.resources.getDimension(contentPaddingRes).toInt().apply {
			contentTextView.setPadding(this, this, this, this)
		}


	}

	private fun inflateView() {
		View.inflate(context, R.layout.view_dropdown_text_view, this)
	}

	private fun expandInternal(animate: Boolean) {
		setHeightToContentHeight(animate)
		setArrowViewState(true, animate)
		setBackgroundState(true)
		setTitleTextState(true)
		isExpanded = true
	}

	private fun collapseInternal(animate: Boolean) {
		setHeightToZero(animate)
		setArrowViewState(false, animate)
		setBackgroundState(false)
		setTitleTextState(false)
		isExpanded = false
	}

	private fun setBackgroundState(expand: Boolean) {
		if (!expand && bgRegularDrawableRes != -1) {
			setBackgroundResource(bgRegularDrawableRes)
		} else if (bgExpandedDrawableRes != -1) {
			setBackgroundResource(bgExpandedDrawableRes)
		}
	}

	private fun setTitleTextState(expand: Boolean) {
		if (expand) {
			(titleTextColorExpanded ?: titleTextColor)?.let { titleTextView.setTextColor(it) }
		} else {
			titleTextColor?.let { titleTextView.setTextColor(it) }
		}
	}

	private fun setArrowViewState(expand: Boolean, animate: Boolean) {
		val angle = if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
			if (expand) 90.0f else 180.0f
		} else {
			if (expand) 90.0f else 0.0f
		}

		arrowView.animate()
				.rotation(angle)
				.setDuration((if (animate) expandDuration else 0).toLong())
				.start()
	}

	private fun setHeightToZero(animate: Boolean) {
		val targetHeight = panelView.height
		if (animate) {
			animate(this, height, targetHeight, expandDuration)
		} else {
			setContentHeight(targetHeight)
		}
	}

	private fun setHeightToContentHeight(animate: Boolean) {
		measureContentTextView()
		val targetHeight = panelView.height + contentTextView.measuredHeight
		if (animate) {
			animate(this, height, targetHeight, expandDuration)
		} else {
			setContentHeight(targetHeight)
		}
	}

	private fun setContentHeight(height: Int) {
		layoutParams.height = height
		requestLayout()
	}

	private fun animate(view: View, from: Int, to: Int, duration: Int) {
		changeValue(from, to, duration.toLong()) {
			view.layoutParams.height = it
			view.requestLayout()
			invalidate()
		}
	}

	private fun measureContentTextView() {
		val widthMS = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
		val heightMS = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
		contentTextView.measure(widthMS, heightMS)
	}

	class Builder(private val context: Context) {

		var expandDuration: Int = -1
			private set
		var titleTextRes: Int = -1
			private set
		var titleTextColorRes: Int = -1
			private set
		var titleTextColorExpandedRes: Int = -1
			private set
		var titleTextSizeRes: Int = -1
			private set
		var titleFontRes: Int = -1
			private set
		var contentTextRes: Int = -1
			private set
		var contentTextColorRes: Int = -1
			private set
		var linkTextColorRes: Int = -1
			private set
		var isLinkUnderline: Boolean = true
			private set
		var linkHandler: ((url: String) -> Unit)? = null
			private set
		var contentTextSizeRes: Int = -1
			private set
		var rawHtmlContent: String? = null
			private set
		var spannableHtmlContent: String? = null
			private set
		var contentFontRes: Int = -1
			private set
		var arrowDrawableRes: Int = -1
			private set
		var bgRegularDrawableRes: Int = -1
			private set
		var bgExpandedDrawableRes: Int = -1
			private set
		var panelPaddingRes: Int = -1
			private set
		var contentPaddingRes: Int = -1
			private set

		fun setExpandDuration(duration: Int): Builder {
			this.expandDuration = duration
			return this
		}

		fun setTitleTextRes(@StringRes colorRes: Int): Builder {
			this.titleTextRes = colorRes
			return this
		}

		fun setTitleTextColorRes(@ColorRes colorRes: Int): Builder {
			this.titleTextColorRes = colorRes
			return this
		}

		fun setTitleTextColorExpandedRes(@ColorRes colorRes: Int): Builder {
			this.titleTextColorExpandedRes = colorRes
			return this
		}

		fun setTitleTextSizeRes(@DimenRes sizeRes: Int): Builder {
			this.titleTextSizeRes = sizeRes
			return this
		}

		fun setTitleFontRes(@FontRes fontRes: Int): Builder {
			this.titleFontRes = fontRes
			return this
		}

		fun setContentTextRes(@StringRes colorRes: Int): Builder {
			this.contentTextRes = colorRes
			return this
		}

		fun setContentTextColorRes(@ColorRes colorRes: Int): Builder {
			this.contentTextColorRes = colorRes
			return this
		}

		fun setLinkTextColorRes(@ColorRes colorRes: Int): Builder {
			this.linkTextColorRes = colorRes
			return this
		}

		fun setLinkUnderline(value: Boolean): Builder {
			this.isLinkUnderline = value
			return this
		}

		fun setContentTextSizeRes(@DimenRes sizeRes: Int): Builder {
			this.contentTextSizeRes = sizeRes
			return this
		}

		fun setHtmlContent(text: String): Builder {
			this.rawHtmlContent = text
			return this
		}

		fun setHtmlContent(text: String, linkHandler: (url: String) -> Unit): Builder {
			this.spannableHtmlContent = text
			this.linkHandler = linkHandler
			return this
		}

		fun setContentFontRes(@FontRes fontRes: Int): Builder {
			this.contentFontRes = fontRes
			return this
		}

		fun setArrowDrawableRes(@DrawableRes drawableRes: Int): Builder {
			this.arrowDrawableRes = drawableRes
			return this
		}

		fun setRegularBackgroundDrawableRes(@DrawableRes drawableRes: Int): Builder {
			this.bgRegularDrawableRes = drawableRes
			return this
		}

		fun setExpandedBackgroundDrawableRes(@DrawableRes drawableRes: Int): Builder {
			this.bgExpandedDrawableRes = drawableRes
			return this
		}

		fun setPanelPaddingRes(@DimenRes paddingRes: Int): Builder {
			this.panelPaddingRes = paddingRes
			return this
		}

		fun setContentPaddingRes(@DimenRes paddingRes: Int): Builder {
			this.contentPaddingRes = paddingRes
			return this
		}

		fun build(): DropdownTextView {
			return DropdownTextView(context, this)
		}
	}
}