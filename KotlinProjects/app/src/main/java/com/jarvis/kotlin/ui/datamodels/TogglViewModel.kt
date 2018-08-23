package com.jarvis.kotlin.ui.datamodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import com.jarvis.kotlin.TogglApp
import com.jarvis.kotlin.constants.BulkOperationResult
import com.jarvis.kotlin.constants.BulkOperationType
import com.jarvis.kotlin.domain.toggl.*
import com.jarvis.kotlin.network.ApiClient
import com.jarvis.kotlin.network.toggl.ApiCallDelegate
import com.jarvis.kotlin.network.toggl.request.CreateTimeEntryRequest
import com.jarvis.kotlin.network.toggl.response.*
import com.jarvis.kotlin.utils.DateUtils
import com.jarvis.kotlin.utils.PrefsHelper
import com.jarvis.kotlin.utils.TimeEntryUtils
import java.util.concurrent.atomic.AtomicInteger

class TogglViewModel : ViewModel() {

    var userData: MutableLiveData<User> = MutableLiveData()

    var errorData: MutableLiveData<String> = MutableLiveData()

    var changedTimeEntry: MutableLiveData<TimeEntry> = MutableLiveData()

    var bulkOperationResponse: MutableLiveData<BulkOperation> = MutableLiveData()

    private var mBatchTimeEntriesResponses = mutableListOf<TogglHttpResponse>()

    private var mPendingActions = AtomicInteger(0)

    private lateinit var mUser: User

    private lateinit var mBatchTimeEntries: MutableList<TimeEntry>


    fun createAndReplicateTimeEntry(timeEntry: TimeEntry) {
        val timeEntries = mutableListOf<TimeEntry>()
        timeEntries.add(timeEntry)
        timeEntries.addAll(TimeEntryUtils.getTimeEntryDuplicatesForWeek(timeEntry))
        mBatchTimeEntries = timeEntries

        if (timeEntries.size == 0) {
            changedTimeEntry.value = null
            changedTimeEntry = MutableLiveData()
        } else {
            mPendingActions.set(timeEntries.size)
            mBatchTimeEntries = timeEntries
            createTimeEntry(mBatchTimeEntries[timeEntries.size - mPendingActions.getAndDecrement()],
                    getBatchReplicateTimeEntriesDelegate())
        }
    }

    private fun createTimeEntry(timeEntry: TimeEntry, delegate: ApiCallDelegate) {
        val createTimeEntryRequest = CreateTimeEntryRequest(timeEntry)
        ApiClient.TogglClient.createTimeEntry(createTimeEntryRequest, delegate)
    }

    fun createMultipleTimeEntries(timeEntries: MutableList<TimeEntry>) {
        if (timeEntries.size == 0) {
            bulkOperationResponse.value = BulkOperation(BulkOperationType.SAVE, BulkOperationResult.SUCCESS)
            bulkOperationResponse = MutableLiveData()
        } else {
            mPendingActions.set(timeEntries.size)
            mBatchTimeEntries = timeEntries
            createTimeEntry(mBatchTimeEntries[timeEntries.size - mPendingActions.getAndDecrement()],
                    getBatchCreateTimeEntriesDelegate())
        }
    }

    private fun getBatchCreateTimeEntriesDelegate(): ApiCallDelegate {
        return object : ApiCallDelegate {
            override fun onResponse(togglResponse: TogglHttpResponse) {
                if (TextUtils.isEmpty(togglResponse.errorMessage)) {
                    // Ignore. Process until all of the pending time entries
                    // have been added.
                }

                if (mPendingActions.get() == 0) {
                    bulkOperationResponse.value = BulkOperation(BulkOperationType.SAVE, BulkOperationResult.SUCCESS)
                    bulkOperationResponse = MutableLiveData()
                } else {
                    createTimeEntry(mBatchTimeEntries[mBatchTimeEntries.size - mPendingActions.getAndDecrement()],
                            getBatchCreateTimeEntriesDelegate())
                }
            }
        }
    }

    private fun getBatchReplicateTimeEntriesDelegate(): ApiCallDelegate {
        return object : ApiCallDelegate {
            override fun onResponse(togglResponse: TogglHttpResponse) {
                val processingFirstItem = mPendingActions.get() + 1 == mBatchTimeEntries.size
                mBatchTimeEntriesResponses.add(togglResponse)
                if (!TextUtils.isEmpty(togglResponse.errorMessage)) {
                    if (processingFirstItem) {
                        changedTimeEntry.value = null
                        changedTimeEntry = MutableLiveData()
                        mBatchTimeEntries.clear()
                        return
                    }
                } else if (mPendingActions.get() == 0) {
                    changedTimeEntry.value = (mBatchTimeEntriesResponses[0].apiResponse as TimeEntryResponse).timeEntry
                    changedTimeEntry = MutableLiveData()
                    mBatchTimeEntries.clear()
                } else {
                    createTimeEntry(mBatchTimeEntries[mBatchTimeEntries.size - mPendingActions.getAndDecrement()],
                            getBatchReplicateTimeEntriesDelegate())
                }
            }
        }
    }


    private fun getTimeEntryChangeDelegate(): ApiCallDelegate {
        return object : ApiCallDelegate {
            override fun onResponse(togglResponse: TogglHttpResponse) {
                if (TextUtils.isEmpty(togglResponse.errorMessage)) {
                    val timeEntryResponse = togglResponse.apiResponse as TimeEntryResponse
                    changedTimeEntry.value = timeEntryResponse.timeEntry
                    Log.d(TAG, "Posted time entry: " + timeEntryResponse.timeEntry)
                } else {
                    changedTimeEntry.value = null
                }
                changedTimeEntry = MutableLiveData()
            }
        }
    }

    fun loadUser(credentials: Credentials?) {
        ApiClient.TogglClient.getUserData(credentials, object : ApiCallDelegate {
            override fun onResponse(togglResponse: TogglHttpResponse) {
                if (TextUtils.isEmpty(togglResponse.errorMessage) && togglResponse.apiResponse != null) {
                    var userDataResponse = togglResponse.apiResponse as GetUserResponse
                    mUser = userDataResponse.userData
                    if (!TextUtils.isEmpty(mUser.apiToken)) {
                        PrefsHelper(TogglApp.instance).saveApiToken(mUser.apiToken)
                    }
                    loadTimeEntries()
                } else {
                    userData = MutableLiveData()
                    errorData.value = "Invalid username or password"
                    errorData = MutableLiveData()
                }
            }
        })
    }

    fun createTimeEntry(timeEntry: TimeEntry) {
        val createTimeEntryRequest = CreateTimeEntryRequest(timeEntry)
        ApiClient.TogglClient.createTimeEntry(createTimeEntryRequest, getTimeEntryChangeDelegate())
    }

    fun updateTimeEntry(timeEntry: TimeEntry) {
        val updateTimeEntryRequest = CreateTimeEntryRequest(timeEntry)
        ApiClient.TogglClient.updateTimeEntry(updateTimeEntryRequest, getTimeEntryChangeDelegate())
    }

    fun deleteTimeEntry(timeEntry: TimeEntry) {
        ApiClient.TogglClient.deleteTimeEntry(timeEntry.id,
                object : ApiCallDelegate {
                    override fun onResponse(togglResponse: TogglHttpResponse) {
                        if (TextUtils.isEmpty(togglResponse.errorMessage)) {
                            errorData.value = null
                        } else errorData.value = togglResponse.errorMessage
                        errorData = MutableLiveData()
                    }
                })
    }

    private fun loadTimeEntries() {
        Log.d(TAG, "loadTimeEntries()")
        val searchRange = DateUtils.getPayPeriod()
        ApiClient.TogglClient.getTimeEntries(searchRange.first!!, searchRange.second!!,
                object : ApiCallDelegate {
                    override fun onResponse(togglResponse: TogglHttpResponse) {
                        if (TextUtils.isEmpty(togglResponse.errorMessage)) {
                            var timeEntriesResponse = togglResponse.apiResponse as GetTimeEntriesResponse
                            mUser.timeEntries = timeEntriesResponse.timeEntries
                            mPendingActions.set(mUser.projects.size)
                            loadProjectTasks(mUser.projects.get(mUser.projects.size - mPendingActions.getAndDecrement()))
                        }
                    }
                })
    }

    private fun loadProjectTasks(project: Project) {
        ApiClient.TogglClient.getProjectTasks(project.id,
                object : ApiCallDelegate {
                    override fun onResponse(togglResponse: TogglHttpResponse) {
                        if (TextUtils.isEmpty(togglResponse.errorMessage)) {
                            val projectTasksResponse = togglResponse.apiResponse as GetProjectTasksResponse
                            if (projectTasksResponse.tasks != null) {
                                project.tasks = projectTasksResponse.tasks
                            }
                        }
                        if (mPendingActions.get() == 0) {
                            userData.value = mUser
                        } else {
                            loadProjectTasks(mUser.projects.get(mUser.projects.size -
                                    mPendingActions.getAndDecrement()))
                        }
                    }
                })
    }

    companion object {
        private val TAG = TogglViewModel::class.java.name
    }


}
