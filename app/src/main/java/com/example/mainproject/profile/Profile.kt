package com.example.mainproject.profile

import android.icu.util.Calendar
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mainproject.R
import com.example.mainproject.add_person.AddParticipantViewModel
import com.example.mainproject.databinding.FragmentAddPersonBinding
import com.example.mainproject.databinding.FragmentProfileBinding

class Profile : Fragment() {
    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding
    private var sourceFragment: String? = null

    val addParticipantViewModel: AddParticipantViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Set up binding
        binding = FragmentProfileBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            sourceFragment = it.getString("sourceFragment")
        }

        if (sourceFragment == "addPerson") {
            Log.d("ProfileFragment", "this worked")
            val participantInfo = addParticipantViewModel.registeredParticipantInfo

            // Set full name in the corresponding text views
            binding.participantFirstname.text = participantInfo?.firstName
            binding.participantSurname.text = participantInfo?.lastName
            binding.participantPhone.text = participantInfo?.participantPhone
            binding.parentPhone.text = participantInfo?.parentPhone
            val day = participantInfo?.birthDay
            val month = participantInfo?.birthMonth
            val year = participantInfo?.birthYear

            binding.commuter.text = if(participantInfo?.isCommuter == true) "Da" else "Nu"

            val age = getAge(day!!, month!!, year!!)

            binding.age.text = age.toString()

            // Get id fields
            val idLetter = participantInfo?.idLetter
            val idValue = participantInfo?.idValue

            // TODO: this code repeats do smth about it
            // Format the id
            val idValueString = String.format("%03d", idValue)
            val participantId : String = "#$idLetter$idValueString"

            // Set the participant id
            binding.participantId.text = participantId
        }

    }

    private fun getAge (day: Int, month: Int, year: Int) : Int {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Note: Months are 0-based, so add 1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        // Calculate age
        var age = currentYear - year

        // Check if the birthdate for this year has occurred yet
        if ((month > currentMonth) || ((month == currentMonth) && (day > currentDay))) {
            age--
        }

        return age
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

}