package xyz.dps0340.firestore_visualizer

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.data_layout.*
import kotlinx.android.synthetic.main.data_layout.view.*
import kotlinx.android.synthetic.main.toolbar_main.view.*
import org.jetbrains.anko.image
import org.jetbrains.anko.imageResource


class ViewerActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var root: DatabaseReference
    private lateinit var contentLayout: LinearLayout
    private lateinit var inflater: LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)
        // 기본 액션바 hide
        supportActionBar?.hide()

        database = Firebase.database
        root = database.getReference("/")
        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        contentLayout = findViewById<LinearLayout>(R.id.contentLayout)

        root.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                refreshDataView()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException())
            }
        })
    }

    private fun refreshDataView() {
        contentLayout.removeAllViews()
        root.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                refreshCore(contentLayout, dataSnapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("FIREBASE", "Failed to read value.", error.toException())
            }
        })
    }

    private fun refreshSubData(Parent: View, dataSnapshot: DataSnapshot) {
        Parent.cardView.contentView.data.text = ""
        Parent.cardView.contentView.data.visibility = View.GONE
        val nestedLayout = Parent.cardView.contentView.nestedLayout
        nestedLayout.visibility = View.VISIBLE
        refreshCore(nestedLayout, dataSnapshot)
    }

    private fun refreshCore(Layout: LinearLayout, dataSnapshot: DataSnapshot) {
        for (child in dataSnapshot.children) {
            val newItem = inflater.inflate(R.layout.data_layout, null)
            newItem.layoutParams = TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            newItem.cardView.titleView.dataName.text = child.ref.toString().substring(child.ref.root.toString().length)
            newItem.cardView.contentView.data.text = child.value.toString()
            if(dataSnapshot.child(child.key.toString()).hasChildren()) {
                Log.i("FIREBASE", "${child.key} is nested object")
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
            Layout.addView(newItem)
            Layout.invalidate()
        }
    }
}