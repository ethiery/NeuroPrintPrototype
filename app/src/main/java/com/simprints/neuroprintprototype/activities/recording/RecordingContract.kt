package com.simprints.neuroprintprototype.activities.recording

import com.simprints.neuroprintprototype.data.users.User

interface RecordingContract {

    interface View {

        fun updateUserList(users: List<User>)

        fun startRecordingUI()

        fun updateRecordingUI(currentProgress: Int, maxProgress: Int)

        fun stopRecordingUI()

    }

    interface Presenter {

        fun onStart()

        fun addUser(user: User)

        fun startRecording()

        fun onStop()

    }

}
