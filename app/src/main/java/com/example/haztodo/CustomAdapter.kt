package com.example.haztodo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import android.view.View.OnFocusChangeListener
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class CustomAdapter(private val todoList: ArrayList<TodoItem>, private val deletionClickListener: View.OnClickListener, private val editClickListener: View.OnClickListener,private val doneEditClickListener: View.OnClickListener) :
    RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var parentLayout: View
        internal var descriptionLayout: View
        internal var descriptionText: TextView
        internal var descriptionEdit: TextInputEditText
        internal var createdDate: TextView
        internal var deleteButton: Button
        internal var doneButton: Button

        init {
            this.parentLayout = itemView.findViewById(R.id.todo_root)
            this.descriptionLayout = itemView.findViewById(R.id.row_description)
            this.descriptionText = itemView.findViewById(R.id.txt_description)
            this.descriptionEdit = itemView.findViewById(R.id.edit_description)
            this.createdDate = itemView.findViewById(R.id.txt_created_date)
            this.deleteButton = itemView.findViewById(R.id.btn_delete)
            this.doneButton = itemView.findViewById(R.id.btn_done_edit)

            this.descriptionLayout.setOnClickListener(editClickListener)
            this.descriptionLayout.tag = parentLayout
            this.doneButton.setOnClickListener(doneEditClickListener)
            this.doneButton.tag = parentLayout

            this.deleteButton.setOnClickListener(deletionClickListener)
            this.deleteButton.tag = parentLayout
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, listPosition: Int) {

        val description = holder.descriptionText
        val descriptionEdit = holder.descriptionEdit
        val createdDate = holder.createdDate

        description.setText(todoList[listPosition].description)
        descriptionEdit.setText(todoList[listPosition].description)

        val format = SimpleDateFormat("dd/MM/yyyy hh:mma", Locale.ENGLISH)
        createdDate.setText(format.format(todoList[listPosition].createdDate))
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

}