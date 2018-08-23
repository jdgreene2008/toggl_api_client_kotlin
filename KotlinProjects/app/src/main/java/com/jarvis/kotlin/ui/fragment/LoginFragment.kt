package com.jarvis.kotlin.ui.fragment


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jarvis.kotlin.BuildConfig

import com.jarvis.kotlin.R
import com.jarvis.kotlin.utils.NetworkUtils
import com.jarvis.kotlin.utils.PrefsHelper
import kotlinx.android.synthetic.main.fragment_login.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class LoginFragment : Fragment() {

    private var listener: OnLoginListener? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onViewCreated()")
        fld_username.setText(PrefsHelper(context!!).getUsername())
        fld_password.setText(null)
        btn_login.setOnClickListener {
            if (NetworkUtils.isNetworkConnected(context!!)) {
                if (validateInput()) {
                    listener?.onLogin(fld_username.text.toString(), fld_password.text.toString())
                }
            } else {
                Toast.makeText(context!!, "Please enable network connectivity to proceed with this action.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLoginListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnLoginListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    private fun validateInput(): Boolean {
        return !(TextUtils.isEmpty(fld_password.text.toString()) ||
                TextUtils.isEmpty(fld_username.text.toString()))
    }


    companion object {
        val TAG = LoginFragment::class.java.name
        @JvmStatic
        fun newInstance() = LoginFragment()
    }

    interface OnLoginListener {
        fun onLogin(username: String, password: String)
    }
}
