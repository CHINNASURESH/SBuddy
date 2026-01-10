package com.sbuddy.app.ui.group

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private var inviteDialogInput: EditText? = null

    companion object {
        private const val REQUEST_CONTACT = 101
        private const val PERMISSION_REQUEST_CONTACT = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        try {
            groupId = intent.getStringExtra("GROUP_ID") ?: ""
            if (groupId.isEmpty()) {
                Toast.makeText(this, "Invalid Group ID", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

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
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading group details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showInviteDialog() {
        inviteDialogInput = EditText(this)
        inviteDialogInput?.hint = "Friend's Name"
        val container = android.widget.FrameLayout(this)
        val params = android.widget.FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.leftMargin = 50
        params.rightMargin = 50
        inviteDialogInput?.layoutParams = params
        container.addView(inviteDialogInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Invite Friend")
            .setMessage("Enter the name of the friend you want to invite:")
            .setView(container)
            .setPositiveButton("Invite") { _, _ ->
                val name = inviteDialogInput?.text.toString().trim()
                addMemberToGroup(name)
            }
            .setNeutralButton("Pick from Contacts") { _, _ ->
                // This specific click will be overridden in the show() listener below
                // to prevent dialog dismissal, but we handle the logic via a separate button or permission check
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // Override the Neutral button click to handle permission without dismissing immediately
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            checkContactPermission()
            dialog.dismiss()
        }
    }

    private fun addMemberToGroup(name: String) {
        if (name.isNotEmpty()) {
            repository.addMember(groupId, name)
            val updatedGroup = repository.getGroupById(groupId)
            if (updatedGroup != null) {
                adapter.updateList(updatedGroup.members)
                Toast.makeText(this, "$name added to group!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSION_REQUEST_CONTACT
            )
        } else {
            openContactPicker()
        }
    }

    private fun openContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, REQUEST_CONTACT)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CONTACT) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactPicker()
            } else {
                Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CONTACT && resultCode == RESULT_OK) {
            data?.data?.let { contactUri ->
                val cursor = contentResolver.query(contactUri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            val name = it.getString(nameIndex)
                            addMemberToGroup(name)
                        } else {
                             Toast.makeText(this, "Unable to get contact name", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
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
