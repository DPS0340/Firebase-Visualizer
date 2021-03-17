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



/*
    메인 액티비티 클래스
*/
class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
            부모 클래스 AppCompatActivity의 onCreate를 호출한다
            super는 부모 클래스 객체를 가리키는 변수!
            이 부모 클래스의 메소드는 안드로이드 API에서 정의되어 있음(메소드명 ctrl-클릭을 통해 직접 소스를 확인 가능)
        */
        super.onCreate(savedInstanceState)
        /*
            우리가 만든 Res/layout/activity_main.xml UI를 받아서 렌더링해주는 메소드
            매개변수로 실제로 객체가 전달되지는 않고, 단지 int인 id값이 전달된다
        */
        setContentView(R.layout.activity_main)
        // 기본 액션바 hide
        supportActionBar?.hide()

        /*
            레이아웃에 있는 Button 객체를 xml id값으로 가져온다
         */
        val viewerButton = findViewById<Button>(R.id.viewer_button)
        val githubButton = findViewById<Button>(R.id.github_button)

        /*
            viewer_button의 클릭 이벤트를 정의한다
         */
        viewerButton.setOnClickListener {
            startActivity<ViewerActivity>()
        }


        /*
            github_button의 클릭 이벤트를 정의한다
         */
        githubButton.setOnClickListener {
            // 웹 브라우저 Intent가 동작된다
            // 깃허브 url로 이동됨
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KPU-KSLA/Firebase-Visualizer/")))
        }
    }
}