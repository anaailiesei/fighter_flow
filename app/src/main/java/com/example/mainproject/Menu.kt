package com.example.mainproject
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.mainproject.databinding.MenuBinding

// TODO: add highlighted menu option if u are in the menu's option corresponding fragment
// TODO: look into motion action and click on motion and transitions to make the animation
// Class file for menu
class Menu(private val activity: AppCompatActivity, private val binding: MenuBinding){
    // Variable used to store the state of the menu
    internal var isExpanded = false

    // TODO: Find a more elegant way to define this
    private val menuAnimationsDuration = 200L

    // Menu options
    private val menuOperatingOptions= listOf(binding.menuMain, binding.menuClose)
    internal val menuNavigationOptions = listOf(binding.menuAddParticipant, binding.menuSearch, binding.menuHome)
    internal val menuOptions = menuOperatingOptions + menuNavigationOptions

    internal val menuToFragmentMap = mapOf(
        binding.menuAddParticipant to R.id.addPerson,
        binding.menuSearch to R.id.search,
        binding.menuHome to R.id.home
    )

    internal fun crossFadeMenu() {
        // Set animators for fading in and fading out
        val animOut by lazy { AnimatorInflater.loadAnimator(activity, R.animator.fade_out)}
        val animIn by lazy { AnimatorInflater.loadAnimator(activity, R.animator.fade_in)}

        // Set targets for the animators based on the state of the menu button
        // If main menu button is pressed, main manu fades out and close menu fades in
        // If closing menu button is pressed, close menu option fades out and main menu fades in
        if (!isExpanded) {
            animOut.setTarget(binding.menuMain)
            animIn.setTarget(binding.menuClose)
        } else {
            animOut.setTarget(binding.menuClose)
            animIn.setTarget(binding.menuMain)
        }

        // Create animation set with the fade in and fade out animators
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animOut, animIn)
        animatorSet.duration = menuAnimationsDuration

        // Start the animator
        animatorSet.start()

        // Change menu button state
        isExpanded = !isExpanded

        // Triggers extended menu if needed and sets visibility and clickability for the other
        // menu options accordingly
        popUpMenu()
        setVisibility()
        setClickable()
    }

    private fun popUpMenu() {
        // This is the point where all the menu icons should collapse for animation
        val collapsePointTop = binding.menuMain.top

        for (option in menuNavigationOptions) {
            val optionTop = option.top
            // Calculate the translation distance along the Y axis
            val translationDistance = optionTop - collapsePointTop

            // Create animations
            val fromBottomAnim by lazy { translationYAnimation(-translationDistance.toFloat(), 0f, 0f, 1f) }
            val toBottomAnim by lazy { translationYAnimation(0f, -translationDistance.toFloat(), 1f, 0f) }

            // Use proper animation for each case (extended menu open or close)
            if (isExpanded)
                option.startAnimation(fromBottomAnim)
            else
                option.startAnimation(toBottomAnim)
        }
    }

    // Set visibility for menu options
    private fun setVisibility() {
        for (option in menuNavigationOptions) {
            if (isExpanded)
                option.visibility = View.VISIBLE
            else
                option.visibility = View.INVISIBLE
        }
    }

    // Set clickability for menu options
    private fun setClickable() {
        for (option in menuNavigationOptions) {
            option.isClickable = isExpanded
        }
    }

    // Translation animation for menu options
    // Only needs values for Y translation and alpha
    // Duration is fixed
    private fun translationYAnimation(
        fromYDelta: Float,
        toYDelta: Float,
        fromAlpha: Float,
        toAlpha: Float
    ): AnimationSet {
        val translationAnimation = TranslateAnimation(0f, 0f, fromYDelta, toYDelta)
        translationAnimation.duration = menuAnimationsDuration
        val alphaAnimation = AlphaAnimation(fromAlpha, toAlpha)
        alphaAnimation.duration = menuAnimationsDuration

        val animationSet = AnimationSet(true)
        animationSet.addAnimation(translationAnimation)
        animationSet.addAnimation(alphaAnimation)

        return animationSet
    }
}
