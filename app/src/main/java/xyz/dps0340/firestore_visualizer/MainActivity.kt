package xyz.dps0340.firestore_visualizer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 기본 액션바 hide
        supportActionBar?.hide()


        val isApiKeyExists: Int = resources
            .getIdentifier("google_api_key", "string", packageName)


        val viewerButton = findViewById<Button>(R.id.viewer_button)
        val manualButton = findViewById<Button>(R.id.manual_button)
        val githubButton = findViewById<Button>(R.id.github_button)

        viewerButton.setOnClickListener {
            if(isApiKeyExists == 0) {
                toast("인증 오류: app 폴더에 google-services.json을 추가 후 진행하세요. Firebase에서 발급받을 수 있습니다.")
                return@setOnClickListener
            }
            startActivity<ViewerActivity>()
        }

        manualButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://dps0340.github.io/Firebase-Visualizer/")))
        }

        githubButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DPS0340/Firebase-Visualizer")))
        }
    }
}