package com.android.doctorapp.ui.doctor.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable

class CustomAutoCompleteAdapter(context: Context, suggestions: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line, suggestions),
    Filterable {

    private var originalSuggestions: List<String> = suggestions.toList()

    override fun getFilter(): Filter {
        return customFilter
    }

    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val filteredList = mutableListOf<String>()

            if (constraint.isNullOrEmpty()) {
                filteredList.addAll(originalSuggestions)
            } else {
                val filterPattern = constraint.toString().lowercase().trim()
                for (suggestion in originalSuggestions) {
                    if (suggestion.lowercase().contains(filterPattern)) {
                        filteredList.add(suggestion)
                    }
                }
            }

            if (filteredList.isEmpty()) {
                filteredList.add(ADD_SUGGESTION_ITEM)
            }

            results.values = filteredList
            results.count = filteredList.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            clear()
            addAll(results?.values as List<String>)
            notifyDataSetChanged()
        }
    }

    companion object {
        const val ADD_SUGGESTION_ITEM = "Add"
    }

}