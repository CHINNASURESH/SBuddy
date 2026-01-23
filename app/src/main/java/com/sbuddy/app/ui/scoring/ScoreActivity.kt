package com.sbuddy.app.ui.scoring

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.sbuddy.app.BaseActivity
import com.sbuddy.app.R
import com.sbuddy.app.data.model.Match
import com.sbuddy.app.data.repository.MatchRepository
import com.sbuddy.app.utils.GameLogic
import java.util.Stack
import java.util.UUID

class ScoreActivity : BaseActivity() {

    private var scoreP1 = 0
    private var scoreP2 = 0
    private val gameLogic = GameLogic()
    private var currentServer = "Team 1"

    private var maxScore = 21
    private var team1Name = "Team 1"
    private var team2Name = "Team 2"
    private var isSingles = false

    // Doubles specific state
    private var p1Name = "Player 1"
    private var p2Name = "Player 2"
    private var p3Name = "Player 3"
    private var p4Name = "Player 4"

    private var matchId: String? = null
    private var tournamentId: String? = null
    private var matchLabel: String? = null

    private var t1LeftName = ""
    private var t1RightName = ""
    private var t2LeftName = ""
    private var t2RightName = ""

    private lateinit var repository: MatchRepository

    // UI Components
    private lateinit var txtScoreP1: TextView
    private lateinit var txtScoreP2: TextView
    private lateinit var txtTeam1: TextView
    private lateinit var txtTeam2: TextView
    private lateinit var layoutTeam1Names: LinearLayout
    private lateinit var layoutTeam2Names: LinearLayout
    private lateinit var txtT1Left: TextView
    private lateinit var txtT1Right: TextView
    private lateinit var txtT2Left: TextView
    private lateinit var txtT2Right: TextView
    private lateinit var cardTeam1: CardView
    private lateinit var cardTeam2: CardView
    private lateinit var lblServingT1: TextView
    private lateinit var lblServingT2: TextView
    private lateinit var indLeftT1: TextView
    private lateinit var indRightT1: TextView
    private lateinit var indLeftT2: TextView
    private lateinit var indRightT2: TextView

    // History stack for Undo
    private data class GameState(
        val scoreP1: Int,
        val scoreP2: Int,
        val currentServer: String,
        val t1Left: String,
        val t1Right: String,
        val t2Left: String,
        val t2Right: String,
        val team1Name: String,
        val team2Name: String
    )
    private val history = Stack<GameState>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        repository = MatchRepository(applicationContext)

        if (savedInstanceState != null) {
            matchId = savedInstanceState.getString("MATCH_ID")
            tournamentId = savedInstanceState.getString("TOURNAMENT_ID")
            matchLabel = savedInstanceState.getString("MATCH_LABEL")

            scoreP1 = savedInstanceState.getInt("SCORE_P1", 0)
            scoreP2 = savedInstanceState.getInt("SCORE_P2", 0)
            currentServer = savedInstanceState.getString("CURRENT_SERVER", "Team 1")
            maxScore = savedInstanceState.getInt("MAX_SCORE", 21)
            team1Name = savedInstanceState.getString("TEAM_1_NAME", "Team 1")
            team2Name = savedInstanceState.getString("TEAM_2_NAME", "Team 2")
            isSingles = savedInstanceState.getBoolean("IS_SINGLES", false)

            p1Name = savedInstanceState.getString("P1_NAME", "Player 1")
            p2Name = savedInstanceState.getString("P2_NAME", "Player 2")
            p3Name = savedInstanceState.getString("P3_NAME", "Player 3")
            p4Name = savedInstanceState.getString("P4_NAME", "Player 4")

            t1LeftName = savedInstanceState.getString("T1_LEFT", p2Name)
            t1RightName = savedInstanceState.getString("T1_RIGHT", p1Name)
            t2LeftName = savedInstanceState.getString("T2_LEFT", p4Name)
            t2RightName = savedInstanceState.getString("T2_RIGHT", p3Name)
        } else {
            matchId = intent.getStringExtra("MATCH_ID")
            tournamentId = intent.getStringExtra("TOURNAMENT_ID")
            matchLabel = intent.getStringExtra("MATCH_LABEL")

            maxScore = intent.getIntExtra("MAX_SCORE", 21)
            team1Name = intent.getStringExtra("TEAM_1_NAME") ?: "Team 1"
            team2Name = intent.getStringExtra("TEAM_2_NAME") ?: "Team 2"
            isSingles = intent.getBooleanExtra("IS_SINGLES", false)

            p1Name = intent.getStringExtra("PLAYER_1_NAME") ?: "Player 1"
            p2Name = intent.getStringExtra("PLAYER_2_NAME") ?: "Player 2"
            p3Name = intent.getStringExtra("PLAYER_3_NAME") ?: "Player 3"
            p4Name = intent.getStringExtra("PLAYER_4_NAME") ?: "Player 4"

            // Initial positions: P1 Right, P2 Left; P3 Right, P4 Left
            t1RightName = p1Name
            t1LeftName = p2Name
            t2RightName = p3Name
            t2LeftName = p4Name
        }

        initializeViews()
        setupListeners()
        updateUI()
    }

    private fun initializeViews() {
        val txtInfo = findViewById<TextView>(R.id.txt_match_info)
        val typeStr = if (isSingles) "Singles" else "Doubles"
        txtInfo.text = "$typeStr Match • First to $maxScore"

        txtTeam1 = findViewById(R.id.txt_team1_name)
        txtTeam2 = findViewById(R.id.txt_team2_name)
        layoutTeam1Names = findViewById(R.id.layout_team1_names)
        layoutTeam2Names = findViewById(R.id.layout_team2_names)

        if (isSingles) {
            layoutTeam1Names.visibility = View.GONE
            layoutTeam2Names.visibility = View.GONE
            txtTeam1.visibility = View.VISIBLE
            txtTeam2.visibility = View.VISIBLE
            txtTeam1.text = team1Name
            txtTeam2.text = team2Name
        } else {
            layoutTeam1Names.visibility = View.VISIBLE
            layoutTeam2Names.visibility = View.VISIBLE
            txtTeam1.visibility = View.GONE
            txtTeam2.visibility = View.GONE
        }

        txtScoreP1 = findViewById(R.id.score_p1)
        txtScoreP2 = findViewById(R.id.score_p2)

        txtT1Left = findViewById(R.id.txt_t1_left)
        txtT1Right = findViewById(R.id.txt_t1_right)
        txtT2Left = findViewById(R.id.txt_t2_left)
        txtT2Right = findViewById(R.id.txt_t2_right)

        cardTeam1 = findViewById(R.id.card_team1)
        cardTeam2 = findViewById(R.id.card_team2)
        lblServingT1 = findViewById(R.id.lbl_serving_t1)
        lblServingT2 = findViewById(R.id.lbl_serving_t2)

        indLeftT1 = findViewById(R.id.indicator_serve_left_t1)
        indRightT1 = findViewById(R.id.indicator_serve_right_t1)
        indLeftT2 = findViewById(R.id.indicator_serve_left_t2)
        indRightT2 = findViewById(R.id.indicator_serve_right_t2)
    }

    private fun setupListeners() {
        val btnP1Add = findViewById<Button>(R.id.btn_p1_add)
        val btnP1Minus = findViewById<Button>(R.id.btn_p1_minus)
        val btnP2Add = findViewById<Button>(R.id.btn_p2_add)
        val btnP2Minus = findViewById<Button>(R.id.btn_p2_minus)
        val btnReset = findViewById<Button>(R.id.btn_reset)
        val btnSwapNames = findViewById<android.widget.ImageButton>(R.id.btn_swap_names)
        val btnSwapCourt = findViewById<android.widget.ImageButton>(R.id.btn_swap_court)

        btnP1Add.setOnClickListener {
            saveState()
            if (currentServer == "Team 1") {
                // Server wins point -> Swap positions
                if (!isSingles) {
                    val temp = t1LeftName
                    t1LeftName = t1RightName
                    t1RightName = temp
                }
            } else {
                // Receiver wins point -> Service Change -> No swap
                currentServer = "Team 1"
            }
            scoreP1++
            updateUI()
            checkGameOver()
        }

        btnP1Minus.setOnClickListener {
            restoreState()
        }

        btnP2Add.setOnClickListener {
            saveState()
            if (currentServer == "Team 2") {
                // Server wins point -> Swap positions
                if (!isSingles) {
                    val temp = t2LeftName
                    t2LeftName = t2RightName
                    t2RightName = temp
                }
            } else {
                // Receiver wins point -> Service Change -> No swap
                currentServer = "Team 2"
            }
            scoreP2++
            updateUI()
            checkGameOver()
        }

        btnP2Minus.setOnClickListener {
            restoreState()
        }

        btnSwapNames.setOnClickListener {
            saveState()
            val tempName = team1Name
            team1Name = team2Name
            team2Name = tempName
            updateUI()
        }

        btnSwapCourt.setOnClickListener {
            saveState()
            val tempT1Name = team1Name
            team1Name = team2Name
            team2Name = tempT1Name

            if (!isSingles) {
                val tempL = t1LeftName
                val tempR = t1RightName
                t1LeftName = t2LeftName
                t1RightName = t2RightName
                t2LeftName = tempL
                t2RightName = tempR
            }

            val tempScore = scoreP1
            scoreP1 = scoreP2
            scoreP2 = tempScore

            currentServer = if (currentServer == "Team 1") "Team 2" else "Team 1"

            updateUI()
        }

        btnReset.setOnClickListener {
            scoreP1 = 0
            scoreP2 = 0
            currentServer = "Team 1"
            history.clear()
            if (!isSingles) {
                t1RightName = p1Name
                t1LeftName = p2Name
                t2RightName = p3Name
                t2LeftName = p4Name
            }
            updateUI()
        }

        if (!isSingles) {
            val listener = View.OnClickListener { v ->
                saveState()
                when (v.id) {
                    R.id.txt_t1_left -> {
                        currentServer = "Team 1"
                        if (scoreP1 % 2 == 0) {
                             val temp = t1LeftName
                             t1LeftName = t1RightName
                             t1RightName = temp
                        }
                    }
                    R.id.txt_t1_right -> {
                        currentServer = "Team 1"
                        if (scoreP1 % 2 != 0) {
                             val temp = t1LeftName
                             t1LeftName = t1RightName
                             t1RightName = temp
                        }
                    }
                    R.id.txt_t2_left -> {
                        currentServer = "Team 2"
                        if (scoreP2 % 2 == 0) {
                             val temp = t2LeftName
                             t2LeftName = t2RightName
                             t2RightName = temp
                        }
                    }
                    R.id.txt_t2_right -> {
                        currentServer = "Team 2"
                        if (scoreP2 % 2 != 0) {
                             val temp = t2LeftName
                             t2LeftName = t2RightName
                             t2RightName = temp
                        }
                    }
                }
                updateUI()
            }
            txtT1Left.setOnClickListener(listener)
            txtT1Right.setOnClickListener(listener)
            txtT2Left.setOnClickListener(listener)
            txtT2Right.setOnClickListener(listener)

            layoutTeam1Names.setOnLongClickListener {
                saveState()
                val temp = t1LeftName
                t1LeftName = t1RightName
                t1RightName = temp
                updateUI()
                true
            }

            layoutTeam2Names.setOnLongClickListener {
                saveState()
                val temp = t2LeftName
                t2LeftName = t2RightName
                t2RightName = temp
                updateUI()
                true
            }
        }
    }

    private fun updateUI() {
        txtScoreP1.text = scoreP1.toString()
        txtScoreP2.text = scoreP2.toString()

        if (!isSingles) {
            txtT1Left.text = t1LeftName
            txtT1Right.text = t1RightName
            txtT2Left.text = t2LeftName
            txtT2Right.text = t2RightName

            txtT1Left.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            txtT1Right.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            txtT2Left.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            txtT2Right.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

            if (currentServer == "Team 1") {
                if (scoreP1 % 2 == 0) {
                    txtT1Right.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_shuttlecock, 0)
                } else {
                    txtT1Left.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_shuttlecock, 0)
                }
            } else {
                if (scoreP2 % 2 == 0) {
                    txtT2Right.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_shuttlecock, 0)
                } else {
                    txtT2Left.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_shuttlecock, 0)
                }
            }
        } else {
                txtTeam1.text = team1Name
                txtTeam2.text = team2Name
        }

        indLeftT1.visibility = View.INVISIBLE
        indRightT1.visibility = View.INVISIBLE
        indLeftT2.visibility = View.INVISIBLE
        indRightT2.visibility = View.INVISIBLE

        if (currentServer == "Team 1") {
            cardTeam1.setCardBackgroundColor(Color.parseColor("#E1BEE7"))
            cardTeam2.setCardBackgroundColor(Color.WHITE)
            lblServingT1.visibility = View.VISIBLE
            lblServingT2.visibility = View.INVISIBLE

            if (scoreP1 % 2 == 0) {
                indRightT1.visibility = View.VISIBLE
            } else {
                indLeftT1.visibility = View.VISIBLE
            }
        } else {
            cardTeam1.setCardBackgroundColor(Color.WHITE)
            cardTeam2.setCardBackgroundColor(Color.parseColor("#E1BEE7"))
            lblServingT1.visibility = View.INVISIBLE
            lblServingT2.visibility = View.VISIBLE

            if (scoreP2 % 2 == 0) {
                indRightT2.visibility = View.VISIBLE
            } else {
                indLeftT2.visibility = View.VISIBLE
            }
        }
    }

    private fun saveState() {
        history.push(GameState(scoreP1, scoreP2, currentServer, t1LeftName, t1RightName, t2LeftName, t2RightName, team1Name, team2Name))
    }

    private fun restoreState() {
        if (history.isNotEmpty()) {
            val state = history.pop()
            scoreP1 = state.scoreP1
            scoreP2 = state.scoreP2
            currentServer = state.currentServer
            t1LeftName = state.t1Left
            t1RightName = state.t1Right
            t2LeftName = state.t2Left
            t2RightName = state.t2Right
            team1Name = state.team1Name
            team2Name = state.team2Name
            updateUI()
        }
    }

    private fun checkGameOver() {
        var gameOver = false
        var winner = ""

        if (maxScore == 30) {
            if (scoreP1 >= 30) {
                gameOver = true
                winner = team1Name
            } else if (scoreP2 >= 30) {
                gameOver = true
                winner = team2Name
            }
        } else {
            if (scoreP1 >= maxScore && (scoreP1 - scoreP2) >= 2) {
                gameOver = true
                winner = team1Name
            } else if (scoreP2 >= maxScore && (scoreP2 - scoreP1) >= 2) {
                gameOver = true
                winner = team2Name
            } else if (scoreP1 >= 30 || scoreP2 >= 30) {
                    if (scoreP1 > scoreP2) {
                        gameOver = true
                        winner = team1Name
                    } else {
                        gameOver = true
                        winner = team2Name
                    }
            }
        }

        if (gameOver) {
            saveGame(winner)
            showGameOverDialog(winner, "$scoreP1 - $scoreP2")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("MATCH_ID", matchId)
        outState.putString("TOURNAMENT_ID", tournamentId)
        outState.putString("MATCH_LABEL", matchLabel)

        outState.putInt("SCORE_P1", scoreP1)
        outState.putInt("SCORE_P2", scoreP2)
        outState.putString("CURRENT_SERVER", currentServer)
        outState.putInt("MAX_SCORE", maxScore)
        outState.putString("TEAM_1_NAME", team1Name)
        outState.putString("TEAM_2_NAME", team2Name)
        outState.putBoolean("IS_SINGLES", isSingles)
        outState.putString("P1_NAME", p1Name)
        outState.putString("P2_NAME", p2Name)
        outState.putString("P3_NAME", p3Name)
        outState.putString("P4_NAME", p4Name)
        outState.putString("T1_LEFT", t1LeftName)
        outState.putString("T1_RIGHT", t1RightName)
        outState.putString("T2_LEFT", t2LeftName)
        outState.putString("T2_RIGHT", t2RightName)
    }

    private fun saveGame(winner: String) {
        val idToUse = matchId ?: UUID.randomUUID().toString()
        val match = Match(
            id = idToUse,
            player1Name = team1Name,
            player2Name = team2Name,
            player1Score = scoreP1,
            player2Score = scoreP2,
            timestamp = System.currentTimeMillis(),
            winner = winner,
            isSingles = isSingles,
            matchLabel = matchLabel ?: ""
        )
        repository.saveMatch(match)

        if (tournamentId != null) {
            val resultIntent = android.content.Intent()
            resultIntent.putExtra("MATCH_ID", idToUse)
            resultIntent.putExtra("WINNER", winner)
            resultIntent.putExtra("SCORE_P1", scoreP1)
            resultIntent.putExtra("SCORE_P2", scoreP2)
            setResult(RESULT_OK, resultIntent)
        }
    }

    private fun showGameOverDialog(winnerName: String, finalScore: String) {
        val overlay = findViewById<View>(R.id.overlay_game_over)
        val txtWinner = findViewById<TextView>(R.id.txt_winner_name)
        val btnNewGame = findViewById<Button>(R.id.btn_new_game)
        val btnRematch = findViewById<Button>(R.id.btn_rematch)

        txtWinner.text = "$winnerName wins!\nFinal Score: $finalScore"
        overlay.visibility = View.VISIBLE

        btnNewGame.setOnClickListener {
            finish()
        }

        btnRematch.setOnClickListener {
            scoreP1 = 0
            scoreP2 = 0
            currentServer = "Team 1"
            history.clear()
            val txtScoreP1 = findViewById<TextView>(R.id.score_p1)
            val txtScoreP2 = findViewById<TextView>(R.id.score_p2)
            txtScoreP1.text = "0"
            txtScoreP2.text = "0"
            if (!isSingles) {
                t1RightName = p1Name
                t1LeftName = p2Name
                t2RightName = p3Name
                t2LeftName = p4Name
            }
            overlay.visibility = View.GONE
            updateUI()
        }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Exit Match?")
            .setMessage("Current progress will be lost. Are you sure?")
            .setPositiveButton("Yes") { _, _ -> super.onBackPressed() }
            .setNegativeButton("No", null)
            .show()
    }
}
