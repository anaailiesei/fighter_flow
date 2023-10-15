package com.example.mainproject

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.mainproject.add_person.AddParticipant
import com.example.mainproject.data_management.DataBase
import com.example.mainproject.databinding.ActivityMainBinding
import com.example.mainproject.databinding.MenuBinding

class MainActivity : AppCompatActivity() {
    // TODO: Make bindings nullable
    // TODO: also check for other nullable cases
    private lateinit var binding: ActivityMainBinding
    private lateinit var menuHandler: Menu
    private lateinit var navController: NavController
    private lateinit var menuBinding: MenuBinding
    private lateinit var dataBase : DataBase
    private lateinit var sharedViewModel : SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set binding for Main Activity
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the container for the menu
        val containerLayout = binding.menuContainer

        // Inflate the menu layout using menuBinding
        menuBinding = MenuBinding.inflate(layoutInflater)
        val inflatedView = menuBinding.root

        // Add the inflated menu view to the container
        containerLayout.addView(inflatedView)

        // Initialize the MenuHandler with the activity and binding
        menuHandler = Menu(this, menuBinding)

        // Set navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val currentFragment = navHostFragment.childFragmentManager.findFragmentById(R.id.nav_host_fragment)
        // TODO: Change this line if u change nav host fr!!!!!!!
        val addParticipantFragment = currentFragment as AddParticipant


        // Create database instance
        dataBase = DataBase()

        // Initialize SharedViewModel
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        sharedViewModel.dataBase = dataBase

        // TODO: Should i find another approach for navigation? it doesn't seem intuitive or a good approach
        menuHandler.menuOptions.forEach {view ->
            view.setOnClickListener {
                // Set navigation for menu options
                if (view in menuHandler.menuNavigationOptions) {
                menuHandler.menuToFragmentMap[view]?.let { fragmentId ->
                    navController.navigate(fragmentId)
                }
            }
                // Set animation for popup/fade of the menu
                menuHandler.crossFadeMenu() }
        }
    }

    // Hide keyboard
    private fun hideSoftInputFromWindow(v: View) {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    // Show keyboard
    internal fun showSoftInputToWindow(v: View) {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, 0)
    }

    // Close the menu when it's in its extended state and you press outside of its bounds
    // TODO: this is a mess, make this better
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val outRect = Rect()
            val v: View? = currentFocus
            if (menuHandler.isExpanded) {
                menuBinding.menu.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    menuHandler.crossFadeMenu()
                    return false
                }
                // TODO: if another textedit is touched, move focus to that one
            } else if ( v is EditText) {
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        hideSoftInputFromWindow(v)
                    v.clearFocus()
                        return false
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}