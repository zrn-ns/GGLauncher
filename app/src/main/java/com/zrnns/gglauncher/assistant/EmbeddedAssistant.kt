package com.zrnns.gglauncher.assistant

import android.content.Context
import android.media.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.IntDef
import com.google.assistant.embedded.v1alpha2.*
import com.google.assistant.embedded.v1alpha2.DialogStateOut.MicrophoneMode
import com.google.assistant.embedded.v1alpha2.EmbeddedAssistantGrpc.EmbeddedAssistantStub
import com.google.auth.oauth2.UserCredentials
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import io.grpc.auth.MoreCallCredentials
import io.grpc.stub.StreamObserver
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.nio.ByteBuffer
import java.util.*


class EmbeddedAssistant private constructor() {

    companion object {
        private val TAG = EmbeddedAssistant::class.java.simpleName
        private const val ASSISTANT_API_ENDPOINT = "embeddedassistant.googleapis.com"
        private const val AUDIO_RECORD_BLOCK_SIZE = 1024
        const val TEXT = 0
        const val HTML = 1

        /**
         * Generates access tokens for the Assistant based on a credentials JSON file.
         *
         * @param context Application context
         * @param resourceId The resource that contains the project credentials
         *
         * @return A [UserCredentials] object which can be used by the Assistant.
         * @throws IOException If the resource does not exist.
         * @throws JSONException If the resource is incorrectly formatted.
         */
        fun generateCredentials(context: Context, resourceId: Int): UserCredentials {
            val inputStream = context.resources.openRawResource(resourceId)
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes)
            val json = JSONObject(String(bytes, Charsets.UTF_8))
            return UserCredentials(
                json.getString("client_id"),
                json.getString("client_secret"),
                json.getString("refresh_token")
            )
        }
    }

    // Device Actions
    private var mDeviceConfig: DeviceConfig? = null

    // Callbacks
    private var mRequestHandler: Handler? = null
    private var mRequestCallback: RequestCallback? = null
    private var mConversationHandler: Handler? = null
    private var mConversationCallback: ConversationCallback? = null

    // Assistant Thread and Runnables implementing the push-to-talk functionality.
    private var mConversationState: ByteString? = null
    private var mLanguageCode = "en-US"
    private var mAudioRecord: AudioRecord? = null
    private var mDeviceLocation: DeviceLocation? = null
    private var mAudioInConfig: AudioInConfig? = null
    private var mAudioOutConfig: AudioOutConfig? = null
    private var mAudioInputDevice: AudioDeviceInfo? = null
    private var mAudioOutputDevice: AudioDeviceInfo? = null
    private var mAudioInputFormat: AudioFormat? = null
    private var mAudioOutputFormat: AudioFormat? = null
    private var mAudioInputBufferSize = 0
    private var mAudioOutputBufferSize = 0
    private var mVolume = 100 // Default to maximum volume.
    private var mScreenOutConfig: ScreenOutConfig? = null
    private var mMicrophoneMode: MicrophoneMode? = null
    private var mAssistantThread: HandlerThread? = null
    private var mAssistantHandler: Handler? = null
    private val mAssistantResponses = ArrayList<ByteBuffer>()
    private var mAssistDisplayOut: ScreenOut? = null

    // gRPC client and stream observers.
    private var mAudioOutSize // Tracks the size of audio responses to determine when it ends.
            = 0
    private var mAssistantService: EmbeddedAssistantStub? = null
    private var mAssistantRequestObserver: StreamObserver<AssistRequest>? = null
    private val mAssistantResponseObserver: StreamObserver<AssistResponse> = object :
        StreamObserver<AssistResponse> {
        override fun onNext(value: AssistResponse) {
            if (value.deviceAction != null &&
                !value.deviceAction.deviceRequestJson.isEmpty()
            ) {
                // Iterate through JSON object
                try {
                    val deviceAction = JSONObject(
                        value.deviceAction
                            .deviceRequestJson
                    )
                    val inputs = deviceAction.getJSONArray("inputs")
                    for (i in 0 until inputs.length()) {
                        if (inputs.getJSONObject(i).getString("intent") ==
                            "action.devices.EXECUTE"
                        ) {
                            val commands = inputs.getJSONObject(i)
                                .getJSONObject("payload")
                                .getJSONArray("commands")
                            for (j in 0 until commands.length()) {
                                val execution = commands.getJSONObject(j)
                                    .getJSONArray("execution")
                                for (k in 0 until execution.length()) {
                                    mConversationHandler!!.post {
                                        try {
                                            mConversationCallback!!.onDeviceAction(
                                                execution
                                                    .getJSONObject(k)
                                                    .getString("command"),
                                                execution.getJSONObject(k)
                                                    .optJSONObject("params")
                                            )
                                        } catch (e: JSONException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            if (value.eventType == AssistResponse.EventType.END_OF_UTTERANCE) {
                mRequestHandler!!.post { mRequestCallback!!.onRequestFinish() }
                mConversationHandler!!.post { mConversationCallback!!.onResponseStarted() }
            }
            if (value.dialogStateOut != null) {
                mConversationState = value.dialogStateOut.conversationState
                if (value.dialogStateOut.volumePercentage != 0) {
                    val volumePercentage = value.dialogStateOut.volumePercentage
                    mVolume = volumePercentage
                    mConversationHandler!!.post {
                        mConversationCallback!!.onVolumeChanged(
                            volumePercentage
                        )
                    }
                }
                mRequestHandler!!.post { mRequestCallback!!.onSpeechRecognition(value.speechResultsList) }
                mMicrophoneMode = value.dialogStateOut.microphoneMode
                mConversationCallback!!.onAssistantResponse(
                    value.dialogStateOut
                        .supplementalDisplayText
                )
            }
            if (value.audioOut != null) {
                if (mAudioOutSize <= value.audioOut.serializedSize) {
                    mAudioOutSize = value.audioOut.serializedSize
                } else {
                    mAudioOutSize = 0
                    onCompleted()
                }
                val audioData =
                    ByteBuffer.wrap(value.audioOut.audioData.toByteArray())
                mAssistantResponses.add(audioData)
                mConversationHandler!!.post { mConversationCallback!!.onAudioSample(audioData) }
            }
            if (value.hasScreenOut()) {
                mAssistDisplayOut = value.screenOut
                mConversationHandler!!.post {
                    mConversationCallback!!.onAssistantDisplayOut(
                        value.screenOut.data.toStringUtf8()
                    )
                }
            }
        }

        override fun onError(t: Throwable) {
            mConversationHandler!!.post { mConversationCallback!!.onError(t) }
        }

        override fun onCompleted() {
            // create a new AudioTrack to workaround audio routing issues.
            val audioTrack = AudioTrack.Builder()
                .setAudioFormat(mAudioOutputFormat!!)
                .setBufferSizeInBytes(mAudioOutputBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            if (mAudioOutputDevice != null) {
                audioTrack.preferredDevice = mAudioOutputDevice
            }
            audioTrack.setVolume(AudioTrack.getMaxVolume() * mVolume / 100.0f)
            audioTrack.play()
            mConversationHandler!!.post { mConversationCallback!!.onResponseStarted() }
            for (audioData in mAssistantResponses) {
                audioTrack.write(
                    audioData, audioData.remaining(),
                    AudioTrack.WRITE_BLOCKING
                )
            }
            audioTrack.stop()
            audioTrack.release()
            mConversationHandler!!.post { mConversationCallback!!.onResponseFinished() }
            if (mMicrophoneMode == MicrophoneMode.DIALOG_FOLLOW_ON) {
                // Automatically start a new request
                startConversation()
            } else {
                // The conversation is done
                mConversationHandler!!.post { mConversationCallback!!.onConversationFinished(mAssistantResponses.isEmpty() && mAssistDisplayOut == null) }
            }
            mAssistantResponses.clear()
        }
    }
    private val mStreamAssistantRequest: Runnable = object : Runnable {
        override fun run() {
            val audioData =
                ByteBuffer.allocateDirect(AUDIO_RECORD_BLOCK_SIZE)
            val result = mAudioRecord!!.read(
                audioData, audioData.capacity(),
                AudioRecord.READ_BLOCKING
            )
            if (result < 0) {
                return
            }
            mRequestHandler!!.post { mRequestCallback!!.onAudioRecording() }
            mAssistantRequestObserver!!.onNext(
                AssistRequest.newBuilder()
                    .setAudioIn(ByteString.copyFrom(audioData))
                    .build()
            )
            mAssistantHandler!!.post(this)
        }
    }
    private var mUserCredentials: UserCredentials? = null

    /**
     * Initializes the Assistant.
     */
    fun connect() {
        mAssistantThread = HandlerThread("assistantThread")
        mAssistantThread!!.start()
        mAssistantHandler = Handler(mAssistantThread!!.looper)
        val channel =
            ManagedChannelBuilder.forTarget(ASSISTANT_API_ENDPOINT)
                .build()
        mAssistantService = EmbeddedAssistantGrpc.newStub(channel)
            .withCallCredentials(MoreCallCredentials.from(mUserCredentials))
    }

    /**
     * Starts a request to the Assistant.
     */
    fun startConversation() {
        mAudioRecord!!.startRecording()
        mRequestHandler!!.post { mRequestCallback!!.onRequestStart() }
        mAssistantHandler!!.post {
            mAssistantRequestObserver = mAssistantService!!.assist(mAssistantResponseObserver)
            val assistConfigBuilder = AssistConfig.newBuilder()
                .setAudioInConfig(mAudioInConfig)
                .setAudioOutConfig(mAudioOutConfig)
                .setDeviceConfig(mDeviceConfig)
            if (mScreenOutConfig != null) {
                assistConfigBuilder.screenOutConfig = mScreenOutConfig
            }
            val dialogStateInBuilder = DialogStateIn.newBuilder()
            if (mConversationState != null) {
                dialogStateInBuilder.conversationState = mConversationState
            }
            if (mDeviceLocation != null) {
                dialogStateInBuilder.deviceLocation = mDeviceLocation
            }
            dialogStateInBuilder.languageCode = mLanguageCode
            assistConfigBuilder.dialogStateIn = dialogStateInBuilder.build()
            mAssistantRequestObserver!!.onNext(
                AssistRequest.newBuilder()
                    .setConfig(assistConfigBuilder.build())
                    .build()
            )
        }
        mAssistantHandler!!.post(mStreamAssistantRequest)
        mAssistDisplayOut = null
    }

    /**
     * Manually ends a conversation with the Assistant.
     */
    fun stopConversation() {
        mAssistantHandler!!.post {
            mAssistantHandler!!.removeCallbacks(mStreamAssistantRequest)
            if (mAssistantRequestObserver != null) {
                mAssistantRequestObserver!!.onCompleted()
                mAssistantRequestObserver = null
            }
        }
        mAudioRecord!!.stop()
        mAssistantResponses.clear()
        mConversationHandler!!.post { mConversationCallback!!.onConversationFinished(false) }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(TEXT, HTML)
    annotation class ResponseFormat

    /**
     * Set desired assistant response format.
     */
    fun setResponseFormat(@ResponseFormat format: Int) {
        mScreenOutConfig = ScreenOutConfig.newBuilder()
            .setScreenMode(if (format == HTML) ScreenOutConfig.ScreenMode.PLAYING else ScreenOutConfig.ScreenMode.SCREEN_MODE_UNSPECIFIED)
            .build()
    }

    /**
     * Removes callbacks and exists the Assistant service. This should be called when an activity is
     * closing to safely quit the Assistant service.
     */
    fun destroy() {
        mAssistantHandler!!.post { mAssistantHandler!!.removeCallbacks(mStreamAssistantRequest) }
        mAssistantThread!!.quitSafely()
        if (mAudioRecord != null) {
            mAudioRecord!!.stop()
            mAudioRecord = null
        }
    }

    /**
     * Used to build an AssistantManager object.
     */
    class Builder {
        private val mEmbeddedAssistant: EmbeddedAssistant
        private var mSampleRate = 0
        private var mDeviceModelId: String? = null
        private var mDeviceInstanceId: String? = null

        /**
         * Sets a preferred [AudioDeviceInfo] device for input.
         *
         * @param device The preferred audio device to acquire audio from.
         * @return Returns this builder to allow for chaining.
         */
        fun setAudioInputDevice(device: AudioDeviceInfo?): Builder {
            mEmbeddedAssistant.mAudioInputDevice = device
            return this
        }

        /**
         * Sets a preferred [AudioDeviceInfo] device for output.
         *
         * param device The preferred audio device to route audio to.
         * @return Returns this builder to allow for chaining.
         */
        fun setAudioOutputDevice(device: AudioDeviceInfo?): Builder {
            mEmbeddedAssistant.mAudioOutputDevice = device
            return this
        }

        /**
         * Sets a [RequestCallback], which is when a request is being made to the Assistant.
         *
         * @param requestCallback The methods that will run during a request.
         * @return Returns this builder to allow for chaining.
         */
        fun setRequestCallback(requestCallback: RequestCallback?): Builder {
            setRequestCallback(requestCallback, null)
            return this
        }

        /**
         * Sets a [RequestCallback], which is when a request is being made to the Assistant.
         *
         * @param requestCallback The methods that will run during a request.
         * @param requestHandler Handler used to dispatch the callback.
         * @return Returns this builder to allow for chaining.
         */
        fun setRequestCallback(
            requestCallback: RequestCallback?,
            requestHandler: Handler?
        ): Builder {
            var requestHandler = requestHandler
            if (requestHandler == null) {
                requestHandler = Handler()
            }
            mEmbeddedAssistant.mRequestCallback = requestCallback
            mEmbeddedAssistant.mRequestHandler = requestHandler
            return this
        }

        /**
         * Sets a [ConversationCallback], which is when a response is being given from the
         * Assistant.
         *
         * @param responseCallback The methods that will run during a response.
         * @return Returns this builder to allow for chaining.
         */
        fun setConversationCallback(responseCallback: ConversationCallback?): Builder {
            setConversationCallback(responseCallback, null)
            return this
        }

        /**
         * Sets a [ConversationCallback], which is when a response is being given from the
         * Assistant.
         *
         * @param responseCallback The methods that will run during a response.
         * @param responseHandler Handler used to dispatch the callback.
         * @return Returns this builder to allow for chaining.
         */
        fun setConversationCallback(
            responseCallback: ConversationCallback?,
            responseHandler: Handler?
        ): Builder {
            var responseHandler = responseHandler
            if (responseHandler == null) {
                responseHandler = Handler()
            }
            mEmbeddedAssistant.mConversationCallback = responseCallback
            mEmbeddedAssistant.mConversationHandler = responseHandler
            return this
        }

        /**
         * Sets the credentials for the user.
         *
         * @param userCredentials Credentials generated by
         * [EmbeddedAssistant.generateCredentials].
         * @return Returns this builder to allow for chaining.
         */
        fun setCredentials(userCredentials: UserCredentials?): Builder {
            mEmbeddedAssistant.mUserCredentials = userCredentials
            return this
        }

        /**
         * Sets the audio sampling rate for input and output streams
         *
         * @param sampleRate The audio sample rate
         * @return Returns this builder to allow for chaining.
         */
        fun setAudioSampleRate(sampleRate: Int): Builder {
            mSampleRate = sampleRate
            return this
        }

        /**
         * Sets the volume for the Assistant response
         *
         * @param volume The audio volume in the range 0 - 100.
         * @return Returns this builder to allow for chaining.
         */
        fun setAudioVolume(volume: Int): Builder {
            mEmbeddedAssistant.mVolume = volume
            return this
        }

        /**
         * Sets the model id for each Assistant request.
         *
         * @param deviceModelId The device model id.
         * @return Returns this builder to allow for chaining.
         */
        fun setDeviceModelId(deviceModelId: String?): Builder {
            mDeviceModelId = deviceModelId
            return this
        }

        /**
         * Sets the instance id for each Assistant request.
         *
         * @param deviceInstanceId The device instance id.
         * @return Returns this builder to allow for chaining.
         */
        fun setDeviceInstanceId(deviceInstanceId: String?): Builder {
            mDeviceInstanceId = deviceInstanceId
            return this
        }

        /**
         * Sets language code of the request using IETF BCP 47 syntax.
         * See [for the documentation](https://tools.ietf.org/html/bcp47).
         * For example: "en-US".
         *
         * @param languageCode Code for the language. Only Assistant-supported languages are valid.
         * @return Returns this builder to allow for chaining.
         */
        fun setLanguageCode(languageCode: String): Builder {
            mEmbeddedAssistant.mLanguageCode = languageCode
            return this
        }

        fun setDeviceLocation(deviceLocation: DeviceLocation?): Builder {
            mEmbeddedAssistant.mDeviceLocation = deviceLocation
            return this
        }

        /**
         * Returns an AssistantManager if all required parameters have been supplied.
         *
         * @return An inactive AssistantManager. Call [EmbeddedAssistant.connect] to start
         * it.
         */
        fun build(): EmbeddedAssistant {
            if (mEmbeddedAssistant.mRequestCallback == null) {
                throw NullPointerException("There must be a defined RequestCallback")
            }
            if (mEmbeddedAssistant.mConversationCallback == null) {
                throw NullPointerException("There must be a defined ConversationCallback")
            }
            if (mEmbeddedAssistant.mUserCredentials == null) {
                throw NullPointerException("There must be provided credentials")
            }
            if (mSampleRate == 0) {
                throw NullPointerException("There must be a defined sample rate")
            }
            val audioEncoding = AudioFormat.ENCODING_PCM_16BIT

            // Construct audio configurations.
            mEmbeddedAssistant.mAudioInConfig = AudioInConfig.newBuilder()
                .setEncoding(AudioInConfig.Encoding.LINEAR16)
                .setSampleRateHertz(mSampleRate)
                .build()
            mEmbeddedAssistant.mAudioOutConfig = AudioOutConfig.newBuilder()
                .setEncoding(AudioOutConfig.Encoding.LINEAR16)
                .setSampleRateHertz(mSampleRate)
                .setVolumePercentage(mEmbeddedAssistant.mVolume)
                .build()

            // Initialize Audio framework parameters.
            mEmbeddedAssistant.mAudioInputFormat = AudioFormat.Builder()
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .setEncoding(audioEncoding)
                .setSampleRate(mSampleRate)
                .build()
            mEmbeddedAssistant.mAudioInputBufferSize = AudioRecord.getMinBufferSize(
                mEmbeddedAssistant.mAudioInputFormat!!.getSampleRate(),
                mEmbeddedAssistant.mAudioInputFormat!!.getChannelMask(),
                mEmbeddedAssistant.mAudioInputFormat!!.getEncoding()
            )
            mEmbeddedAssistant.mAudioOutputFormat = AudioFormat.Builder()
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(audioEncoding)
                .setSampleRate(mSampleRate)
                .build()
            mEmbeddedAssistant.mAudioOutputBufferSize = AudioTrack.getMinBufferSize(
                mEmbeddedAssistant.mAudioOutputFormat!!.getSampleRate(),
                mEmbeddedAssistant.mAudioOutputFormat!!.getChannelMask(),
                mEmbeddedAssistant.mAudioOutputFormat!!.getEncoding()
            )

            // create new AudioRecord to workaround audio routing issues.
            mEmbeddedAssistant.mAudioRecord = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                .setAudioFormat(mEmbeddedAssistant.mAudioInputFormat!!)
                .setBufferSizeInBytes(mEmbeddedAssistant.mAudioInputBufferSize)
                .build()
            if (mEmbeddedAssistant.mAudioInputDevice != null) {
                val result = mEmbeddedAssistant!!.mAudioRecord!!.setPreferredDevice(
                    mEmbeddedAssistant.mAudioInputDevice
                )
                if (!result) {
                    Log.e(
                        TAG,
                        "failed to set preferred input device"
                    )
                }
            }

            // Construct DeviceConfig
            mEmbeddedAssistant.mDeviceConfig = DeviceConfig.newBuilder()
                .setDeviceId(mDeviceInstanceId)
                .setDeviceModelId(mDeviceModelId)
                .build()

            // Construct default ScreenOutConfig
            mEmbeddedAssistant.mScreenOutConfig = ScreenOutConfig.newBuilder()
                .setScreenMode(ScreenOutConfig.ScreenMode.SCREEN_MODE_UNSPECIFIED)
                .build()
            return mEmbeddedAssistant
        }

        /**
         * Creates a Builder.
         */
        init {
            mEmbeddedAssistant = EmbeddedAssistant()
        }
    }

    /**
     * Callback for methods during a request to the Assistant.
     */
    abstract class RequestCallback {
        /**
         * Called when a request is first made.
         */
        open fun onRequestStart() {}

        /**
         * Called when a request has completed.
         */
        open fun onRequestFinish() {}

        /**
         * Called when audio is being recording. This may be called multiple times during a single
         * request.
         */
        fun onAudioRecording() {}

        /**
         * Called when the request is complete and the Assistant returns the user's speech-to-text.
         */
        open fun onSpeechRecognition(results: List<SpeechRecognitionResult?>?) {}
    }

    /**
     * Callback for methods during a conversation from the Assistant.
     */
    abstract class ConversationCallback {
        /**
         * Called when the user's voice query ends and the response from the Assistant is about to
         * start a response.
         */
        fun onResponseStarted() {}

        /**
         * Called when the Assistant's response is complete.
         */
        fun onResponseFinished() {}

        /**
         * Called when audio is being played. This may be called multiple times during a single
         * response. The audio will play using the AudioTrack, although this method may be used
         * to provide auxiliary effects.
         *
         * @param audioSample The raw audio sample from the Assistant
         */
        fun onAudioSample(audioSample: ByteBuffer?) {}

        /**
         * Called when an error occurs during the response
         *
         * @param throwable A [Throwable] which contains information about the response error.
         */
        open fun onError(throwable: Throwable?) {}

        /**
         * Called when the user requests to change the Assistant's volume.
         *
         * @param percentage The desired volume as a percentage of intensity, in the range 0 - 100.
         */
        open fun onVolumeChanged(percentage: Int) {}

        /**
         * Called when the response contains a DeviceAction.
         *
         * @param intentName The name of the intent to execute.
         * @param parameters A JSONObject containing parameters related to this intent.
         */
        open fun onDeviceAction(intentName: String?, parameters: JSONObject?) {}

        /**
         * Called when the response contains supplemental display text from the Assistant.
         *
         * @param response Supplemental display text.
         */
        open fun onAssistantResponse(response: String?) {}

        /**
         * Called when the response contains HTML output from the Assistant.
         *
         * @param html HTML data showing a rich response
         */
        open fun onAssistantDisplayOut(html: String?) {}

        /**
         * Called when the entire conversation is finished.
         */
        open fun onConversationFinished(isTimeout: Boolean) {}
    }
}
