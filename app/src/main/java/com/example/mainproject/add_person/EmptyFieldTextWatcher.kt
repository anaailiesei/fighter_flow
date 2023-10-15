package com.example.mainproject.add_person

import android.text.Editable
import android.text.TextWatcher
import java.util.Locale

class EmptyFieldTextWatcher(private val addParticipant : AddParticipant, private val type : String) : TextWatcher {
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    // TODO: make this piece of code better
    override fun afterTextChanged(p0: Editable) {
        val nameInput = addParticipant.getNameInputEditable()
        val dateInput = addParticipant.getDateInputEditable()
        /*if (p0.isNotEmpty()) {
            if (p0.equals(nameInput)) {
                val name = capitalizeWordsAfterHyphen(nameInput.text.toString())
                nameInput.setText(name)
                AddParticipant.nameValidator = true
            } else if (p0.equals(dateInput)) {
                AddParticipant.dateValidator = true
            }
        } else {
            if (p0.equals(nameInput)) {
                AddParticipant.nameValidator = false
            } else if (p0.equals(dateInput)) {
                AddParticipant.dateValidator = false
            }
        }*/
        if (p0.isNotEmpty()) {
            if (type == "name") {
                //val name = capitalizeWordsAfterHyphen(nameInput.text.toString())
                //nameInput.setText(name)
                AddParticipant.nameValidator = true
            } else if (type == "date") {
                AddParticipant.dateValidator = true
            }
        } else {
            if (type == "name") {
                AddParticipant.nameValidator = false
            } else if (type == "date") {
                AddParticipant.dateValidator = false
            }
        }
        val phoneCondition = AddParticipant.participantPhoneValidator || AddParticipant.parentPhoneValidator
        val condition = AddParticipant.nameValidator && AddParticipant.dateValidator && phoneCondition
        addParticipant.submitButtonEnable(condition)
    }
}