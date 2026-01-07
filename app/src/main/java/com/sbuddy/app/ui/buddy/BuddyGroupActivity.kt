package com.sbuddy.app.ui.buddy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.repository.MatchRepository

class BuddyGroupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buddy_group)

        val btnShare = findViewById<Button>(R.id.btn_share_history)
        btnShare.setOnClickListener {
            shareHistory()
        }
    }

    private fun shareHistory() {
        val history = MatchRepository.getMatches().joinToString("\n") {
            "${it.player1Name} vs ${it.player2Name}: ${it.player1Score}-${it.player2Score}"
        }

        if (history.isEmpty()) {
            Toast.makeText(this, "No history to share", Toast.LENGTH_SHORT).show()
            return
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "My Match History:\n\n$history")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share History via")
        startActivity(shareIntent)
    }
}
