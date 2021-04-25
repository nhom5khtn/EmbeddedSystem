package demo.embeddedsystem.ui

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import demo.embeddedsystem.adapter.ProjectsListAdapter
import demo.embeddedsystem.model.IndoorProject
import demo.embeddedsystem.utils.RecyclerItemClickListener
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults


class HomeActivity : AppCompatActivity() {

    private var realm: Realm? = null
    private var projects: RealmResults<IndoorProject>? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: ProjectsListAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var fab: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
        val realmConfiguration = RealmConfiguration.Builder().build()
        // Create a new empty instance of Realm

        // Clear the realm from last time
//        Realm.deleteRealm(realmConfiguration);
        realm = Realm.getInstance(realmConfiguration)
        projects = realm.where(IndoorProject::class.java).findAll()
        if (projects!!.isEmpty()) {
            Snackbar.make(fab!!, "Empty List, Try creating project", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        // specify an adapter
        mAdapter = ProjectsListAdapter(projects as RealmResults<IndoorProject>)
        mRecyclerView!!.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        mAdapter?.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.settings -> {
                val intent = Intent(this, UnifiedNavigationActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initUI() {
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        fab = findViewById(R.id.fab)
        fab.setOnClickListener(this)
        mRecyclerView = findViewById(R.id.projects_recycler_view)
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
//        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.setLayoutManager(mLayoutManager)
        mRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mRecyclerView.addOnItemTouchListener(RecyclerItemClickListener(this, mRecyclerView, this))
    }

    fun onClick(view: View) {
        if (view.getId() === fab!!.id) {
            val intent = Intent(this, NewProjectActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!realm!!.isClosed) {
            realm!!.close()
        }
    }

    fun onItemClick(view: View?, position: Int) {
        val intent = Intent(this, ProjectDetailActivity::class.java)
        val project: IndoorProject? = projects!![position]
        intent.putExtra("id", project.getId())
        startActivity(intent)
    }

    fun onLongClick(view: View?, position: Int) {}


}