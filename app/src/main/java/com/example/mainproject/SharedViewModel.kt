package com.example.mainproject

import androidx.lifecycle.ViewModel
import com.example.mainproject.data_management.DataBase

class SharedViewModel : ViewModel() {
    var dataBase : DataBase? = null
}