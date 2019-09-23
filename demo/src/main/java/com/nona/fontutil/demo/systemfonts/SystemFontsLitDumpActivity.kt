package com.nona.fontutil.demo.systemfonts

import android.app.ListActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nona.fontutil.base.IOUtil
import com.nona.fontutil.core.otparser.OpenTypeParser
import com.nona.fontutil.systemfont.FontVariant
import com.nona.fontutil.systemfont.SystemFont
import com.nona.fontutil.systemfont.SystemFonts


private data class Holder(val view: View) : RecyclerView.ViewHolder(view)

class SystemFontsLitDumpActivity : ListActivity() {

    private lateinit var listData: List<Map<String, Any>>

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listData = SystemFonts.retrieve().map {

            val indexSuffix = if (it.file.extension == "ttc" || it.file.extension == "otc") {
                "#${it.index}"
            } else {
                ""
            }

            mapOf("title" to "${it.file.name}${indexSuffix}", "font" to it)

        }.toList()

        listAdapter = SimpleAdapter(
            this,
            listData,
            android.R.layout.simple_list_item_1,
            arrayOf("title"),
            intArrayOf(android.R.id.text1)
        )
        listView.isTextFilterEnabled = true
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        val font = listData[position]["font"] as SystemFont
        val otpasrser = OpenTypeParser(IOUtil.mmap(font.file))
        val name = otpasrser.parseName()
        val style = otpasrser.parseStyle()

        val infoList = listOf(
            Pair("File Name", font.file.name),
            Pair("Family Name", name.familyName),
            Pair("Sub Family Name", name.subFamilyName),
            Pair("File Size", "${font.file.length()}"),
            Pair("Weight (from Settings)", "${font.style.weight}"),
            Pair("Weight (from OS/2 table)", "${style.weight}"),
            Pair("Italic (from Settigns)", if (font.style.italic) "Yes" else "No"),
            Pair("Italic (from OS/2 table)", if (style.italic) "Yes" else "No"),
            Pair("Language", "${font.langauge}"),
            Pair("Variant", when (font.variant) {
                FontVariant.UNSPECIFIED -> "UNSPECIFIED"
                FontVariant.COMPACT -> "COMPACT"
                FontVariant.ELEGANT -> "ELEGANT"
            })
        )
        val rv = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@SystemFontsLitDumpActivity)

            adapter = object: RecyclerView.Adapter<Holder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    Holder(this@SystemFontsLitDumpActivity.layoutInflater.inflate(
                        com.nona.fontutil.demo.R.layout.small_item, null))

                override fun getItemCount(): Int = infoList.size


                override fun onBindViewHolder(holder: Holder, position: Int) {
                    val vg = holder.view as ViewGroup
                    vg.findViewById<TextView>(com.nona.fontutil.demo.R.id.small_item_title).apply {
                        setText(infoList[position].first)
                    }

                    vg.findViewById<TextView>(com.nona.fontutil.demo.R.id.small_item_body).apply {
                        setText(infoList[position].second)
                    }
                }
            }
        }

        AlertDialog.Builder(this)
            .setView(rv)
            .show()
    }

}