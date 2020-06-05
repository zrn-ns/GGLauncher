package com.zrnns.gglauncher.assistant

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class GoogleAssistantJsonParser {

    data class Result(
        val imageUrls: List<String>,
        val message: String,
        val infoHtml: String
    )

    companion object {
        private val UNUSABLE_IMAGE_URLS = listOf("https://www.gstatic.com/actions/devices_platform/assistant_horizontal_logos/GoogleAssistant_white_ja-JP.svg")
        private val UNUSABLE_IMAGE_URL_REGEX_LIST = listOf(Regex("""^https:\/\/www\.gstatic\.com\/images\/icons\/.*$"""))

        fun parse(html: String): Result {
            val document = Jsoup.parse(html)
            val imageUrls = document.select("img")
                .map { it -> String
                    val src = it.absUrl("src")
                    val dataSrc = it.absUrl("data-src")
                    if (src.isNotEmpty()) {
                        src
                    } else {
                        dataSrc
                    }
                }
                .filter { it.isNotEmpty() }
                .filter { !UNUSABLE_IMAGE_URLS.contains(it) }
                .filter {
                    UNUSABLE_IMAGE_URL_REGEX_LIST.none { regex -> regex.containsMatchIn(it) }
                }
            var infoHtml: String = generateInfoHtml(document)
            var message = Jsoup.parse(infoHtml).text()
            return Result(imageUrls, message, infoHtml)
        }

        private fun generateInfoHtml(document: Document): String {
            var mainAreaElement = document.getElementById("assistant-main-cards")
            // 見辛いのでいったんやめる
//            mainAreaElement.allElements.forEach {
//                it.attr("style", "flex-wrap:wrap; color: white;")
//            }
            mainAreaElement.removeAttr("style")
            mainAreaElement.getElementById("assistant-bar")?.remove()

            return mainAreaElement.html()
        }
    }
}