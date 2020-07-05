package xyz.dps0340.firestore_visualizer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 기본 액션바 hide
        supportActionBar?.hide()

        val viewerButton = findViewById<Button>(R.id.viewer_button)

        viewerButton.setOnClickListener {
            FirebaseFirestore.getInstance()
            startActivity<ViewerActivity>()
        }
    }
}