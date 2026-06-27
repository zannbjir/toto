package org.koitharu.kotatsu.list.ui.adapter

import androidx.core.view.isVisible
import androidx.core.graphics.ColorUtils
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.ui.list.AdapterDelegateClickListenerAdapter
import org.koitharu.kotatsu.core.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.core.util.ext.setTooltipCompat
import org.koitharu.kotatsu.core.util.ext.getThemeColor
import org.koitharu.kotatsu.databinding.ItemMangaGridBinding
import org.koitharu.kotatsu.list.ui.ListModelDiffCallback.Companion.PAYLOAD_PROGRESS_CHANGED
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.list.ui.model.MangaGridModel
import org.koitharu.kotatsu.list.ui.model.MangaListModel
import org.koitharu.kotatsu.list.ui.size.ItemSizeResolver
import androidx.appcompat.R as appcompatR

fun mangaGridItemAD(
	sizeResolver: ItemSizeResolver,
	clickListener: OnListItemClickListener<MangaListModel>,
) = adapterDelegateViewBinding<MangaGridModel, ListModel, ItemMangaGridBinding>(
	{ inflater, parent -> ItemMangaGridBinding.inflate(inflater, parent, false) },
) {

	AdapterDelegateClickListenerAdapter(this, clickListener).attach(itemView)
	sizeResolver.attachToView(itemView, binding.textViewTitleOverlay, binding.progressView)

    val density = context.resources.displayMetrics.density
    val darkAccent = ColorUtils.blendARGB(context.getThemeColor(androidx.appcompat.R.attr.colorPrimary), Color.BLACK, 0.78f)
    
    binding.viewScrim.background = GradientDrawable(
        GradientDrawable.Orientation.BOTTOM_TOP,
        intArrayOf(
            ColorUtils.setAlphaComponent(darkAccent, 0xF2),
            ColorUtils.setAlphaComponent(darkAccent, 0xC0),
            ColorUtils.setAlphaComponent(darkAccent, 0x00),
        )
    ).apply {
        val r = 16f * density
        cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r)
    }
    
    
	bind { payloads ->
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val isTextInsideCover = prefs.getBoolean("pref_text_inside_cover", false)
        
        itemView.setTooltipCompat(item.getSummary(context))
        binding.textViewTitleOverlay.text = item.title
        binding.textViewTitle.text = item.title

        binding.textViewTitleOverlay.isVisible = isTextInsideCover
        binding.viewScrim.isVisible = isTextInsideCover
        binding.textViewTitle.isVisible = !isTextInsideCover
        binding.progressView.setProgress(item.progress, PAYLOAD_PROGRESS_CHANGED in payloads)

        with(binding.iconsView) {
            clearIcons()
            if (item.isPinned) addIcon(R.drawable.ic_pin_small)
            if (item.isSaved) addIcon(R.drawable.ic_storage)
            if (item.isFavorite) addIcon(R.drawable.ic_heart_outline)
            isVisible = iconsCount > 0
        }

        binding.imageViewCover.setImageAsync(item.coverUrl, item.manga)
        binding.badge.number = item.counter
        binding.badge.isVisible = item.counter > 0
    }
}
