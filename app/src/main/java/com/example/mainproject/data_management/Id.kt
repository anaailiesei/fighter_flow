package com.example.mainproject.data_management

data class Id (val letter: String, val value: Int) {
    private val firstValue = 1

    fun getNextId(): Id {
        return if (value < 999)
            Id(letter, value + 1)
        else Id(getNextLetter(), firstValue)
    }

    private fun getNextLetter () : String {
        return (letter[0] + 1).toString()
    }
}