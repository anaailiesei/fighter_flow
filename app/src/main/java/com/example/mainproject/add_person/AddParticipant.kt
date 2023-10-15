package com.example.mainproject.add_person

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.ProxyFileDescriptorCallback
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mainproject.data_management.DataBase
import com.example.mainproject.MainActivity
import com.example.mainproject.data_management.ParticipantInfo
import com.example.mainproject.R
import com.example.mainproject.SharedViewModel
import com.example.mainproject.data_management.Id
import com.example.mainproject.databinding.FragmentAddPersonBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AddParticipant : Fragment() {

    // TODO: make binding nullable
    // TODO: add back/close button
    private lateinit var binding: FragmentAddPersonBinding
    private lateinit var hostingActivity: MainActivity
    private lateinit var dataBase: DataBase

    // For TextInput data handling
    var selectedDate: Long? = null

    // For submit button state
    private var isSubmitButtonEnabled = false

    // For TextWatcher validation of TextInput fields
    companion object {
        var participantPhoneValidator = false
        var parentPhoneValidator = false
        var dateValidator = false
        var nameValidator = false
        val TAG = "AILIESEI"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Set up binding
        binding = FragmentAddPersonBinding.inflate(layoutInflater)

        // Clone the constraints from the fragment view
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.fragmentConstraint)

        // Set percent constraints
        constraintSet.constrainPercentWidth(binding.infoBox.id, 0.85f)
        constraintSet.setGuidelinePercent(binding.horizontalGuideline.id, 0.2f)

        // Apply modified constraints
        constraintSet.applyTo(binding.fragmentConstraint)
        return binding.root
    }

    // Initialise the hosting activity
    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is MainActivity) {
            hostingActivity = context
        } else {
            throw IllegalArgumentException("Hosting activity must be of type MainActivity")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize AddParticipant view model
        val viewModel: AddParticipantViewModel by activityViewModels()

        // Initialize SharedViewModel
        val sharedViewModel: SharedViewModel by activityViewModels()
        dataBase = sharedViewModel.dataBase!!

        // For easy management of views
        val participantPhoneInput = binding.participantPhoneNumberInput
        val participantPhoneField = binding.participantPhoneNumberField
        val parentPhoneInput = binding.parentPhoneNumberInput
        val parentPhoneField = binding.parentPhoneNumberField
        val dateInput = binding.birthDateInput
        val nameInput = binding.nameInput
        val bdayInput = binding.birthDateInput

        bdayInput.setOnClickListener {
            // Create and show date picker
            val picker = createDatePickerDialog()
            picker.show(parentFragmentManager, "TAG")

            // Format the date and set it to the Date Edit Text
            picker.addOnPositiveButtonClickListener {
                selectedDate = picker.selection
                val formattedDate = picker.selection?.let { it1 -> formatDate(it1) }
                //bdayInput.setText("")
                bdayInput.setText(formattedDate)
            }
        }

        // Show phone number hint if nothing is entered and view is in focus
        showPhoneNumberHint(parentPhoneInput, parentPhoneField)
        showPhoneNumberHint(participantPhoneInput, participantPhoneField)

        // Text Watcher for formatting phone number and submit button enabler
        participantPhoneInput.addTextChangedListener(ThreeDigitPhoneNumberFormatWatcher(this,"participant"))
        parentPhoneInput.addTextChangedListener(ThreeDigitPhoneNumberFormatWatcher(this,"parent"))
        // Text Watcher for submit button enabler
        nameInput.addTextChangedListener(EmptyFieldTextWatcher(this, "name"))
        dateInput.addTextChangedListener(EmptyFieldTextWatcher(this, "date"))

        // Find Nav Controller
        val navController = findNavController()

        // Set bundle
        val bundle = Bundle()
        bundle.putString("sourceFragment", "addPerson")

        // When submit is clicked create participant's profile
        binding.submitButton.setOnClickListener {
            getParticipantInfo{participantInfo ->

                dataBase.addParticipant(dataBase.createParticipant(participantInfo))
                viewModel.registeredParticipantInfo = participantInfo
                parentPhoneValidator = false
                nameValidator = false
                participantPhoneValidator = false
                dateValidator = false
                navController.navigate(R.id.action_addPerson_to_profile, bundle)
            }
        }

    }

    // TODO: Dummy code, delete if not needed anymore
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
    }

    // Format the date to be in dd/MM/yyyy style
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // TODO: add selecteaza data as a string
    // TODO: make this work with day/month format
    // Creates material date picker
    private fun createDatePickerDialog(): MaterialDatePicker<Long> {
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build()

        return MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecteaza data")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraintsBuilder)
            .build()
    }

    // TODO: maybe add a name hint too
    // Show phone number hint only when in focus and no text has been entered
    private fun showPhoneNumberHint(input: TextInputEditText, field: TextInputLayout) {
        input.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hostingActivity.showSoftInputToWindow(input)
                val showHint = input.text.isNullOrEmpty()
                input.hint =
                    if (showHint) getString(R.string.default_phone_num) else ""
                if (!showHint) field.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
            } else {
                field.endIconMode = TextInputLayout.END_ICON_NONE
                input.hint = ""
            }
        }
    }

    // TODO: Add field validation errors

    // Set enable for submit button
    fun submitButtonEnable(enable: Boolean) {
        val submitButton = binding.submitButton

        if (isSubmitButtonEnabled && enable)
            return

        if (!isSubmitButtonEnabled && !enable)
            return

        if (enable) {
            submitButton.isEnabled = true
            submitButton.setTextColor(ContextCompat.getColor(hostingActivity, R.color.green))
            submitButton.setStrokeColorResource(R.color.green)
            submitButton.setBackgroundColor(ContextCompat.getColor(hostingActivity, R.color.translucent_forest_green))
            isSubmitButtonEnabled = true
        } else {
            submitButton.isEnabled = false
            submitButton.setTextColor(ContextCompat.getColor(hostingActivity, R.color.light_grey))
            submitButton.setStrokeColorResource(R.color.light_grey)
            submitButton.setBackgroundColor(ContextCompat.getColor(hostingActivity, R.color.translucent_grey))
            isSubmitButtonEnabled = false
        }
    }

    fun getParticipantPhoneInputEditText(): TextInputEditText {
        return binding.participantPhoneNumberInput
    }

    fun getParentPhoneInputEditText(): TextInputEditText {
        return binding.parentPhoneNumberInput
    }

    fun getNameInputEditable(): TextInputEditText {
        return  binding.nameInput
    }

    fun getDateInputEditable(): TextInputEditText {
        return  binding.birthDateInput
    }

    fun setDatabaseInstance(dataBase: DataBase) {
        this.dataBase = dataBase;
    }

    // Takes a full name as an argument and parses it into a last and first name
    private fun parseFullName(name: String): Pair<String, String> {
        // Split in lastName and firstName
        val nameParts = name.split(" ", limit = 2)

        // TODO: Handle the case in which the name only has one name in it
        val lastName = nameParts.get(0)
        val firstName = nameParts.getOrElse(1) {""}

        // Correct case of first name
        val firstNameCaseCorrected = capitalizeWordsAfterHyphen(firstName)

        return Pair<String, String>(lastName, firstNameCaseCorrected)
    }

    // Capitalizes names that use hyphens
    private fun capitalizeWordsAfterHyphen(name: String): String {
        if (name == "") return ""
        return name.split('-').joinToString("-") { it ->
            it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }
    }

    fun getSearchName(lastname: String, firstName: String): String {
        val fullName = lastname + firstName
        val regex = Regex("[^A-Za-z]")
        return regex.replace(fullName, "").lowercase(Locale.getDefault())
    }

    private fun getParticipantInfo(callback: (ParticipantInfo) -> Unit){
        // Handle Name
        val name = binding.nameInput.text.toString()
        val nameParsed = parseFullName(name)
        val lastName = nameParsed.first
        val firstName = nameParsed.second

        // Handle Date
        val calendar = Calendar.getInstance()
        selectedDate?.let { calendar.time = Date(it) }
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        Log.d(
            TAG,
            "day = " + day.toString() + "month = " + month.toString() + "year = " + year.toString()
        )

        // TODO: Be cautious about null strings/ data entries
        // Handle participant's phone number
        val participantPhonePrefix = binding.participantCountryPicker.selectedCountryCode
        Log.d(TAG, "participant phone prefix = $participantPhonePrefix")
        val participantPhone = binding.participantPhoneNumberInput.text
        Log.d(TAG, "participant phone = " + participantPhone.toString())
        val participantFullPhone = if (participantPhone!!.isNotEmpty())
            ("+$participantPhonePrefix$participantPhone").replace(" ", "") else ""
        Log.d(TAG, "participant full phone = $participantFullPhone")

        // Handle parent's phone number
        val parentPhonePrefix = binding.parentCountryPicker.selectedCountryCode
        Log.d(TAG, "parent phone prefix = $parentPhonePrefix")
        val parentPhone = binding.parentPhoneNumberInput.text
        Log.d(TAG, "parent phone = " + parentPhone.toString())
        val parentFullPhone = if (parentPhone!!.isNotEmpty())
            ("+$parentPhonePrefix$parentPhone").replace(" ", "") else ""
        Log.d(TAG, "parent full phone = $parentFullPhone")

        // Handle commuter switch
        val isCommuter = binding.commuterSwitch.isChecked
        Log.d(TAG, "commuter = $isCommuter")

        // Get the id for this user
        var availableId: Id
        dataBase.getNextAvailableId {
            availableId = it
            dataBase.updateNextId()

            val nameSearch = getSearchName(lastName, firstName)
            callback (ParticipantInfo(
                firstName,
                lastName,
                day,
                month,
                year,
                participantFullPhone,
                parentFullPhone,
                isCommuter,
                availableId.letter,
                availableId.value,
                nameSearch
            ))
        }

    }
}

