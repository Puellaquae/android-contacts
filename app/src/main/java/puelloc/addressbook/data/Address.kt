package puelloc.addressbook.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType

private fun getRuby(str: String): String {
    val format = HanyuPinyinOutputFormat().apply {
        toneType = HanyuPinyinToneType.WITHOUT_TONE
    }
    val ruby = str.map {
        val pinyins = PinyinHelper.toHanyuPinyinStringArray(it, format)
        if (pinyins == null || pinyins.isEmpty()) {
            it.lowercase()
        } else if (pinyins[0] == "none") {
            ""
        } else {
            pinyins[0]
        }
    }.joinToString("")
    if (ruby == "") {
        return "#$str"
    } else if (!(ruby.first() in 'a'..'z')) {
        return "#$ruby"
    } else {
        return ruby
    }
}

@Entity(indices = [Index("ruby")])
data class Address(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val ruby: String = getRuby(name)
)