package com.facebook.pedometer_app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class StepHistoryAdapter(context: Context, private val stepHistoryList: List<StepHistoryItem>)
    : ArrayAdapter<StepHistoryItem>(context, 0, stepHistoryList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_step_history, parent, false)

        val item = stepHistoryList[position]

        val dateText = view.findViewById<TextView>(R.id.dateText)
        val timeText = view.findViewById<TextView>(R.id.timeText)
        val stepCountText = view.findViewById<TextView>(R.id.stepCountText)

        dateText.text = "Date: ${item.date}"
        timeText.text = "Time: ${item.time}"
        stepCountText.text = "Steps: ${item.steps}"

        return view
    }
}
