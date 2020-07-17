package xyz.dps0340.firebase_visualizer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.data_layout.view.*
import org.jetbrains.anko.imageResource
import java.net.URLDecoder

// 주석 TODO


class ViewerActivity : AppCompatActivity() {
    /*
        private: 이 클래스에서만 접근 가능한 변수
        lateinit var: 추후 한번의 초기화만 허용하고 그 후에는 수정이 불가능한 변수
        : (타입): 변수 선언시 타입을 지정하여 선언한다.
        자바의 String a;와 비슷하고 변수가 lateinit var로 선언되지 않은 경우는 생략하여도 무방하다

        상수는 val로 선언함: 실수를 방지하기 위해 바뀔 필요가 없는 변수는 상수로 선언하는것이 권장됨
    */
    private lateinit var database: FirebaseDatabase
    private lateinit var root: DatabaseReference
    private lateinit var rootLayout: LinearLayout
    private lateinit var contentLayout: LinearLayout
    private lateinit var inflater: LayoutInflater
    private lateinit var rootView: View

    /*
        액티비티 생성시 호출되는 메소드
        savedInstanceState: Bundle?의 의미:
        Bundle 타입이나 null일수도 있는 savedInstanceState 매개변수를 가진다
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
            부모 클래스 AppCompatActivity의 onCreate를 호출한다
            super는 부모 클래스 객체를 가리키는 변수!
            이 부모 클래스의 메소드는 안드로이드 API에서 정의되어 있음(메소드명 ctrl-클릭을 통해 직접 소스를 확인 가능)
        */
        super.onCreate(savedInstanceState)
        /*
            우리가 만든 Res/layout/activity_viewer.xml UI를 받아서 렌더링해주는 메소드
            매개변수로 실제로 객체가 전달되지는 않고, 단지 int인 id값이 전달된다
        */
        setContentView(R.layout.activity_viewer)
        /*
            툴바의 xml을 따로 정의하였고 쓸 예정이므로 기본 액션바를 숨긴다
        */
        supportActionBar?.hide()

        /*
            여러 lateinit var 변수 초기화

         */
        database = Firebase.database
        root = database.getReference("/")
        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        contentLayout = findViewById<LinearLayout>(R.id.contentLayout)

        
        /*
            밑에서 정의한 동적 레이아웃 초기화 메소드를 호출
        */
        initRootLayout()

        /*
            우리가 생성한 Firebase DB가 수정될 시 호출되는 메소드를 작성
            이벤트가 생기면 리스너를 통해 호출되는 방식 - event driven
            지금 정의하는 메소드는 리스너 생성 직후에도 초기화를 위해 한번 호출된다
        */
        root.addValueEventListener(object: ValueEventListener { // 중괄호 생성자를 통해 리스너 초기화
            /*
                DB가 수정될 시 호출되는 메소드
                dataSnapshot 변수는 DB가 수정된 경로의 데이터만 가지고 있는 지역적인 데이터이다
             */
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 수정된 데이터의 절대 경로를 가져온다
                val referenceString: String = getRefString(dataSnapshot.ref)
                // 추후 디버깅에 편리하게 로그를 남긴다
                Log.i(getString(R.string.LOG_TAG_FIREBASE), "onDataChange Called!")
                Log.i(getString(R.string.LOG_TAG_FIREBASE), "reference String: $referenceString")
                // 상위 뷰를 가져오고, 혹시나 null값이면 return으로 바로 함수를 종료한다
                val dataView: View = getParentView(referenceString) ?: return
                // 상위 뷰와 변화된 경로의 데이터를 사용해서 사용자가 보고 있는 레이아웃을 업데이트한다
                refreshSubData(dataView, dataSnapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                // 데이터 변화시 값을 읽는데 실패하였음 - 오류 발생
                Log.w(getString(R.string.LOG_TAG_FIREBASE), "Failed to read value.", error.toException())
            }
        })
    }

    private fun getParentView(key: String?): View? {
        if(key == null) {
            return null
        }

        val dataView: View? = findDataViewByIdentifier(key)
        val idx: Int = key.lastIndexOf("/")
        if(dataView != null) {
            return dataView
        }
        if(idx == -1) {
            return rootView
        }
        return getParentView(key.substring(0, idx))
    }

    private fun findDataViewByIdentifier(id: String?): View? {
        if(id == null) {
            return null
        }
        return findDataViewCore(id, rootView)
    }

    private fun findDataViewCore(id: String, parent: View): View? {
        if(parent.identifierName.text == id) {
            return parent
        }
        for(elem in parent.cardView.contentView.nestedLayout) {
            val res: View? = findDataViewCore(id, elem)
            if(res != null) {
                return res
            }
        }
        return null
    }

    private fun initRootLayout() {
        rootView = inflater.inflate(R.layout.data_layout, null)

        rootView.cardView.titleView.dataName.text = "/"
        rootView.cardView.contentView.data.text = ""
        rootView.cardView.contentView.data.visibility = View.GONE
        rootLayout = rootView.cardView.contentView.nestedLayout
        rootLayout.visibility = View.VISIBLE
        contentLayout.addView(rootView)
        contentLayout.invalidate()
        rootView.cardView.titleView.setOnClickListener {
            if(rootView.cardView.contentView.visibility == View.VISIBLE) {
                rootView.cardView.contentView.visibility = View.GONE
                rootView.cardView.titleView.dropdown.imageResource = R.drawable.down_arrow
            } else {
                rootView.cardView.contentView.visibility = View.VISIBLE
                rootView.cardView.titleView.dropdown.imageResource = R.drawable.up_arrow
            }
        }
    }


    private fun refreshSubData(parent: View, dataSnapshot: DataSnapshot) {
        val cardView = parent.cardView
        val nestedLayout = cardView.contentView.nestedLayout
        nestedLayout.removeAllViews()
        cardView.contentView.data.text = ""
        cardView.contentView.data.visibility = View.GONE
        nestedLayout.visibility = View.VISIBLE
        refreshCore(nestedLayout, dataSnapshot)
    }

    private fun getRefString(ref: DatabaseReference): String {
        return URLDecoder.decode(ref.toString().substring(ref.root.toString().length), "UTF-8")
    }

    private fun refreshCore(layout: LinearLayout, dataSnapshot: DataSnapshot) {
        for (child in dataSnapshot.children) {
            val newItem = inflater.inflate(R.layout.data_layout, null)
            newItem.layoutParams = TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            newItem.cardView.titleView.dataName.text = getRefString(child.ref)
            newItem.cardView.contentView.data.text = child.value.toString()
            newItem.identifierName.text = newItem.cardView.titleView.dataName.text
            if(dataSnapshot.child(child.key.toString()).hasChildren()) {
                Log.i(getString(R.string.LOG_TAG_FIREBASE), "${child.key} is nested object")
                refreshSubData(newItem, dataSnapshot.child(child.key.toString()))
            } else {
                newItem.cardView.contentView.data.text = child.value.toString()
            }
            newItem.cardView.titleView.setOnClickListener {
                if(newItem.cardView.contentView.visibility == View.VISIBLE) {
                    newItem.cardView.contentView.visibility = View.GONE
                    newItem.cardView.titleView.dropdown.imageResource = R.drawable.down_arrow
                } else {
                    newItem.cardView.contentView.visibility = View.VISIBLE
                    newItem.cardView.titleView.dropdown.imageResource = R.drawable.up_arrow
                }
            }
            layout.addView(newItem)
            layout.invalidate()
        }
    }
}