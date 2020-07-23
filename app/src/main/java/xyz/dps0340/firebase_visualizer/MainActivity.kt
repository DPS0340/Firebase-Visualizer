package xyz.dps0340.firebase_visualizer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

// 주석 TODO


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 기본 액션바 hide
        supportActionBar?.hide()


        val viewerButton = findViewById<Button>(R.id.viewer_button)
        val manualButton = findViewById<Button>(R.id.manual_button)
        val githubButton = findViewById<Button>(R.id.github_button)

        viewerButton.setOnClickListener {
            startActivity<ViewerActivity>()
        }

        manualButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://dps0340.github.io/Firebase-Visualizer/")))
        }

        githubButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DPS0340/Firebase-Visualizer/")))
        }
    }
}