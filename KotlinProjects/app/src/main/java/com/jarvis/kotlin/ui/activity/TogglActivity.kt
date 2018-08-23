package com.jarvis.kotlin.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.CompoundButtonCompat
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.jarvis.kotlin.R
import com.jarvis.kotlin.domain.toggl.*
import com.jarvis.kotlin.ui.TimeEntryDisplayModel
import com.jarvis.kotlin.ui.datamodels.TogglViewModel
import com.jarvis.kotlin.ui.fragment.EditTimeEntryFragment
import com.jarvis.kotlin.ui.fragment.LoginFragment
import com.jarvis.kotlin.ui.fragment.TimeEntriesFragment
import com.jarvis.kotlin.ui.widget.TimeEntryActionsView
import com.jarvis.kotlin.utils.DialogUtils
import com.jarvis.kotlin.utils.NetworkUtils
import com.jarvis.kotlin.utils.PrefsHelper
import kotlinx.android.synthetic.main.activity_toggl.*

class TogglActivity : BaseActivity(), TimeEntriesFragment.OnTimeEntryApiActionListener,
        EditTimeEntryFragment.TimeEntryListener,
        TimeEntryActionsView.OnTimeEntryOptionListener,
        LoginFragment.OnLoginListener {

    private var mUser: User? = null

    private var mAlertDialog: AlertDialog? = null

    private var mLoginShowing = false

    private var mMenuViewProfile: View? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toggl)
        setSupportActionBar(toggl_toolbar)
        toggl_toolbar.title = null
        title = null
        showLoginScreen()
        val apiToken = PrefsHelper(this).getApiToken()
        if (!TextUtils.isEmpty(apiToken) && NetworkUtils.isNetworkConnected(this)) {
            loadUser(Credentials(apiToken, "api_token"))
        }
        prepareFab()
    }

    private fun prepareFab() {
        val statesChecked = intArrayOf(android.R.attr.state_pressed)
        val statesNormal = intArrayOf()
        val states = arrayOf<IntArray>(statesChecked, statesNormal)
        val colors = intArrayOf(ContextCompat.getColor(this, R.color.dark_red),
                ContextCompat.getColor(this, R.color.red))
        fab.backgroundTintList = ColorStateList(states, colors)
        fab.setOnClickListener { view ->
            getTimeEntriesFragment()?.createNewEntry()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toggl, menu)
        val profileIconView = menu?.findItem(R.id.item_user_profile)?.getActionView();
        mMenuViewProfile = profileIconView?.findViewById(R.id.icon_profile)
        mMenuViewProfile?.setOnClickListener {
            showUserProfilePopup()
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (mLoginShowing) {
            menu?.findItem(R.id.item_user_profile)?.setVisible(false)
        } else {
            menu?.findItem(R.id.item_user_profile)?.setVisible(true)
        }
        return true
    }

    private fun showUserProfilePopup() {
        val coords = intArrayOf(0, 0)
        mMenuViewProfile!!.getLocationInWindow(coords)
        if (mMenuViewProfile != null) {
            DialogUtils.showUserDetailsPopup(this, mUser!!, mMenuViewProfile!!)
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else if (supportFragmentManager.backStackEntryCount > 2) {
            fab.visibility = View.VISIBLE
            return super.onBackPressed()
        }
    }


    private fun loadUser(credentials: Credentials?) {
        val viewModel = ViewModelProviders.of(this).get(TogglViewModel::class.java)
        viewModel.userData.observe(this, Observer {
            mUser = it
            hideProgress()
            showTimeEntriesScreen()
        })
        viewModel.errorData.observe(this, Observer {
            hideProgress()
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
        showProgress("Loading user data...")
        viewModel.loadUser(credentials)
    }


    private fun showProgress(message: String) {
        Log.d(TAG, "showProgress()")
        // Update to display an in-layout progress dialog
        if (mAlertDialog != null && mAlertDialog!!.isShowing) {
            mAlertDialog!!.dismiss()
        }

        val builder = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.common_progress_dialog, null)
        val messageText: TextView = dialogView.findViewById(R.id.label_progress_msg)
        messageText.text = message
        builder.setView(dialogView)
        builder.setCancelable(false)
        mAlertDialog = builder.create()
        mAlertDialog!!.show()
    }

    private fun hideProgress() {
        if (mAlertDialog != null && mAlertDialog!!.isShowing) {
            mAlertDialog!!.dismiss()
        }
    }

    private fun showLoginScreen() {
        toggl_toolbar.visibility = View.GONE
        fab.visibility = View.GONE
        invalidateOptionsMenu()
        mLoginShowing = true
        var loginFragment = supportFragmentManager.findFragmentByTag(LoginFragment.TAG) as LoginFragment?

        if (loginFragment != null) {
            supportFragmentManager.popBackStack(LoginFragment.TAG, 0)
        } else supportFragmentManager.beginTransaction().replace(R.id.content, LoginFragment.newInstance(),
                LoginFragment.TAG).addToBackStack(LoginFragment.TAG).commit()
    }

    private fun showTimeEntriesScreen() {
        toggl_toolbar.visibility = View.VISIBLE
        fab.visibility = View.VISIBLE
        invalidateOptionsMenu()
        mLoginShowing = false
        var timeEntriesFragment = supportFragmentManager.findFragmentByTag(TimeEntriesFragment.TAG) as
                TimeEntriesFragment?

        if (timeEntriesFragment != null) {
            Log.d(TAG, "showTimeEntriesScreen() : Time entries fragment exists!")
            supportFragmentManager.popBackStack(TimeEntriesFragment.TAG, 0)
        } else {
            Log.d(TAG, "showTimeEntriesScreen() :Time entries fragment does not exist!")
            supportFragmentManager.beginTransaction().replace(R.id.content, TimeEntriesFragment.newInstance(),
                    TimeEntriesFragment.TAG).addToBackStack(
                    TimeEntriesFragment.TAG
            ).commit()
        }
    }

    private fun showTimeEntryEditScreen(timeEntry: TimeEntry?, adapterPosition: Int, user: User?) {
        presentTimeEntryEditCreateScreen(EditTimeEntryFragment.newInstance(timeEntry, adapterPosition, user, this))
    }

    private fun showTimeEntryCreateScreen(user: User?) {
        presentTimeEntryEditCreateScreen(EditTimeEntryFragment.newInstance(user, this))
    }

    private fun presentTimeEntryEditCreateScreen(form: EditTimeEntryFragment) {
        invalidateOptionsMenu()
        fab.visibility = View.GONE
        mLoginShowing = false
        supportFragmentManager.beginTransaction().replace(R.id.content,
                form).addToBackStack(EditTimeEntryFragment.TAG).commit()
    }

    private fun getTimeEntriesFragment(): TimeEntriesFragment? {
        return supportFragmentManager.findFragmentByTag(TimeEntriesFragment.TAG) as TimeEntriesFragment?
    }

    override fun onLogin(username: String, password: String) {
        val credentials = Credentials(username, password)
        loadUser(credentials)
        val viewModel = ViewModelProviders.of(this).get(TogglViewModel::class.java)
        viewModel.userData.observe(this, Observer {
            mUser = it
            if (it != null) {
                if (!TextUtils.isEmpty(it.apiToken)) {
                    PrefsHelper(this).saveApiToken(it.apiToken)
                }
                setupUserInteractionMonitor()
                PrefsHelper(this).saveUsername(username)
            } else {
                Toast.makeText(this, "An error occurred while attempting to login. Please try again.",
                        Toast.LENGTH_LONG).show()
            }
        })
    }


    override fun onCreateNew(user: User?) {
        showTimeEntryCreateScreen(user)
    }

    override fun onSaveAll(timeEntries: MutableList<TimeEntry>?) {
        Log.d(TAG, "onSaveAll()")
        showProgress("Saving time entry batch...")
        val viewModel = ViewModelProviders.of(this).get(TogglViewModel::class.java)
        val observer: Observer<BulkOperation> = Observer {
            hideProgress()
            viewModel.bulkOperationResponse.removeObservers(this)
            if (it != null) {
                Toast.makeText(this, "Batch time entry creation complete!", Toast.LENGTH_SHORT).show()
                getTimeEntriesFragment()?.clearTimeEntries()
                showTimeEntriesScreen()
                loadUser(null)
            } else {
                Toast.makeText(this, "An error occurred while performing batch operation!", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.bulkOperationResponse.observe(this, observer)
        viewModel.createMultipleTimeEntries(timeEntries!!)
    }

    override fun onDelete(timeEntry: TimeEntry, adapterPosition: Int) {
        showProgress("Removing Time Entry...")
        val viewModel = ViewModelProviders.of(this).get(TogglViewModel::class.java)
        val observer: Observer<String> = Observer {
            hideProgress()
            viewModel.errorData.removeObservers(this)
            if (it == null) {
                Toast.makeText(this, "Time Entry Removed", Toast.LENGTH_SHORT).show()
                showTimeEntriesScreen()
                getTimeEntriesFragment()!!.addChange(adapterPosition, TimeEntryChangeResult(timeEntry, TimeEntryChangeResult.ChangeType.DELETE))
            } else {
                Toast.makeText(this, "Error creating time entry.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorData.observe(this, observer)
        viewModel.deleteTimeEntry(timeEntry)
    }

    override fun onSave(timeEntry: TimeEntry, adapterPosition: Int) {
        onRequestCreateTimeEntry(timeEntry, adapterPosition, false)
    }

    override fun onEdit(timeEntry: TimeEntry, adapterPosition: Int, user: User?) {
        showTimeEntryEditScreen(timeEntry, adapterPosition, user)
    }


    override fun onSaveEntry(timeEntry: TimeEntry, adapterPosition: Int) {
        getTimeEntriesFragment()?.saveTimeEntry(timeEntry, adapterPosition)
    }

    override fun onEditEntry(timeEntry: TimeEntry, adapterPosition: Int, user: User?) {
        showTimeEntryEditScreen(timeEntry, adapterPosition, user)
    }

    override fun onDeleteEntry(timeEntryDisplayModel: TimeEntryDisplayModel, adapterPosition: Int) {
        getTimeEntriesFragment()?.deleteTimeEntry(timeEntryDisplayModel, adapterPosition)
    }

    override fun onCancel() {
        fab.visibility = View.VISIBLE
        supportFragmentManager.popBackStack()
    }

    override fun onRequestCreateMultiple(timeEntries: MutableList<TimeEntry>?) {
        onSaveAll(timeEntries)
    }

    override fun onRequestCreateTimeEntry(timeEntry: TimeEntry, adapterPosition: Int?, replicate: Boolean?) {
        Log.d(TAG, "onRequestCreateTimeEntry() :: replicate->".plus(replicate!!))
        showProgress("Creating Time Entry...")
        val viewModel = ViewModelProviders.of(this).get(TogglViewModel::class.java)
        val observer = getTimeEntryChangeObserver(adapterPosition, "Time Entry Added!",
                "Error creating time entry.")
        viewModel.changedTimeEntry.observe(this, observer)

        if (replicate) {
            viewModel.createAndReplicateTimeEntry(timeEntry)
        } else {
            viewModel.createTimeEntry(timeEntry)
        }
    }


    override fun onRequestUpdateTimeEntry(timeEntry: TimeEntry?, adapterPosition: Int, replicate: Boolean?) {
        Log.d(TAG, "onRequestUpdateTimeEntry() :: ".plus(timeEntry))
        showProgress("Updating Time Entry...")
        val viewModel = ViewModelProviders.of(this).get(TogglViewModel::class.java)
        val observer = getTimeEntryChangeObserver(adapterPosition, "Time Entry Updated!",
                "Error updating time entry.")
        viewModel.changedTimeEntry.observe(this, observer)
        viewModel.updateTimeEntry(timeEntry!!)
    }

    private fun getTimeEntryChangeObserver(adapterPosition: Int?, successText: String, errorText: String): Observer<TimeEntry> {
        val viewModel = ViewModelProviders.of(this).get(TogglViewModel::class.java)
        val observer: Observer<TimeEntry> = Observer {
            hideProgress()
            viewModel.changedTimeEntry.removeObservers(this)
            if (it != null) {
                Log.d(TAG, "getTimeEntryChangeObserver() :: changedTimeEntry observed")
                Toast.makeText(this, successText, Toast.LENGTH_SHORT).show()
                showTimeEntriesScreen()
                getTimeEntriesFragment()!!.addChange(adapterPosition, TimeEntryChangeResult(it!!,
                        TimeEntryChangeResult.ChangeType.CREATE))
            } else {
                Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show()
            }
        }
        return observer
    }

    override fun onShowTimeEntryOptions(timeEntryModel: TimeEntryDisplayModel, adapterPosition: Int, user: User?,
                                        anchorView: View) {
        DialogUtils.showTimeEntryActionsPopup(this, timeEntryModel, adapterPosition,
                user, anchorView)
    }


    companion object {
        private val TAG = TogglActivity::class.java.name
    }

}
