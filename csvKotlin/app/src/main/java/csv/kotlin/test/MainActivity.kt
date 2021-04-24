package csv.kotlin.test

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import csv.kotlin.test.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: StudentAdapter
    val studentList = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        csvReader().open(resources.openRawResource(R.raw.student)) {
            readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->
                studentList.add(Student(
                        row.get("StudentID").toString(),
                        row.get("FirstName").toString(),
                        row.get("LastName").toString(),
                        row.get("Score").toString()
                ))
                Log.e("read csv", studentList.toString())
            }
        }
        adapter = StudentAdapter()
        binding.rcList.adapter = adapter
        adapter.listener = object : StudentAdapter.StudentAdapterListener{
            override fun onClickItem(student: Student) {
                Log.e("score", student.score)
            }
        }

        adapter.submitList(studentList)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.listener = null
    }
}