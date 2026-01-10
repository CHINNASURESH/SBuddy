package com.sbuddy.app.ui.group

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.repository.GroupRepository
import com.sbuddy.app.ui.history.MatchHistoryActivity

class GroupDetailActivity : BaseActivity() {

    private val repository = GroupRepository()
    private lateinit var adapter: MemberAdapter
    private var groupId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        groupId = intent.getStringExtra("GROUP_ID") ?: return
        val group = repository.getGroupById(groupId)

        if (group == null) {
            Toast.makeText(this, "Group not found or error loading data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.txt_detail_group_name).text = group.name
        findViewById<TextView>(R.id.txt_detail_group_desc).text = group.description

        val recycler = findViewById<RecyclerView>(R.id.recycler_members)
        val fab = findViewById<ExtendedFloatingActionButton>(R.id.fab_invite_member)

        adapter = MemberAdapter(group.members) { memberName ->
            // Navigate to History
            val intent = Intent(this, MatchHistoryActivity::class.java)
            intent.putExtra("USER_NAME", memberName)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        fab.setOnClickListener {
            showInviteDialog()
        }
    }

    private fun showInviteDialog() {
        val input = EditText(this)
        input.hint = "Friend's Name"
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = 50
        params.rightMargin = 50
        input.layoutParams = params
        container.addView(input)

        AlertDialog.Builder(this)
            .setTitle("Invite Friend")
            .setMessage("Enter the name of the friend you want to invite:")
            .setView(container)
            .setPositiveButton("Invite") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    repository.addMember(groupId, name)
                    val updatedGroup = repository.getGroupById(groupId)
                    if (updatedGroup != null) {
                        adapter.updateList(updatedGroup.members)
                        Toast.makeText(this, "$name added to group!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNeutralButton("Share Link") { _, _ ->
                val group = repository.getGroupById(groupId)
                if (group != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Join my badminton group '${group.name}' on SBuddy!")
                    }
                    startActivity(Intent.createChooser(shareIntent, "Invite Friend via"))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class MemberAdapter(
    private var members: List<String>,
    private val onHistoryClick: (String) -> Unit
) : RecyclerView.Adapter<MemberAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txt_member_name)
        val btnHistory: Button = view.findViewById(R.id.btn_view_history)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memberName = members[position]
        holder.name.text = memberName
        holder.btnHistory.setOnClickListener {
            onHistoryClick(memberName)
        }
    }

    override fun getItemCount() = members.size

    fun updateList(newMembers: List<String>) {
        members = newMembers
        notifyDataSetChanged()
    }
}
