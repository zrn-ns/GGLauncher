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
import java.util.*

class NewsFragment : Fragment() {

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
            val url = getRssURL()
            val document = Jsoup.connect(url).parser(Parser.xmlParser()).get()
            val titles = document.select("item>title").shuffled().take(2).map { it.html() }

            activity?.runOnUiThread {
                titles.getOrNull(0)?.let {
                    textView1.text = it
                }
                titles.getOrNull(1)?.let {
                    textView2.text = it
                }
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
}