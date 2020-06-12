package com.zrnns.gglauncher.core.speech_recognizer

class SpeechRecognizer {
    fun start(callback: SpeechRecognizerCallback) {
        // TODO: 実装
        Thread {
            Thread.sleep(1000)
            callback.recognitionInProgress("世界の")
            Thread.sleep(1000)
            callback.recognitionInProgress("世界のヨコサワ")
            Thread.sleep(1000)
            callback.recognitionInProgress("世界のヨコサワチャンネル")
            Thread.sleep(1000)
            callback.recognitionCompleted("世界のヨコサワチャンネル")
        }.start()
    }

    abstract class SpeechRecognizerCallback() {
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
}