package com.example.mainproject.add_person

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File.separator

class ThreeDigitPhoneNumberFormatWatcher(val addParticipant: AddParticipant, private val current: String) : TextWatcher{
    // Class for formatting phone numbers (to Romanian standard)
    // Number will be formatted as: 723 172 563
    companion object {
        private const val separator = ' '
    }


    // Each group should have a length of 3 digits
    private val groupLength = 3

    // After each 3 digit group, there will be a separator
    private val separatorPosition = groupLength + 1

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable) {
        // Remove spacing char
            if (s.isNotEmpty() && s.length % separatorPosition == 0) {
                val c = s[s.length - 1]
                if (separator == c) {
                    s.delete(s.length - 1, s.length)
                }
            }

            // Insert char where needed.
            if (s.isNotEmpty() && s.length % separatorPosition == 0) {
                val c = s[s.length - 1]
                // Only if its a digit where there should be a separator we insert a separator
                if (Character.isDigit(c) && TextUtils.split(s.toString(), separator.toString()).size < groupLength) {
                    s.insert(s.length - 1, separator.toString())
                }
            }

            // TODO: now, if one of the phone fields is empty u can submit
            // TODO: u can still submit if one of them is invalid, fix this

            // TODO: find a better approach to combine textwatchers
        // TODO: delete button for phone field doesnt work anymore, fix it

        // Implement here the validation too
        if (s.isNotEmpty() && s.length == 11)
                if (current.equals("participant"))
                    AddParticipant.parentPhoneValidator = true
                else if (current.equals("parent"))
                    AddParticipant.parentPhoneValidator = true

        val phoneCondition : Boolean = AddParticipant.participantPhoneValidator || AddParticipant.parentPhoneValidator
            val condition = AddParticipant.nameValidator && AddParticipant.dateValidator && phoneCondition
            addParticipant.submitButtonEnable(condition)
    }


}