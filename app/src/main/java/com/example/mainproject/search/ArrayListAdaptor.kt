package com.example.mainproject.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainproject.R
import java.util.Locale


class ArrayListAdaptor(private val dataSet: ArrayList<ListItemModel>) :
    RecyclerView.Adapter<ArrayListAdaptor.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TODO: Dummy code, maybe u have smth to do with it
        /*val textView: TextView
            init {
                // Define click listener for the ViewHolder's View
                textView = view.findViewById(R.id.textView)
            }*/
        val id = itemView.findViewById<TextView>(R.id.participant_id)
        val name = itemView.findViewById<TextView>(R.id.participant_name)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_view, viewGroup, false)
        // TODO: Set onclick listener for profile
        view.setOnClickListener {

        }
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.id.text = dataSet[position].Id
        viewHolder.name.text = dataSet[position].Name
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}

data class ListItemModel (var Id : String = "", var Name : String = "")
