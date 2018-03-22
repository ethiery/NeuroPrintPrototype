package com.simprints.neuroprintprototype.activities.recording

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.hardware.Sensor
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import com.simprints.neuroprintprototype.App
import com.simprints.neuroprintprototype.R
import com.simprints.neuroprintprototype.data.recordings.RecordingRepository
import com.simprints.neuroprintprototype.data.users.User
import com.simprints.neuroprintprototype.data.users.UserRepository
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sensorManager




@SuppressLint("LogNotTimber")
open class RecordingActivity : AppCompatActivity(), RecordingContract.View {

    companion object {
        private const val ADD_USER_ITEM = "Add user"
    }

    private lateinit var presenter: RecordingContract.Presenter

    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter = injectRecordingPresenter()

        startButton.setOnClickListener { start() }
        initSpinner()
    }

    private fun initSpinner() {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (parent.getItemAtPosition(position) == ADD_USER_ITEM) {
                    openUserCreationDialog()
                }
            }
        }
    }

    private fun openUserCreationDialog() {
        val input = EditText(this@RecordingActivity)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        AlertDialog.Builder(this@RecordingActivity)
            .setTitle("New user")
            .setView(input)
            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                val newUser = User(input.text.toString())
                userList.add(newUser)
                updateSpinnerValues()
                spinner.setSelection(userList.indexOf(newUser))
                presenter.addUser(newUser)
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.cancel()
            }
            .show()
    }

    open fun injectRecordingPresenter(): RecordingContract.Presenter {
        val app = application as App
        val firestore = app.firestore
        val sensorRecorder = SensorRecorder(sensorManager, Sensor.TYPE_LINEAR_ACCELERATION)
        val recordingRepository = RecordingRepository(firestore)
        val userRepository = UserRepository(firestore)
        return RecordingPresenter(this, recordingRepository, userRepository, sensorRecorder)
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    private fun start() {
        presenter.startRecording()
    }

    override fun startRecordingUI() {
        startButton.isEnabled = false
        recordingProgressBar.isIndeterminate = true
        recordingProgressBar.visibility = View.VISIBLE
    }

    override fun updateRecordingUI(currentProgress: Int, maxProgress: Int) {
        recordingProgressBar.isIndeterminate = false
        recordingProgressBar.progress = currentProgress
        recordingProgressBar.max = maxProgress
    }

    override fun stopRecordingUI() {
        startButton.isEnabled = true
        recordingProgressBar.visibility = View.INVISIBLE
    }

    override fun updateUserList(users: List<User>) {
        userList.clear()
        userList.addAll(users)
        updateSpinnerValues()
    }

    private fun updateSpinnerValues() {
        val values = userList.map { it.name } + ADD_USER_ITEM
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

}
