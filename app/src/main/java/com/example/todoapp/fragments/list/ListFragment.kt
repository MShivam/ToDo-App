package com.example.todoapp.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.*
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.data.ToDoViewModel
import com.example.todoapp.data.models.ToDoData
import com.example.todoapp.databinding.FragmentListBinding
import com.example.todoapp.fragments.SharedViewModel
import com.example.todoapp.fragments.list.adapter.ListAdapter
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val adapter: ListAdapter by lazy { ListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Data Binding
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mSharedViewModel = mSharedViewModel

        //Setup RecyclerView
        setUpRecyclerView()

        //Observing LiveData
        mToDoViewModel.getAllData.observe(viewLifecycleOwner, Observer {
            data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
        })

        //Set Menu
        setHasOptionsMenu(true)

        return binding.root
    }

    private fun setUpRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 150
        }

        //Swipe to delete
        swipeToDelete(recyclerView)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallBack = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                //Delete item
                mToDoViewModel.deleteItem(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                //Restore deleted item
                restoreDeletedData(viewHolder.itemView, deletedItem, viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedData(view: View, deletedItem: ToDoData, position: Int) {
        val snackBar = Snackbar.make(view, "Deleted '${deletedItem.title}'", Snackbar.LENGTH_LONG)
        snackBar.setAction("Undo"){
            mToDoViewModel.insertData(deletedItem)
            adapter.notifyItemChanged(position)
        }
        snackBar.show()
    }

    //-------- Implement and override Menu members --------------//
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(this, Observer { adapter.setData(it) })
            R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(this, Observer { adapter.setData(it) })
        }
        return super.onOptionsItemSelected(item)
    }

    //--------- Implement and override Search query members ----------//
    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText != null) {
            searchThroughDatabase(newText)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        mToDoViewModel.searchDatabase(searchQuery).observe(this, Observer {
            list -> list?.let {
                adapter.setData(it)
            }
        })
    }

    //Show alert dialog to delete everything
    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("DELETE") { _, _ ->
            mToDoViewModel.deleteAll()
            Toast.makeText(requireContext(), "Successfully Deleted Everything!", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("CANCEL") { _, _ -> }
        builder.setTitle("Delete Everything")
        builder.setMessage("You are about to delete everything")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}