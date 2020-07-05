package xyz.dps0340.firestore_visualizer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 기본 액션바 hide
        supportActionBar?.hide()
        // Firestore logging 허용
        FirebaseFirestore.setLoggingEnabled(true)

        val auth = FirebaseAuth.getInstance().currentUser

        if(auth != null) {
            Log.i("FIREBASE", "Current user is $auth")
        } else {
            Log.i("FIREBASE", "Authentication failed")
        }


        val viewerButton = findViewById<Button>(R.id.viewer_button)

        viewerButton.setOnClickListener {
            if(auth == null) {
                toast("인증 오류: app 폴더에 google-services.json을 추가 후 진행하세요. Firebase에서 발급받을 수 있습니다.")
                return@setOnClickListener
            }
            startActivity<ViewerActivity>()
        }
    }
}