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
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    companion object {
        private var TAG = MainActivity::class.java.simpleName
    }

    lateinit var recyclerView: RecyclerView
    private val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    lateinit var adapter: RecyclerView.Adapter<*>
    var todos: ArrayList<TodoItem> = ArrayList()
    val requestService: RequestService = RequestService("http://haz-generest.ap-southeast-1.elasticbeanstalk.com/haztodos")

    private val parentJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + parentJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.main_recycler_view)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        val activityContext = this
        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                try {
                    todos = requestService.getTodos()
                } catch (e: Exception) {
                    Snackbar.make(recyclerView, "Couldn't fetch todos", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()

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

    private fun addItem(view: View) {
        val newTodo = TodoItem(description = "", id = null)
        todos.add(newTodo)
        adapter.notifyItemInserted(todos.size)

        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                val savedTodo = requestService.createTodo(newTodo)
                newTodo.id = savedTodo.id
            }
            Snackbar.make(view, "New todo added", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
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

        txtDesc.visibility = View.GONE
        editDesc.setText(txtDesc.text)
        editDesc.visibility = View.VISIBLE
        editDesc.requestFocus()
        doneBtn.visibility = View.VISIBLE
        deleteBtn.visibility = View.GONE
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

        txtDesc.visibility = View.VISIBLE
        editDesc.visibility = View.GONE
        doneBtn.visibility = View.GONE
        deleteBtn.visibility = View.VISIBLE

        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                try {
                    requestService.putTodo(todos[index])
                    Snackbar.make(recyclerView, "Todo saved", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                } catch (e: Exception) {
                    Snackbar.make(recyclerView, "There was a problem saving your todo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
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
                    Snackbar.make(recyclerView, "Todo deleted", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                    withContext(Dispatchers.Main) {
                        Snackbar.make(recyclerView, "There was a problem deleting your todo", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
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
