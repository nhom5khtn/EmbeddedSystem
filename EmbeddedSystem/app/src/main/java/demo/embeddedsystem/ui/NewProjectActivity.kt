package demo.embeddedsystem.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import demo.embeddedsystem.R
import demo.embeddedsystem.model.IndoorProject
import io.realm.Realm
import java.util.*

class NewProjectActivity : AppCompatActivity() , View.OnClickListener{
    private var etProjectName: EditText? = null
    private  var etProjectDesc:EditText? = null
    private var btCreate: Button? = null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_project)
        etProjectName = findViewById(R.id.et_project_name)
        etProjectDesc = findViewById(R.id.et_project_desc)
        btCreate = findViewById(R.id.bn_project_create)
        btCreate.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view.id == btCreate!!.id) {
            val text = etProjectName!!.text.toString().trim { it <= ' ' }
            val desc: String = etProjectDesc.getText().toString().trim { it <= ' ' }
            if (text.isEmpty()) {
                Snackbar.make(btCreate!!, "Provide Project Name", Snackbar.LENGTH_LONG).setAction("Action", null).show()
            } else {
                val indoorProject = IndoorProject(Date(), text, desc)
                // Obtain a Realm instance
                val realm = Realm.getDefaultInstance()
                realm.executeTransactionAsync({ bgRealm ->
                    val indoorProject: IndoorProject = bgRealm.createObject(
                        IndoorProject::class.java,
                        UUID.randomUUID().toString()
                    )
                    indoorProject.setName(text)
                    indoorProject.setDesc(desc)
                    indoorProject.setCreatedAt(Date())
                }, { // Transaction was a success.
                    finish()
                }) { error -> // Transaction failed and was automatically canceled.
                    print(error.message)
                }
            }
        }
    }
}