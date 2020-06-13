package com.zrnns.gglauncher.core.speech_recognizer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import com.google.assistant.embedded.v1alpha2.SpeechRecognitionResult
import com.google.auth.oauth2.UserCredentials
import com.zrnns.gglauncher.R
import com.zrnns.gglauncher.assistant.EmbeddedAssistant
import java.util.*


// Currently using Google Assistant API.
class SpeechRecognizer(val context: Context) {

    companion object {
        // Audio constants.
        private const val SAMPLE_RATE = 16000

        // Assistant SDK constants.
        private const val DEVICE_INSTANCE_ID = "PLACEHOLDER"
        @SuppressLint("ConstantLocale")
        private val LANGUAGE_CODE = Locale.getDefault().toString()
    }

    abstract class SpeechRecognizerCallback() {
        open fun startListening() {
            // Please Implement
        }
        open fun recognitionTimeout() {
            // Please Implement
        }
        open fun recognitionInProgress(text: String) {
            // Please Implement
        }
        open fun recognitionCompleted(text: String) {
            // Please Implement
        }
    }

    private val deviceModelId: String = context.resources.openRawResource(R.raw.google_assistant_sdk_device_model_id).bufferedReader().readLine()
    private lateinit var mEmbeddedAssistant: EmbeddedAssistant
    private var mMainHandler: Handler? = null

    fun start(callback: SpeechRecognizerCallback) {
        mMainHandler = Handler(context.mainLooper)

        var userCredentials: UserCredentials? = null

        userCredentials = EmbeddedAssistant.generateCredentials(context, R.raw.credentials)

        mEmbeddedAssistant = EmbeddedAssistant.Builder()
            .setCredentials(userCredentials)
            .setDeviceInstanceId(DEVICE_INSTANCE_ID)
            .setDeviceModelId(deviceModelId)
            .setLanguageCode(LANGUAGE_CODE)
            .setAudioSampleRate(SAMPLE_RATE)
            .setAudioVolume(0)
            .setRequestCallback(object : EmbeddedAssistant.RequestCallback() {
                override fun onRequestStart() {
                    callback.startListening()
                }

                override fun onSpeechRecognition(results: List<SpeechRecognitionResult?>?) {
                    results?.filterNotNull()?.forEach {
                        if (it.stability < 1.0) {
                            callback.recognitionInProgress(it.transcript)
                        } else {
                            callback.recognitionCompleted(it.transcript)
                        }
                    }
                }

                override fun onRequestFinish() {
                    mEmbeddedAssistant.stopConversation()
                    mEmbeddedAssistant.destroy()
                }
            })
            .setConversationCallback(object : EmbeddedAssistant.ConversationCallback() {
                override fun onConversationFinished(isTimeout: Boolean) {
                    if (isTimeout) {
                        callback.recognitionTimeout()
                    }
                }
            })
            .build()
        mEmbeddedAssistant.setResponseFormat(EmbeddedAssistant.TEXT)
        mEmbeddedAssistant.connect()
        mEmbeddedAssistant.startConversation()
    }
}