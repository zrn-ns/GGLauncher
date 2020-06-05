package com.zrnns.gglauncher.assistant

import android.content.Context
import android.graphics.Color
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.graphics.toColor
import androidx.lifecycle.MutableLiveData
import com.google.assistant.embedded.v1alpha2.SpeechRecognitionResult
import com.google.auth.oauth2.UserCredentials
import com.zrnns.gglauncher.R
import org.json.JSONException
import java.io.IOException
import java.util.*

class AssistantActivityViewModel(context: Context): androidx.lifecycle.ViewModel() {

    companion object {
        private val TAG = AssistantActivity::class.java.simpleName

        // Audio constants.
        private const val PREF_CURRENT_VOLUME = "current_volume"
        private const val SAMPLE_RATE = 16000
        private const val DEFAULT_VOLUME = 100

        // Assistant SDK constants.
        private const val DEVICE_MODEL_ID = "gglauncher-dev-google-glass-zfhcc1"
        private const val DEVICE_INSTANCE_ID = "PLACEHOLDER"
        private const val LANGUAGE_CODE = "ja-JP"
    }

    enum class Status {
        WAITING_FOR_TALKING, // 待機中
        TALKING, //レスポンスを表示中（読み上げ中）
        RESULT // 結果表示
    }

    private val context: Context = context

    private var mMainHandler: Handler? = null
    // List & adapter to store and display the history of Assistant Requests.
    private lateinit var mEmbeddedAssistant: EmbeddedAssistant
    private val mAssistantRequests = ArrayList<String>()

    // LiveData
    val message: MutableLiveData<String> =
        MutableLiveData<String>()
    val messageColor: MutableLiveData<Color> =
        MutableLiveData()
    val messageTextSize: MutableLiveData<Float> =
        MutableLiveData()
    val html: MutableLiveData<String?> =
        MutableLiveData()
    private val status: MutableLiveData<Status> =
        MutableLiveData()
    val activityFinishTrigger: MutableLiveData<UUID> =
        MutableLiveData()

    init {
        status.value = Status.WAITING_FOR_TALKING
        message.value = "Listening..."
        messageColor.value = context.resources.getColor(R.color.colorPrimaryFont, context.theme) .toColor()
        messageTextSize.value = 42f
        html.value = null
    }

    // actions

    fun onCreateAction() {
        setupGoogleAssistant()
    }

    fun requestStartAction() {
        status.value = Status.WAITING_FOR_TALKING
        message.value = "Listening..."
        messageColor.value = context.resources.getColor(R.color.colorPrimaryFont, context.theme) .toColor()
        messageTextSize.value = 42f
        html.value = null
    }

    fun onTalkingAction(speechText: String) {
        status.value = Status.TALKING
        message.value = speechText
        messageColor.value = context.resources.getColor(R.color.colorPrimaryFont, context.theme) .toColor()
        messageTextSize.value = 42f
        html.value = null
    }

    fun assistantDisplayOutAction(html: String?) {
        status.value = Status.RESULT
        messageColor.value = context.resources.getColor(R.color.colorSubFont, context.theme) .toColor()
        messageTextSize.value = 22f
        this.html.value = html
    }

    fun onDestroyAction() {
        mEmbeddedAssistant.destroy()
    }

    fun viewTappedAction() {
        mEmbeddedAssistant.stopConversation()
        mEmbeddedAssistant.startConversation()
    }

    fun viewSwipedUpAction() {
        activityFinishTrigger.value = UUID.randomUUID()
    }

    fun timeoutAction() {
        activityFinishTrigger.value = UUID.randomUUID()
    }

    // private functions

    private fun setupGoogleAssistant() {
        mMainHandler = Handler(context.mainLooper)

        // Audio routing configuration: use default routing.
        val audioInputDevice: AudioDeviceInfo? = null
        val audioOutputDevice: AudioDeviceInfo? = null

        // Set volume from preferences
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(
                context
            )
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
                EmbeddedAssistant.generateCredentials(
                    context,
                    R.raw.credentials
                )
        } catch (e: IOException) {
            Log.e(
                TAG,
                "error getting user credentials",
                e
            )
        } catch (e: JSONException) {
            Log.e(
                TAG,
                "error getting user credentials",
                e
            )
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
            .setRequestCallback(object : EmbeddedAssistant.RequestCallback() {
                override fun onRequestStart() {
                    Log.i(
                        TAG,
                        "starting assistant request, enable microphones"
                    )
                    requestStartAction()
                }

                override fun onSpeechRecognition(results: List<SpeechRecognitionResult?>?) {
                    results?.filterNotNull()?.forEach {
                        onTalkingAction(it.transcript)
                    }
                }
            })
            .setConversationCallback(object : EmbeddedAssistant.ConversationCallback() {
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
                    val editor = PreferenceManager.getDefaultSharedPreferences(
                        context
                    ).edit()
                    editor.putInt(PREF_CURRENT_VOLUME, percentage)
                    editor.apply()
                }

                override fun onConversationFinished(isTimeout: Boolean) {
                    if (isTimeout) {
                        timeoutAction()
                    }
                }

                override fun onAssistantResponse(response: String?) {
                }

                override fun onAssistantDisplayOut(html: String?) {
                    mMainHandler!!.post {
                        html?.let {
                            assistantDisplayOutAction(
                                GoogleAssistantJsonParser.parse(
                                    it
                                ).infoHtml)
                        } ?: run {
                            assistantDisplayOutAction(null)
                        }
                    }
                }
            })
            .build()
        mEmbeddedAssistant.setResponseFormat(EmbeddedAssistant.HTML)
        mEmbeddedAssistant.connect()

        mEmbeddedAssistant.startConversation()
    }

    private fun findAudioDevice(deviceFlag: Int, deviceType: Int): AudioDeviceInfo? {
        val manager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val adis = manager.getDevices(deviceFlag)
        for (adi in adis) {
            if (adi.type == deviceType) {
                return adi
            }
        }
        return null
    }
}