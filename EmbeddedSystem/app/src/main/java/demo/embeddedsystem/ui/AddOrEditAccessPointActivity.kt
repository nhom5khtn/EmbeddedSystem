package demo.embeddedsystem.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import demo.embeddedsystem.R
import io.realm.Realm
import java.util.*


import demo.embeddedsystem.model.AccessPoint;
import demo.embeddedsystem.model.IndoorProject;

class AddOrEditAccessPointActivity : AppCompatActivity(), View.OnClickListener {
    private var addAp: Button? = null
    private var btnScanAP: Button? = null
    private var etName: EditText? = null
    private var etDesc: EditText? = null
    private var etX: EditText? = null
    private var etY: EditText? = null
    private var etMAC: EditText? = null
    private var projectId: String? = null
    private var apID: String? = null
    private var isEdit = false
    private var apToBeEdited: AccessPoint? = null
    private val PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 199
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_access_point)
        projectId = intent.getStringExtra("projectId")
        if (projectId == null) {
            Toast.makeText(this, "Access point not found", Toast.LENGTH_LONG).show()
            finish()
        }
        apID = intent.getStringExtra("apID")
        initUI()
        if (apID == "") {
            isEdit = false
        } else {
            isEdit = true
            addAp!!.text = "Save"
        }
        if (isEdit) setUpEditMode()
    }

    private fun setUpEditMode() {
        val realm = Realm.getDefaultInstance()
        apToBeEdited = realm.where(AccessPoint::class.java).equalTo("id", apID).findFirst()
        setValuesToFields(apToBeEdited)
    }

    private fun setValuesToFields(accessPoint: AccessPoint?) {
        etName.setText(accessPoint.getSsid())
        etDesc.setText(accessPoint.getDescription())
        etX.setText(java.lang.String.valueOf(accessPoint.getX()))
        etY.setText(java.lang.String.valueOf(accessPoint.getY()))
        etMAC.setText(accessPoint.getMac_address())
    }

    private fun initUI() {
        etName = findViewById(R.id.et_ap_name)
        etDesc = findViewById(R.id.et_ap_desc)
        etX = findViewById(R.id.et_ap_x)
        etY = findViewById(R.id.et_ap_y)
        etMAC = findViewById(R.id.et_ap_mac)
        addAp = findViewById(R.id.bn_ap_create)
        addAp.setOnClickListener(this)
        btnScanAP = findViewById(R.id.bn_ap_scan)
        btnScanAP.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view.id == addAp!!.id) {
            val text = etName!!.text.toString().trim { it <= ' ' }
            val desc = etDesc!!.text.toString().trim { it <= ' ' }
            val x = etX!!.text.toString().trim { it <= ' ' }
            val y = etY!!.text.toString().trim { it <= ' ' }
            val mac = etMAC!!.text.toString().trim { it <= ' ' }
            val isEditMode = isEdit
            if (text.isEmpty()) {
                Snackbar.make(addAp!!, "Provide Access Point Name", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            } else {
                // Obtain a Realm instance
                val realm = Realm.getDefaultInstance()
                realm.beginTransaction()
                val project: IndoorProject? =
                    realm.where(IndoorProject::class.java).equalTo("id", projectId).findFirst()
                if (isEditMode) {
                    apToBeEdited.setSsid(text)
                    apToBeEdited.setDescription(desc)
                    apToBeEdited.setX(java.lang.Double.valueOf(x))
                    apToBeEdited.setY(java.lang.Double.valueOf(y))
                    apToBeEdited.setMac_address(mac)
                } else {
                    val accessPoint: AccessPoint =
                        realm.createObject(AccessPoint::class.java, UUID.randomUUID().toString())
                    accessPoint.setBssid(mac)
                    accessPoint.setDescription(desc)
                    accessPoint.setCreatedAt(Date())
                    accessPoint.setX(java.lang.Double.valueOf(x))
                    accessPoint.setY(java.lang.Double.valueOf(y))
                    accessPoint.setSsid(text)
                    accessPoint.setMac_address(mac)
                    project.getAps().add(accessPoint)
                }
                realm.commitTransaction()
                finish()
            }
        } else if (view.id == btnScanAP!!.id) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
            )
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
    }

    private fun startSearchWifiActivity() {
        val intent = Intent(this, SearchWifiAccessPointActivity::class.java)
        startActivityForResult(intent, REQ_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSearchWifiActivity()
        }
    }

    protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            val accessPoint: AccessPoint? =
                data.getParcelableExtra<Parcelable>("accessPoint") as AccessPoint?
            setValuesToFields(accessPoint)
        }
    }

    companion object {
        private const val REQ_CODE = 1212 //this is always positive
    }
}
