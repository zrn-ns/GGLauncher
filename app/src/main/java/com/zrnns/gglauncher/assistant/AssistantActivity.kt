package com.zrnns.gglauncher.assistant

import android.app.Activity
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.*
import com.google.assistant.embedded.v1alpha2.SpeechRecognitionResult
import com.google.auth.oauth2.UserCredentials
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.assistant.EmbeddedAssistant.Companion.generateCredentials
import com.zrnns.gglauncher.assistant.EmbeddedAssistant.ConversationCallback
import com.zrnns.gglauncher.assistant.EmbeddedAssistant.RequestCallback
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.*

class AssistantActivity : Activity() {
    private lateinit var mButtonWidget: Button
    private var mMainHandler: Handler? = null

    // List & adapter to store and display the history of Assistant Requests.
    private lateinit var mEmbeddedAssistant: EmbeddedAssistant
    private val mAssistantRequests =
        ArrayList<String>()
    private var mAssistantRequestsAdapter: ArrayAdapter<String>? = null
    private lateinit var mHtmlOutputCheckbox: CheckBox
    private lateinit var mWebView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "starting assistant demo")
        setContentView(R.layout.activity_main)
        val assistantRequestsListView =
            findViewById<ListView>(R.id.assistantRequestsListView)
        mAssistantRequestsAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            mAssistantRequests
        )
        assistantRequestsListView.adapter = mAssistantRequestsAdapter
        mHtmlOutputCheckbox = findViewById(R.id.htmlOutput)
        mHtmlOutputCheckbox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, useHtml ->
            mWebView.visibility = if (useHtml) View.VISIBLE else View.GONE
            assistantRequestsListView.visibility = if (useHtml) View.GONE else View.VISIBLE
            mEmbeddedAssistant!!.setResponseFormat(if (useHtml) EmbeddedAssistant.HTML else EmbeddedAssistant.TEXT)
        })
        mWebView = findViewById(R.id.webview)
        mWebView.getSettings().javaScriptEnabled = true
        mMainHandler = Handler(mainLooper)
        mButtonWidget = findViewById(R.id.assistantQueryButton)
        mButtonWidget.setOnClickListener(View.OnClickListener { mEmbeddedAssistant.startConversation() })

        // Audio routing configuration: use default routing.
        val audioInputDevice: AudioDeviceInfo? = null
        val audioOutputDevice: AudioDeviceInfo? = null

        // Set volume from preferences
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val initVolume = preferences.getInt(
            PREF_CURRENT_VOLUME,
            DEFAULT_VOLUME
        )
        Log.i(
            TAG,
            "setting audio track volume to: $initVolume"
        )
        var userCredentials: UserCredentials? = null
        try {
            userCredentials =
                generateCredentials(this, R.raw.credentials)
        } catch (e: IOException) {
            Log.e(TAG, "error getting user credentials", e)
        } catch (e: JSONException) {
            Log.e(TAG, "error getting user credentials", e)
        }
        mEmbeddedAssistant = EmbeddedAssistant.Builder()
            .setCredentials(userCredentials)
            .setDeviceInstanceId(DEVICE_INSTANCE_ID)
            .setDeviceModelId(DEVICE_MODEL_ID)
            .setLanguageCode(LANGUAGE_CODE)
            .setAudioInputDevice(audioInputDevice)
            .setAudioOutputDevice(audioOutputDevice)
            .setAudioSampleRate(SAMPLE_RATE)
            .setAudioVolume(initVolume)
            .setRequestCallback(object : RequestCallback() {
                override fun onRequestStart() {
                    Log.i(
                        TAG,
                        "starting assistant request, enable microphones"
                    )
                    mButtonWidget.setText("聞き取り中")
                    mButtonWidget.setEnabled(false)
                }

                override fun onSpeechRecognition(results: List<SpeechRecognitionResult?>?) {
                    for (result in results!!) {
                        Log.i(
                            TAG,
                            "assistant request text: " + result!!.transcript +
                                    " stability: " + java.lang.Float.toString(result.stability)
                        )
                        mAssistantRequestsAdapter!!.add(result.transcript)
                    }
                }
            })
            .setConversationCallback(object : ConversationCallback() {
                override fun onError(throwable: Throwable?) {
                    Log.e(
                        TAG,
                        "assist error: " + throwable!!.message,
                        throwable
                    )
                }

                override fun onVolumeChanged(percentage: Int) {
                    Log.i(
                        TAG,
                        "assistant volume changed: $percentage"
                    )
                    // Update our shared preferences
                    val editor = PreferenceManager
                        .getDefaultSharedPreferences(this@AssistantActivity)
                        .edit()
                    editor.putInt(PREF_CURRENT_VOLUME, percentage)
                    editor.apply()
                }

                override fun onConversationFinished() {
                    Log.i(
                        TAG,
                        "assistant conversation finished"
                    )
                    mButtonWidget.setText("聞き取る")
                    mButtonWidget.setEnabled(true)
                }

                override fun onAssistantResponse(response: String?) {
                    if (!response!!.isEmpty()) {
                        Log.i(TAG, response)
                        mMainHandler!!.post { mAssistantRequestsAdapter!!.add("Google Assistant: $response") }
                    }
                }

                override fun onAssistantDisplayOut(html: String?) {
                    mMainHandler!!.post { // Need to convert to base64
                        try {
                            Log.i(TAG, html)
                            val data = html!!.toByteArray(charset("UTF-8"))
                            val base64String =
                                Base64.encodeToString(
                                    data,
                                    Base64.DEFAULT
                                )
                            mWebView.loadData(
                                base64String, "text/html; charset=utf-8",
                                "base64"
                            )
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onDeviceAction(
                    intentName: String?,
                    parameters: JSONObject?
                ) {
                    if (parameters != null) {
                        Log.d(
                            TAG,
                            "Get device action " + intentName + " with parameters: " +
                                    parameters.toString()
                        )
                    } else {
                        Log.d(
                            TAG,
                            "Get device action " + intentName + " with no paramete"
                                    + "rs"
                        )
                    }
                    if (intentName == "action.devices.commands.OnOff") {
                        try {
                            val turnOn = parameters!!.getBoolean("on")
                            //                                mLed.setValue(turnOn);
                        } catch (e: JSONException) {
                            Log.e(
                                TAG,
                                "Cannot get value of command",
                                e
                            )
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "Cannot set value of LED",
                                e
                            )
                        }
                    }
                }
            })
            .build()
        mEmbeddedAssistant!!.connect()
    }

    private fun findAudioDevice(deviceFlag: Int, deviceType: Int): AudioDeviceInfo? {
        val manager =
            this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val adis = manager.getDevices(deviceFlag)
        for (adi in adis) {
            if (adi.type == deviceType) {
                return adi
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "destroying assistant demo")
        mEmbeddedAssistant!!.destroy()
    }

    companion object {
        private val TAG = AssistantActivity::class.java.simpleName

        // Audio constants.
        private const val PREF_CURRENT_VOLUME = "current_volume"
        private const val SAMPLE_RATE = 16000
        private const val DEFAULT_VOLUME = 100

        // Assistant SDK constants.
        private const val DEVICE_MODEL_ID = "PLACEHOLDER"
        private const val DEVICE_INSTANCE_ID = "PLACEHOLDER"
        private const val LANGUAGE_CODE = "ja-JP"
    }
}