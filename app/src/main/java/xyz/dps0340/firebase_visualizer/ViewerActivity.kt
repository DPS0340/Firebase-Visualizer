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


class ViewerActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var root: DatabaseReference
    private lateinit var rootLayout: LinearLayout
    private lateinit var contentLayout: LinearLayout
    private lateinit var inflater: LayoutInflater
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)
        // 기본 액션바 hide
        supportActionBar?.hide()

        database = Firebase.database
        root = database.getReference("/")
        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        contentLayout = findViewById<LinearLayout>(R.id.contentLayout)

        initRootLayout()

        root.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val referenceString: String = getRefString(dataSnapshot.ref)
                Log.i(getString(R.string.LOG_TAG_FIREBASE), "onDataChange Called!")
                Log.i(getString(R.string.LOG_TAG_FIREBASE), "reference String: $referenceString")
                val dataView: View = getParentView(referenceString) ?: return
                refreshSubData(dataView, dataSnapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("FIREBASE_VISUALIZER", "Failed to read value.", error.toException())
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