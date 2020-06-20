package com.zrnns.gglauncher.launcher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zrnns.gglauncher.R
import kotlinx.android.synthetic.main.fragment_news.*
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.IOException
import java.util.*

class NewsFragment : Fragment() {

    companion object {
        private const val UPDATE_FREQUENCY_MINUTES: Int = 5

        private var latestNewsDataCache: CachedNewsData? = null
    }

    private data class CachedNewsData(val news1Text: String?, val news2Text: String?, val updateDate: Date)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onResume() {
        super.onResume()

        Thread {
            if (isNewsNotLoadedOrStale()) {
                val url = getRssURL()

                var document: org.jsoup.nodes.Document
                try {
                    document = Jsoup.connect(url).parser(Parser.xmlParser()).get()
                } catch (e: IOException) {
                    return@Thread
                }
                val titles = document.select("item>title").shuffled().take(2).map { it.html() }

                latestNewsDataCache = CachedNewsData(titles.getOrNull(0), titles.getOrNull(1), Date())
            }

            activity?.runOnUiThread {
                textView1.text = latestNewsDataCache?.news1Text
                textView2.text = latestNewsDataCache?.news2Text
            }
        }.start()
    }

    private fun getRssURL(): String {
        // Parameter Details: https://qiita.com/KMD/items/872d8f4eed5d6ebf5df1

        val locale = Locale.getDefault().toString() // ja_JP
        val country = Locale.getDefault().country // JP
        val lang = Locale.getDefault().language // ja

        val topic = "TECHNOLOGY"

        return "https://news.google.com/news/rss/headlines/section/topic/TECHNOLOGY?hl=$lang&gl=$country&ceid=$country:$lang"
    }

    private fun isNewsNotLoadedOrStale(): Boolean {
        val latestNewsDataCache = latestNewsDataCache?.let { it } ?: return true

        val expireDateCalendar = {
            val tmpCalendar = Calendar.getInstance()
            tmpCalendar.time = latestNewsDataCache.updateDate
            tmpCalendar.add(Calendar.MINUTE, UPDATE_FREQUENCY_MINUTES)
            tmpCalendar
        }()

        val currentDateCalendar = {
            val tmpCalendar = Calendar.getInstance()
            tmpCalendar.time = Date()
            tmpCalendar
        }()
        return currentDateCalendar.after(expireDateCalendar)
    }
}