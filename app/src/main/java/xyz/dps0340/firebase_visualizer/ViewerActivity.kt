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
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.data_layout.*
import kotlinx.android.synthetic.main.data_layout.view.*
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.linearLayout
import java.net.URLDecoder

/*
    뷰어 액티비티의 클래스
 */
class ViewerActivity : AppCompatActivity() {
    /*
        private: 이 클래스에서만 접근 가능한 변수
        lateinit var: 추후 한번의 초기화만 허용하고 그 후에는 수정이 불가능한 변수
        : (타입): 변수 선언시 타입을 지정하여 선언한다.
        자바의 (타입) (변수명);과 비슷하고 변수가 lateinit var로 선언되지 않은 경우는 타입을 생략하여도 무방하다

        상수는 val로 선언함: 실수를 방지하기 위해 바뀔 필요가 없는 변수는 상수로 선언하는것이 권장됨
    */
    private lateinit var database: FirebaseDatabase
    private lateinit var root: DatabaseReference
    private lateinit var rootLayout: LinearLayout
    private lateinit var contentLayout: LinearLayout
    private lateinit var inflater: LayoutInflater
    private lateinit var rootView: View
    val notCheckedText = "출석체크"
    val checkedText = "출석체크해제"

    /*
        액티비티 생성시 호출되는 메소드
        savedInstanceState: Bundle?의 의미:
        onCreate 메소드는 Bundle 타입이나 null일수도 있는 savedInstanceState 매개변수를 가진다
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
            as 연산자: 형변환 연산자
        */

        // 데이터베이스 변수를 가져옴
        database = Firebase.database
        // 데이터베이스의 루트 경로 레퍼런스(주소랑 비슷한 개념)를 가져옴
        root = database.getReference("/")
        // 레이아웃 팩토리 가져옴
        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // xml상의 최상단 레이아웃 가져옴
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
    
    
    /*
        상위 경로에 있는 뷰를 가져오는 메소드
     */
    private fun getParentView(path: String?): View? {
        // 경로값이 null이면 null을 반환
        if(path == null) {
            return null
        }

        // 경로값을 통해 뷰를 가져옴
        val dataView: View? = findDataViewByTitle(path)
        // 경로값의 맨 마지막 /의 인덱스를 가져옴
        val idx: Int = path.lastIndexOf("/")
        // dataView가 있으면 null을 반환
        if(dataView != null) {
            return dataView
        }
        // 더 상위 경로가 없다면 rootView를 반환
        if(idx == -1) {
            return rootView
        }
        // 둘다 아니라면 부모 경로로 재귀호출
        return getParentView(path.substring(0, idx))
    }

    /*
        레이아웃에 있는 뷰를 타이틀을 키로 써서 찾는 메소드
     */
    private fun findDataViewByTitle(id: String?): View? {
        // id가 null이면 null을 반환
        if(id == null) {
            return null
        }
        // 실제 동작이 이루어지는 코어 함수를 호출해서 반환
        return findDataViewCore(id, rootView)
    }

    /*
        findDataViewByTitle의 실제 동작이 이루어지는 메소드
        재귀를 사용
     */
    private fun findDataViewCore(id: String, currentView: View): View? {
        // 타이틀이 일치할 경우 현재 뷰를 반환
        if(currentView.cardView.dataName.text == id) {
            return currentView
        }
        // 일치하지 않을 경우 자식 뷰를 탐색
        for(elem in currentView.cardView.contentView.nestedLayout) {
            // 재귀적으로 탐색한다 - 깊이 우선 탐색을 사용
            val res: View? = findDataViewCore(id, elem)
            // 일치하는 뷰가 있으면 뷰를 반환
            if(res != null) {
                return res
            }
        }
        // 없으면 null 반환
        return null
    }

    /*
        최상위 레이아웃을 만드는 메소드
     */
    private fun initRootLayout() {
        // rootView 초기화
        rootView = inflater.inflate(R.layout.data_layout, null)

        // 제목을 /로 설정
        rootView.cardView.dataName.text = "/"
        // 파일이 아니라 디렉토리 형식이니 스스로의 데이터는 없어야함
        // 빈 문자열로 초기화
        rootView.cardView.contentView.data.text = ""
        // 데이터 부분을 숨긴다
        rootView.cardView.contentView.data.visibility = View.GONE
        // 자식 객체들을 담는 레이아웃 초기화
        rootLayout = rootView.cardView.contentView.nestedLayout
        // 레이아웃을 볼수 있게 한다
        rootLayout.visibility = View.VISIBLE
        // 실제 레이아웃에 바인딩
        contentLayout.addView(rootView)
        // 레이아웃을 다시 렌더링해서 사용자가 볼 수 있게 한다
        contentLayout.invalidate()
        // 타이틀 클릭시 내용이 보이거나 사라지게하는 리스너 추가
        addOnClickToggleVisibleListener(rootView)
    }

    /*
        갱신된 데이터를 이용해서 일부 변화된 레이아웃을 갱신하는 메소드
     */
    private fun refreshSubData(currentView: View, dataSnapshot: DataSnapshot) {
        val cardView = currentView.cardView
        val nestedLayout = cardView.contentView.nestedLayout
        // 현재 객체의 기존 뷰 삭제
        nestedLayout.removeAllViews()
        // 내용 문자열 빈 문자열로 대입 후 숨긴다
        cardView.contentView.data.text = ""
        cardView.contentView.data.visibility = View.GONE
        // 자식 객체를 보여주는 레이아웃을 볼 수 있게 한다
        nestedLayout.visibility = View.VISIBLE
        // 현재 객체의 자식 뷰 갱신
        refreshCore(nestedLayout, dataSnapshot)
    }

    /*
        ref 변수를 통해 파일의 경로를 리턴하는 메소드
     */
    private fun getRefString(ref: DatabaseReference): String {
        /*
            ref을 문자열로 바꾼 후 도메인 부분을 제거한 후 url 인코딩을 원래 문자로 바꾼 후 리턴
         */
        return URLDecoder.decode(ref.toString().substring(ref.root.toString().length), "UTF-8")
    }

    /*
        해당 뷰가 보일때 클릭하면 사라지게 하고,
        뷰가 보이지 않을때 클릭하면 보이게 한다
        클릭시 화살표 이미지도 변경됨
     */
    private fun addOnClickToggleVisibleListener(target: View) {
        target.cardView.DropdownLayout.dropdown.setOnClickListener {
            if(target.cardView.contentView.visibility == View.VISIBLE) {
                target.cardView.contentView.visibility = View.GONE
                target.cardView.DropdownLayout.dropdown.imageResource = R.drawable.down_arrow
            } else {
                target.cardView.contentView.visibility = View.VISIBLE
                target.cardView.DropdownLayout.dropdown.imageResource = R.drawable.up_arrow
            }
        }
    }

    /*
        실제로 레이아웃 갱신이 이루어지는 메소드
     */
    private fun refreshCore(layout: LinearLayout, dataSnapshot: DataSnapshot) {
        // 현재 스냅샷의 자식 객체들을 반복
        for (child in dataSnapshot.children) {
            // 새로운 자식 레이아웃 객체 생성
            val newItem = inflater.inflate(R.layout.data_layout, null)
            // 레이아웃 width, height, weight 설정
            newItem.layoutParams = TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            // 타이틀 이름 대입
            val childRef = getRefString(child.ref)
            if(childRef.startsWith("/cases/") && !childRef.contains("result")) {
                newItem.cardView.dataName.text = childRef.split('/').last()
            } else if(childRef.contains("result") && dataSnapshot.child(child.key.toString()).hasChildren()) {
                newItem.cardView.dataName.text = "result"
            } else if(childRef.contains("result")) {
                newItem.cardView.dataName.text = "result/${childRef.split('/').last()}"
            } else {
                newItem.cardView.dataName.text = childRef
            }
            val checkButton = newItem.cardView.DropdownLayout.checkButton
            if(childRef.startsWith("/cases/") && !childRef.contains("result") && dataSnapshot.child(child.key.toString()).hasChildren()) {
                checkButton.visibility = View.VISIBLE
                child.ref.addListenerForSingleValueEvent(
                    object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val case = snapshot.getValue<Case>()
                            case?.let {
                                if(case.checkedByAdmin) {
                                    checkButton.text = checkedText
                                } else {
                                    checkButton.text = notCheckedText
                                }
                                checkButton.setOnClickListener(object :
                                    View.OnClickListener {
                                        override fun onClick(p0: View?) {
                                            case.checkedByAdmin = !case.checkedByAdmin
                                            child.ref.setValue(case)
                                            if(case.checkedByAdmin) {
                                                checkButton.text = checkedText
                                            } else {
                                                checkButton.text = notCheckedText
                                            }
                                        }
                                })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    }
                )
            }
            // 자식 객체에 또다른 자식들이 있을 경우
            if(dataSnapshot.child(child.key.toString()).hasChildren()) {
                // 디버깅에 용이하게 로그를 남김
                Log.i(getString(R.string.LOG_TAG_FIREBASE), "${child.key} is nested object")
                // 재귀적으로 레이아웃 초기화
                if(childRef.startsWith("/cases/") && !childRef.contains("result")) {
                    refreshSubData(newItem, dataSnapshot.child(child.key.toString()))
                } else {
                    refreshSubData(newItem, dataSnapshot.child(child.key.toString()))
                }
            } else { // 자식들이 없을 경우
                // 데이터 값을 대입
                newItem.cardView.contentView.data.text = child.value.toString()
            }

            // 타이틀 클릭시 내용이 보이거나 사라지게하는 리스너 추가
            addOnClickToggleVisibleListener(newItem)
            // 현재 레이아웃에 자식 뷰 추가
            layout.addView(newItem)
            // 사용자가 볼 수 있게끔 레이아웃을 다시 렌더링
            layout.invalidate()
        }
    }
}