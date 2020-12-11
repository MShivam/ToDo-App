package com.example.todoapp.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todoapp.R
import com.example.todoapp.data.ToDoViewModel
import com.example.todoapp.data.models.ToDoData
import com.example.todoapp.databinding.FragmentUpdateBinding
import com.example.todoapp.fragments.SharedViewModel
import kotlinx.android.synthetic.main.fragment_update.*
import kotlinx.android.synthetic.main.fragment_update.view.*

class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()

    private val mSharedViewModel: SharedViewModel by viewModels()
    private val mToDoViewModel: ToDoViewModel by viewModels()

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //Data Binding
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        binding.args = args
        binding.lifecycleOwner = this

        //Set menu
        setHasOptionsMenu(true)

        //Spinner Item Selected Listener
        binding.spinnerPrioritiesUpdate.onItemSelectedListener = mSharedViewModel.listener

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_save -> updateItem()
            R.id.menu_delete -> confirmItemRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateItem() {
        val title = editText_title_update.text.toString()
        val description = editText_description_update.text.toString()
        val getPriority = spinner_priorities_update.selectedItem.toString()

        val validation = mSharedViewModel.verifyDataFromUser(title, description)
        if (validation) {
            //Update current item
            val updateItem = ToDoData(
                args.updateItem.id,
                title,
                mSharedViewModel.parsePriority(getPriority),
                description
            )
            mToDoViewModel.updateData(updateItem)

            Toast.makeText(requireContext(), "Successfully Updated!", Toast.LENGTH_SHORT).show()
            //Navigate back to main view
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please fill out all the fields!", Toast.LENGTH_SHORT).show()
        }
    }

    //Show alert dialog to delete selected item
    private fun confirmItemRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("DELETE") { _, _ ->
            mToDoViewModel.deleteItem(args.updateItem)
            Toast.makeText(requireContext(), "Successfully Deleted: ${args.updateItem.title}", Toast.LENGTH_SHORT).show()
            //Navigate back to main view
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("CANCEL") { _, _ -> }
        builder.setTitle("Delete '${args.updateItem.title}'")
        builder.setMessage("You are about to delete '${args.updateItem.title}'")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}