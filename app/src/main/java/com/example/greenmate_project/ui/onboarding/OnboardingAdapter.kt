package com.example.greenmate_project.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.example.greenmate_project.R

/**
 * Adapter for onboarding pages ViewPager2.
 */
class OnboardingAdapter(
    private val pages: List<OnboardingPage>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_page, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageOnboarding: ImageView = itemView.findViewById(R.id.image_onboarding)
        private val textTitle: TextView = itemView.findViewById(R.id.text_title)
        private val textDescription: TextView = itemView.findViewById(R.id.text_description)

        fun bind(page: OnboardingPage) {
            imageOnboarding.setImageResource(page.imageRes)
            textTitle.setText(page.titleRes)
            textDescription.setText(page.descriptionRes)
        }
    }
}

/**
 * Data class representing an onboarding page.
 */
data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)
