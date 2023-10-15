package com.example.mainproject.search

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainproject.data_management.DataBase
import com.example.mainproject.MainActivity
import com.example.mainproject.R
import com.example.mainproject.SharedViewModel
import com.example.mainproject.databinding.FragmentSearchBinding
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Locale

class Search : Fragment() {

    companion object {
        fun newInstance() = Search()
    }

    private lateinit var viewModel: SearchViewModel
    private lateinit var binding: FragmentSearchBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var dataBase: DataBase
    private lateinit var hostingActivity : MainActivity


    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater)
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

        // Handlers for view
        searchView = binding.searchView
        recyclerView = binding.listRecyclerView

        // Get the data base
        val sharedViewModel: SharedViewModel by activityViewModels()
        dataBase = sharedViewModel.dataBase!!

        // Set the layout for the recycler view
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        dataBase.getAllParticipants { allParticipants ->
            val itemAdaptor = ArrayListAdaptor(allParticipants)
            recyclerView.adapter = itemAdaptor
        }

        searchView.setOnQueryTextListener(object: OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean { return true }

                override fun onQueryTextChange(newText:  String?): Boolean {
                    if (newText == "") {
                        showAllParticipants()
                    } else if (newText != null){
                        showQueryParticipants(newText)
                    }
                    return true
                }
            })
    }

    private fun showAllParticipants () {
        dataBase.getAllParticipants { allParticipants ->
            val itemAdaptor = ArrayListAdaptor(allParticipants)
            recyclerView.adapter = itemAdaptor
        }
    }

    private fun showQueryParticipants (query : String) {
        val searchable = getSearchable(query)
        dataBase.searchParticipants(searchable) { filteredParticipants ->
            val itemAdaptor = ArrayListAdaptor(filteredParticipants)
            recyclerView.adapter = itemAdaptor
        }
    }

    private fun getSearchable (s: String): String {
        val regex = Regex("[^A-Za-z]")
        return regex.replace(s, "").lowercase(Locale.getDefault())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        // TODO: Use the ViewModel
    }
}