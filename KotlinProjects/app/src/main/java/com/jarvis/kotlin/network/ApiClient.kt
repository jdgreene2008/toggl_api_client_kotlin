package com.jarvis.kotlin.network

import android.net.Uri
import android.os.Build
import android.os.PatternMatcher
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jarvis.kotlin.BuildConfig
import com.jarvis.kotlin.TogglApp
import com.jarvis.kotlin.domain.toggl.Credentials
import com.jarvis.kotlin.network.toggl.ApiCallDelegate
import com.jarvis.kotlin.network.toggl.ApiCallTask
import com.jarvis.kotlin.network.toggl.Endpoint
import com.jarvis.kotlin.network.toggl.request.BaseRequest
import com.jarvis.kotlin.network.toggl.request.CreateTimeEntryRequest
import com.jarvis.kotlin.network.toggl.response.BaseResponse
import com.jarvis.kotlin.network.toggl.response.TogglHttpResponse
import com.jarvis.kotlin.utils.PrefsHelper
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

class ApiClient {
    private var mError: String? = null


    fun call(credentials: Credentials?, endpoint: Endpoint, urlFieldSubstitutions: List<String>?): TogglHttpResponse {
        return call(null, credentials, endpoint, null, null, urlFieldSubstitutions)
    }

    fun call(endpoint: Endpoint, additionalPaths: List<String>?, urlFieldSubstitutions: List<String>?):
            TogglHttpResponse {
        return call(null, null, endpoint, additionalPaths, null, urlFieldSubstitutions)
    }

    fun call(endpoint: Endpoint, paramMap: Array<out Pair<String, String>>?, urlFieldSubstitutions: List<String>?):
            TogglHttpResponse {
        return call(null, null, endpoint, null, paramMap, urlFieldSubstitutions)
    }


    fun <T : BaseRequest> call(request: T?, endpoint: Endpoint, additionalPaths: List<String>?, urlFieldSubstitutions:
    List<String>?): TogglHttpResponse {
        return call(request, null, endpoint, additionalPaths, null, urlFieldSubstitutions)
    }

    fun <T : BaseRequest> call(request: T?, endpoint: Endpoint, paramMap: Array<out Pair<String, String>>?,
                               urlFieldSubstitutions: List<String>?): TogglHttpResponse {
        return call(request, null, endpoint, null, paramMap, urlFieldSubstitutions)
    }


    fun call(credentials: Credentials?, endpoint: Endpoint): TogglHttpResponse {
        return call(null, credentials, endpoint, null, null, null)
    }

    fun <T : BaseRequest> call(request: T?, endpoint: Endpoint): TogglHttpResponse {
        return call(request, null, endpoint, null, null, null)
    }


    fun call(endpoint: Endpoint, paramMap: Array<out Pair<String, String>>?): TogglHttpResponse {
        return call(null, null, endpoint, null, paramMap, null)
    }


    fun <T : BaseRequest> call(request: T?, endpoint: Endpoint, paramMap: Array<out Pair<String, String>>?): TogglHttpResponse {
        return call(request, null, endpoint, null, paramMap, null)
    }

    fun <T : BaseRequest> call(request: T?, credentials: Credentials?, endpoint: Endpoint, additionalPaths: List<String>?,
                               paramMap: Array<out Pair<String, String>>?, urlFieldSubstitutions: List<String>?): TogglHttpResponse {
        val gson: Gson = GsonBuilder().create()
        var apiResponse: BaseResponse
        var togglResponse: TogglHttpResponse
        var response: String?

        if (endpoint.httpMethod.equals(HttpMethod.Get)) {
            response = performRequest(BASE_ENDPOINT.plus(endpoint.path), credentials, endpoint.httpMethod,
                    additionalPaths, paramMap, urlFieldSubstitutions, null)
        } else {
            val json: String = GsonBuilder().create().toJson(request)
            response = performRequest(BASE_ENDPOINT.plus(endpoint.path), credentials, endpoint.httpMethod, additionalPaths,
                    paramMap, urlFieldSubstitutions, json)
        }

        if (response == null) {
            Log.d(TAG, "Network Error: ".plus(mError))
            togglResponse = TogglHttpResponse(null, mError)
        } else {

            // Format to make the time entries query response compatible with the other api responses
            // which are subclasses of BaseResponse. Wrap the data inside a json object with tag "data"
            if (!(response.trim().startsWith("{") && response.trim().endsWith("}"))) {
                response = "{\"data\":".plus(response).plus("}")
            }
            Log.d(TAG, "API Response: ".plus(response))
            if (endpoint.responseClass == null) {
                togglResponse = TogglHttpResponse(null, null)
            } else {
                apiResponse = gson.fromJson(response, endpoint.responseClass)
                togglResponse = TogglHttpResponse(apiResponse, null)
            }
        }

        return togglResponse
    }


    private fun performRequest(url: String?, credentials: Credentials?,
                               httpMethod: HttpMethod,
                               additionalPaths: List<String>?,
                               paramMap: Array<out Pair<String, String>>?, urlFieldSubstitutions: List<String>?,
                               body: String?): String? {
        var requestUrl: String = url!!

        // Substitute url fields, replacing {field} occurrances with the substitutions.
        if (urlFieldSubstitutions != null) {
            for (substitution in urlFieldSubstitutions) {
                requestUrl = requestUrl.replaceFirst("{field}", substitution)
            }
        }

        var uri = Uri.parse(requestUrl)
        val builder = uri.buildUpon()

        // Add query params
        if (paramMap != null) {
            for ((param, value) in paramMap) {
                builder.appendQueryParameter(param, value)
            }
        }
        if (additionalPaths != null) {
            for (path in additionalPaths) {
                builder.appendPath(path)
            }
        }
        uri = builder.build()


        val connection = URL(uri.toString()).openConnection() as HttpsURLConnection
        connection.connectTimeout = CONNECTION_TIMEOUT
        connection.doOutput = (httpMethod == HttpMethod.Put || httpMethod == HttpMethod.Post)
        connection.addRequestProperty(RequestProperty.AUTHORIZATION, "Basic ".plus(getEncodedCredentials(credentials)))
        connection.requestMethod = httpMethod.value
        connection.addRequestProperty(RequestProperty.CONTENT_TYPE, "application/json")
        connection.addRequestProperty(RequestProperty.ACCEPT, "application/json")
        if (!TextUtils.isEmpty(body)) {
            Log.d(TAG, "Body: " + body)
            connection.setFixedLengthStreamingMode(body!!.length)
            writeRequest(connection, body)
        }

        Log.d(TAG, "URL: " + uri.toString())
        Log.d(TAG, "HTTP Status Code:".plus(connection.responseCode))
        Log.d(TAG, "HTTP Status Message:".plus(connection.responseMessage))

        if (connection.responseCode == RESPONSE_OK) {
            return readResponseStream(connection.inputStream)
        } else {
            mError = readResponseStream(connection.errorStream)
            return null
        }
    }

    private fun writeRequest(connection: HttpURLConnection, content: String?) {
        val outputStream: OutputStream = BufferedOutputStream(connection.outputStream)
        val writer = BufferedWriter(OutputStreamWriter(outputStream))
        writer.write(content)
        writer.close()
        outputStream.close()
    }

    private fun readResponseStream(stream: InputStream): String {
        val inputStreamReader = InputStreamReader(stream)
        val reader = BufferedReader(inputStreamReader)
        val stringBuilder = StringBuilder()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for (curLine in reader.lines()) {
                stringBuilder.append(curLine)
            }
        } else {
            var line: String?
            var done = false
            do {
                line = reader.readLine()
                if (line != null) {
                    stringBuilder.append(line)
                } else {
                    done = true
                }
            } while (!done)
        }

        return stringBuilder.toString()
    }

    private fun getEncodedCredentials(credentials: Credentials?): String {
        var stringToEncode: String
        if (credentials == null) {
            stringToEncode = PrefsHelper(TogglApp.instance).getApiToken().plus(":api_token")
        } else {
            stringToEncode = credentials!!.username.plus(":").plus(credentials!!.password)
        }

        return Base64.encodeToString(stringToEncode.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
    }


    companion object {
        private val TAG = ApiClient::class.java.name
        private val CONNECTION_TIMEOUT = 20000
        private val RESPONSE_OK = 200
        private val BASE_ENDPOINT = "https://www.toggl.com/api/v8"
    }

    private object RequestProperty {
        val CONTENT_TYPE = "Content-Type"
        val ACCEPT = "Accept"
        val AUTHORIZATION = "Authorization"
    }


    object TogglClient {
        private fun getClient(): ApiClient {
            return ApiClient()
        }

        fun getUserData(credentials: Credentials?, delegate: ApiCallDelegate) {
            object : ApiCallTask(delegate) {
                override fun doInBackground(vararg params: Void?): TogglHttpResponse? {
                    return getClient().call(credentials, Endpoint.GetUserData)
                }
            }.execute()
        }

        fun getTimeEntries(startDate: String, endDate: String, delegate: ApiCallDelegate) {
            val paramArray: Array<out Pair<String, String>> =
                    arrayOf(Pair("start_date", startDate), Pair("end_date", endDate))
            object : ApiCallTask(delegate) {
                override fun doInBackground(vararg params: Void?): TogglHttpResponse? {
                    return getClient().call(Endpoint.GetTimeEntries, paramArray)
                }
            }.execute()
        }

        fun createTimeEntry(request: CreateTimeEntryRequest, delegate: ApiCallDelegate) {
            object : ApiCallTask(delegate) {
                override fun doInBackground(vararg params: Void?): TogglHttpResponse? {
                    return getClient().call(request, Endpoint.CreateTimeEntry)
                }
            }.execute()
        }

        fun updateTimeEntry(request: CreateTimeEntryRequest, delegate: ApiCallDelegate) {
            val pathList = listOf(request.timeEntry.id.toString())
            object : ApiCallTask(delegate) {
                override fun doInBackground(vararg params: Void?): TogglHttpResponse? {
                    return getClient().call(request, Endpoint.UpdateTimeEntry, pathList,null)
                }
            }.execute()
        }

        fun deleteTimeEntry(id: Int, delegate: ApiCallDelegate) {
            val pathList = listOf(id.toString())
            object : ApiCallTask(delegate) {
                override fun doInBackground(vararg params: Void?): TogglHttpResponse? {
                    return getClient().call(Endpoint.DeleteTimeEntry, pathList, null)
                }
            }.execute()
        }

        fun getProjectTasks(pid: Int, delegate: ApiCallDelegate) {
            val fieldSubstitutionList = listOf(pid.toString())
            object : ApiCallTask(delegate) {
                override fun doInBackground(vararg params: Void?): TogglHttpResponse? {
                    return getClient().call(Endpoint.GetProjectTasks, listOf(), fieldSubstitutionList)
                }
            }.execute()
        }
    }
}
