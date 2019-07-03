package com.example.haztodo

import android.content.Context
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

import kotlinx.android.synthetic.main.activity_main.*
import android.app.Activity
import android.view.inputmethod.InputMethodManager


class MainActivity : AppCompatActivity() {

    companion object {
        private var TAG = MainActivity::class.java.simpleName
    }

    lateinit var recyclerView: RecyclerView
    private val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    lateinit var adapter: RecyclerView.Adapter<*>
    val todos: ArrayList<TodoItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.main_recycler_view)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        //todo delete stub data
        todos.add(TodoItem(description = "test1"))
        todos.add(TodoItem(description = "test2"))

        adapter = CustomAdapter(todos, DeletionClickListener(this), EditClickListener(this), EditDoneListener(this))
        recyclerView.adapter = adapter

        fab.setOnClickListener { view ->
            Snackbar.make(view, "New todo added", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            todos.add(TodoItem(description = ""))
            adapter.notifyItemInserted(todos.size)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editItem(v: View) {
        val parentLayout = v.getTag() as LinearLayout
        val selectedItemPosition = recyclerView.getChildLayoutPosition(parentLayout)
        Log.d(TAG, "editing at ${selectedItemPosition}")

        val txtDesc = parentLayout.findViewById<TextView>(R.id.txt_description)
        val editDesc = parentLayout.findViewById<TextInputEditText>(R.id.edit_description)
        val doneBtn = parentLayout.findViewById<Button>(R.id.btn_done_edit)
        val deleteBtn = parentLayout.findViewById<Button>(R.id.btn_delete)

        txtDesc.visibility = View.GONE
        editDesc.visibility = View.VISIBLE
        doneBtn.visibility = View.VISIBLE
        deleteBtn.visibility = View.GONE
    }

    private fun doneEdit(v: View) {
        // code to execute when EditText loses focus
        val parentLayout = v.getTag() as LinearLayout
        val selectedItemPosition = recyclerView.getChildLayoutPosition(parentLayout)
        Log.d(TAG, "editing done at ${selectedItemPosition}")

        val txtDesc = parentLayout.findViewById<TextView>(R.id.txt_description)
        val editDesc = parentLayout.findViewById<TextInputEditText>(R.id.edit_description)
        val doneBtn = parentLayout.findViewById<Button>(R.id.btn_done_edit)
        val deleteBtn = parentLayout.findViewById<Button>(R.id.btn_delete)

        Log.d(TAG, "new text is ${editDesc.text.toString()}")
        txtDesc.text = editDesc.text
        todos[selectedItemPosition].description = editDesc.text.toString()

        txtDesc.visibility = View.VISIBLE
        editDesc.visibility = View.GONE
        doneBtn.visibility = View.GONE
        deleteBtn.visibility = View.VISIBLE
    }

    inner class EditClickListener constructor(val context: Context) : View.OnClickListener {

        override fun onClick(v: View) {
            editItem(v)
        }
    }

    inner class EditDoneListener constructor(val context: Context) : View.OnClickListener {

        override fun onClick(v: View) {
            doneEdit(v)
        }
    }

    inner class DeletionClickListener constructor(val context: Context) : View.OnClickListener {

        override fun onClick(v: View) {
            removeItem(v)
        }

        private fun removeItem(v: View) {
            val selectedItemPosition = recyclerView.getChildLayoutPosition(v.getTag() as LinearLayout)
            Log.d(TAG, "deleting at ${selectedItemPosition}")
            todos.removeAt(selectedItemPosition)
            adapter.notifyItemRemoved(selectedItemPosition)

        }
    }

}
