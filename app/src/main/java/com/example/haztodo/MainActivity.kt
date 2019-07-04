package com.example.haztodo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    companion object {
        private var TAG = MainActivity::class.java.simpleName
    }

    lateinit var recyclerView: RecyclerView
    private val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    lateinit var adapter: RecyclerView.Adapter<*>
    var todos: ArrayList<TodoItem> = ArrayList()
    lateinit var requestService: RequestService

    private val parentJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + parentJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        requestService = RequestService(resources.getString(R.string.todo_url))

        recyclerView = findViewById(R.id.main_recycler_view)
        recyclerView.let {
            it.layoutManager = layoutManager
            it.itemAnimator = DefaultItemAnimator()
        }

        val activityContext = this
        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                try {
                    todos = requestService.getTodos()
                } catch (e: Exception) {
                    displayNotice(recyclerView, "Couldn't fetch todos")
                    Log.d(TAG, e.toString())
                }
            }
            adapter = TodoListAdapter(
                todos,
                DeletionClickListener(activityContext),
                EditClickListener(activityContext),
                EditDoneListener(activityContext)
            )
            recyclerView.adapter = adapter
        }

        fab.setOnClickListener { view ->
            addItem(view)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayNotice(view: View, text: String) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
    }

    private fun addItem(view: View) {
        val newTodo = TodoItem(description = "", id = null)
        todos.add(newTodo)
        adapter.notifyItemInserted(todos.size)

        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                val savedTodo = requestService.createTodo(newTodo)
                newTodo.id = savedTodo.id
            }
            displayNotice(view, "New todo added")
        }

    }

    private fun editItem(v: View) {
        val parentLayout = v.getTag() as LinearLayout
        val index = recyclerView.getChildLayoutPosition(parentLayout)
        Log.d(TAG, "editing at ${index}")

        val txtDesc = parentLayout.findViewById<TextView>(R.id.txt_description)
        val editDesc = parentLayout.findViewById<TextInputEditText>(R.id.edit_description)
        val doneBtn = parentLayout.findViewById<Button>(R.id.btn_done_edit)
        val deleteBtn = parentLayout.findViewById<Button>(R.id.btn_delete)

        fab.visibility = View.GONE
        txtDesc.visibility = View.GONE
        editDesc.setText(txtDesc.text)
        editDesc.visibility = View.VISIBLE
        editDesc.requestFocus()
        doneBtn.visibility = View.VISIBLE
        deleteBtn.visibility = View.GONE
        Utils.openKeyboard(this)
    }

    private fun doneEdit(v: View) {
        // code to execute when EditText loses focus
        val parentLayout = v.getTag() as LinearLayout
        val index = recyclerView.getChildLayoutPosition(parentLayout)
        Log.d(TAG, "editing done at ${index}")

        val txtDesc = parentLayout.findViewById<TextView>(R.id.txt_description)
        val editDesc = parentLayout.findViewById<TextInputEditText>(R.id.edit_description)
        val doneBtn = parentLayout.findViewById<Button>(R.id.btn_done_edit)
        val deleteBtn = parentLayout.findViewById<Button>(R.id.btn_delete)

        Log.d(TAG, "new text is ${editDesc.text.toString()}")
        txtDesc.text = editDesc.text
        val oldText = todos[index].description
        todos[index].description = editDesc.text.toString()

        Utils.closeKeyboard(this)
        editDesc.clearFocus()
        txtDesc.visibility = View.VISIBLE
        editDesc.visibility = View.GONE
        doneBtn.visibility = View.GONE
        deleteBtn.visibility = View.VISIBLE
        fab.visibility = View.VISIBLE

        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                try {
                    requestService.putTodo(todos[index])
                    displayNotice(recyclerView, "Todo saved")
                } catch (e: Exception) {
                    displayNotice(recyclerView, "There was a problem saving your todo")
                    todos[index].description = oldText
                    txtDesc.text = oldText
                }
            }
        }
    }

    private fun removeItem(v: View) {
        val index = recyclerView.getChildLayoutPosition(v.getTag() as LinearLayout)
        Log.d(TAG, "deleting at ${index}")
        val deleted = todos.removeAt(index)
        adapter.notifyItemRemoved(index)

        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                try {
                    requestService.deleteTodo(deleted)
                    displayNotice(recyclerView, "Todo deleted")
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                    withContext(Dispatchers.Main) {
                        displayNotice(recyclerView, "There was a problem deleting your todo")
                        todos.add(index, deleted)
                        adapter.notifyItemInserted(index)
                    }
                }
            }

        }

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
    }

}
