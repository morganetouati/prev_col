package com.example.prevcol

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsContainer: LinearLayout
    private lateinit var nextButton: Button
    private lateinit var skipButton: Button

    data class OnboardingSlide(
        val imageRes: Int,
        val titleRes: Int,
        val descriptionRes: Int
    )

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        dotsContainer = findViewById(R.id.dotsContainer)
        nextButton = findViewById(R.id.nextButton)
        skipButton = findViewById(R.id.skipButton)

        val slides = listOf(
            OnboardingSlide(
                R.drawable.fiche_1,
                R.string.onboarding_title_1,
                R.string.onboarding_desc_1
            ),
            OnboardingSlide(
                R.drawable.fiche_2,
                R.string.onboarding_title_2,
                R.string.onboarding_desc_2
            ),
            OnboardingSlide(
                R.drawable.ic_eye_logo,
                R.string.onboarding_title_3,
                R.string.onboarding_desc_3
            )
        )

        val adapter = OnboardingAdapter(slides)
        viewPager.adapter = adapter

        setupDots(slides.size)
        updateDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                if (position == slides.size - 1) {
                    nextButton.text = getString(R.string.onboarding_start)
                } else {
                    nextButton.text = getString(R.string.onboarding_next)
                }
            }
        })

        nextButton.setOnClickListener {
            if (viewPager.currentItem < slides.size - 1) {
                viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        skipButton.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_seen", true)
            .apply()
        finish()
    }

    private fun setupDots(count: Int) {
        dotsContainer.removeAllViews()
        for (i in 0 until count) {
            val dot = View(this)
            val params = LinearLayout.LayoutParams(24, 24)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            dot.setBackgroundResource(android.R.drawable.presence_invisible)
            dotsContainer.addView(dot)
        }
    }

    private fun updateDots(activePosition: Int) {
        for (i in 0 until dotsContainer.childCount) {
            val dot = dotsContainer.getChildAt(i)
            if (i == activePosition) {
                dot.background = createDotDrawable(ContextCompat.getColor(this, R.color.accent))
                dot.layoutParams = LinearLayout.LayoutParams(32, 12).apply {
                    setMargins(8, 0, 8, 0)
                }
            } else {
                dot.background = createDotDrawable(ContextCompat.getColor(this, R.color.text_hint))
                dot.layoutParams = LinearLayout.LayoutParams(12, 12).apply {
                    setMargins(8, 0, 8, 0)
                }
            }
        }
    }

    private fun createDotDrawable(color: Int): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 12f
            setColor(color)
        }
    }

    // Adapter pour le ViewPager2
    inner class OnboardingAdapter(
        private val slides: List<OnboardingSlide>
    ) : RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder>() {

        inner class SlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image: ImageView = view.findViewById(R.id.slideImage)
            val title: TextView = view.findViewById(R.id.slideTitle)
            val description: TextView = view.findViewById(R.id.slideDescription)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding_slide, parent, false)
            return SlideViewHolder(view)
        }

        override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
            val slide = slides[position]
            holder.image.setImageResource(slide.imageRes)
            holder.title.text = getString(slide.titleRes)
            holder.description.text = getString(slide.descriptionRes)
        }

        override fun getItemCount() = slides.size
    }
}
