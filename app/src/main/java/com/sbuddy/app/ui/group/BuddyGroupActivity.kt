package com.sbuddy.app.ui.group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sbuddy.app.R
import com.sbuddy.app.data.model.BuddyGroup
import com.sbuddy.app.data.repository.GroupRepository

class BuddyGroupActivity : AppCompatActivity() {

    private val repository = GroupRepository()
    private lateinit var adapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buddy_group)

        val recycler = findViewById<RecyclerView>(R.id.recycler_groups)
        val fab = findViewById<FloatingActionButton>(R.id.fab_add_group)

        adapter = GroupAdapter(repository.getGroups()) { group ->
            // Navigate to Detail
            val intent = Intent(this, GroupDetailActivity::class.java)
            intent.putExtra("GROUP_ID", group.id)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        fab.setOnClickListener {
            showCreateGroupDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh list in case members were added in Detail screen (mock count update)
        adapter.updateList(repository.getGroups())
    }

    private fun showCreateGroupDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_group, null)
        val inputName = dialogView.findViewById<EditText>(R.id.input_group_name)
        val inputDesc = dialogView.findViewById<EditText>(R.id.input_group_desc)

        AlertDialog.Builder(this)
            .setTitle("Create New Group")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = inputName.text.toString().trim()
                val desc = inputDesc.text.toString().trim()
                if (name.isNotEmpty()) {
                    repository.createGroup(name, desc)
                    adapter.updateList(repository.getGroups())
                    Toast.makeText(this, "Group '$name' created!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class GroupAdapter(
    private var groups: List<BuddyGroup>,
    private val onItemClick: (BuddyGroup) -> Unit
) : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txt_group_name)
        val desc: TextView = view.findViewById(R.id.txt_group_desc)
        val count: TextView = view.findViewById(R.id.txt_member_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_buddy_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        holder.name.text = group.name
        holder.desc.text = group.description
        holder.count.text = "${group.memberCount} Members"

        holder.itemView.setOnClickListener {
            onItemClick(group)
        }
    }

    override fun getItemCount() = groups.size

    fun updateList(newGroups: List<BuddyGroup>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
