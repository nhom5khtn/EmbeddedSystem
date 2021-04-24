package demo.embeddedsystem.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import demo.embeddedsystem.R
import demo.embeddedsystem.adapter.holder.AccessPointSection
import demo.embeddedsystem.adapter.holder.ReferencePointSection
import demo.embeddedsystem.model.IndoorProject
import demo.embeddedsystem.utils.RecyclerItemClickListener
import io.realm.Realm

class ProjectDetailActivity : AppCompatActivity(), View.OnClickListener, RecyclerItemClickListener.OnItemClickListener {
    private var pointRV: RecyclerView? = null
    private var btnAddAp: Button? = null
    private var btnAddRp: Button? = null
    private var btnLocateMe: Button? = null
    private var project: IndoorProject? = null
    private val sectionAdapter: SectionedRecyclerViewAdapter = SectionedRecyclerViewAdapter()
    private var rpSec: ReferencePointSection? = null
    private var apSec: AccessPointSection? = null
    private var layoutManager: LinearLayoutManager? = null
    private var projectId: String? = null
    private val PERM_REQ_CODE_RP_ACCESS_COARSE_LOCATION = 198
    private val PERM_REQ_CODE_LM_ACCESS_COARSE_LOCATION = 197
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_detail)
        projectId = intent.getStringExtra("id")
        if (projectId == null) {
            Toast.makeText(applicationContext, "Project Not Found", Toast.LENGTH_LONG).show()
            finish()
        }
        Log.i("ProjectDetailActivity", "id>$projectId")
        val realm = Realm.getDefaultInstance()
        project = realm.where(IndoorProject::class.java).equalTo("id", projectId).findFirst()
        Log.i("ProjectDetailActivity", "name>" + project.getName())
        initUI()
    }

    private fun initUI() {
        pointRV = findViewById(R.id.rv_points)
        btnAddAp = findViewById(R.id.btn_add_ap)
        btnAddAp.setOnClickListener(this)
        btnAddRp = findViewById(R.id.btn_add_rp)
        btnAddRp.setOnClickListener(this)
        btnLocateMe = findViewById(R.id.btn_locate_me)
        btnLocateMe.setOnClickListener(this)
        setCounts()
        val sp: SectionParameters = Builder(R.layout.item_point_details)
            .headerResourceId(R.layout.item_section_details)
            .build()
        apSec = AccessPointSection(sp)
        rpSec = ReferencePointSection(sp)
        apSec.setAccessPoints(project.getAps())
        rpSec.setReferencePoints(project.getRps())
        sectionAdapter.addSection(apSec)
        sectionAdapter.addSection(rpSec)
        layoutManager = LinearLayoutManager(this)
        pointRV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        pointRV.setLayoutManager(layoutManager)
        pointRV.setAdapter(sectionAdapter)
        pointRV.addOnItemTouchListener(RecyclerItemClickListener(this, pointRV, this))
    }

    private fun setCounts() {
        val name: String = project.getName()
        val apCount: Int = project.getAps().size()
        val rpCount: Int = project.getRps().size()
        if (supportActionBar != null) {
            supportActionBar!!.setTitle(name)
        }
        if (apCount > 0) {
            (findViewById(R.id.tv_ap_count) as TextView).text = "Access Points:$apCount"
        }
        if (rpCount > 0) {
            (findViewById(R.id.tv_rp_count) as TextView).text = "Reference Points:$rpCount"
        }
    }

    override fun onResume() {
        super.onResume()
        sectionAdapter.notifyDataSetChanged()
        setCounts()
    }

    override fun onClick(view: View) {
        if (view.id == btnAddAp!!.id) {
            startAddAPActivity("")
        } else if (view.id == btnAddRp!!.id) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERM_REQ_CODE_RP_ACCESS_COARSE_LOCATION
                )
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            } else {
                startAddRPActivity(null)
            }
        } else if (view.id == btnLocateMe!!.id) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERM_REQ_CODE_LM_ACCESS_COARSE_LOCATION
                )
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            } else {
                startAddLocateMeActivity()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERM_REQ_CODE_RP_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startAddRPActivity(null)
        } else if (requestCode == PERM_REQ_CODE_LM_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startAddLocateMeActivity()
        }
    }

    private fun startAddAPActivity(apId: String) {
        val intent = Intent(this, AddOrEditAccessPointActivity::class.java)
        intent.putExtra("projectId", projectId)
        intent.putExtra("apID", apId)
        startActivity(intent)
    }

    private fun startAddRPActivity(rpId: String?) {
        val intent = Intent(this, AddOrEditReferencePointActivity::class.java)
        intent.putExtra("projectId", projectId)
        intent.putExtra("rpId", rpId)
        startActivity(intent)
    }

    private fun startAddLocateMeActivity() {
        val intent = Intent(this, LocateMeActivity::class.java)
        intent.putExtra("projectId", projectId)
        startActivity(intent)
    }

    override fun onItemClick(view: View?, position: Int) {
//        Toast.makeText(this,"onItemClick pos>"+position, Toast.LENGTH_SHORT).show();
        var apsCount = 0
        if (project.getAps() != null) {
            apsCount = project.getAps().size()
        }
        if (position <= apsCount && position != 0) { //AP section event
            val accessPoint: AccessPoint = project.getAps().get(position - 1)
            startAddAPActivity(accessPoint.getId())
        } else if (position > apsCount + 1) { //RP section event
            val referencePoint: ReferencePoint = project.getRps().get(position - apsCount - 1 - 1)
            startAddRPActivity(referencePoint.getId())
        }
    }

    override fun onLongClick(view: View?, position: Int) {
//        Toast.makeText(this,"onLongClick pos>"+position, Toast.LENGTH_SHORT).show();
        var apsCount = 0
        if (project.getAps() != null) {
            apsCount = project.getAps().size()
        }
        if (position <= apsCount && position != 0) { //AP section event
            val accessPoint: AccessPoint = project.getAps().get(position - 1)
            showDeleteDialog(accessPoint, null)
        } else if (position > apsCount + 1) { //RP section event
            val referencePoint: ReferencePoint = project.getRps().get(position - apsCount - 1 - 1)
            showDeleteDialog(null, referencePoint)
        }
    }

    private fun showDeleteDialog(accessPoint: AccessPoint?, referencePoint: ReferencePoint?) {
        val builder: AlertDialog.Builder = Builder(this, R.style.Theme_AppCompat_DayNight_Dialog)
        if (accessPoint != null) {
            builder.setTitle("Delete this Access Point")
            builder.setMessage("Delete " + accessPoint.getSsid())
        } else {
            builder.setTitle("Delete this Reference Point")
            builder.setMessage("Delete " + referencePoint.getName())
        }
        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialogInterface, i ->
                val realm = Realm.getDefaultInstance()
                if (accessPoint != null) {
                    realm.executeTransaction {
                        accessPoint.deleteFromRealm()
                        refreshList()
                    }
                } else {
                    realm.executeTransaction {
                        referencePoint.deleteFromRealm()
                        //                            project.getRps().deleteAllFromRealm();
                        refreshList()
                    }
                }
            })
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun refreshList() {
        runOnUiThread { sectionAdapter.notifyDataSetChanged() }
    }
}