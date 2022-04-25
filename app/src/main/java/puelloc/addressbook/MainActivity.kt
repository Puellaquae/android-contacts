package puelloc.addressbook

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        val addressData = AddressDB(applicationContext)
        val books = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = AddressesAdapter(addressData)
        books.adapter = adapter
        addressData.bindAdapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.new_contact_button)
        fab.setOnClickListener {
            val dialog = ContactDialog(addressData)
            val transaction = supportFragmentManager.beginTransaction()
            transaction
                .add(android.R.id.content, dialog)
                .addToBackStack("contact dialog")
                .commit()
        }

        FastScrollerBuilder(books).setPopupTextProvider {
            addressData[it].ruby[0].uppercaseChar().toString()
        }.setPadding(0, 16, 0, 64).useMd2Style().build()

        fun getRandomString(length: Int): String {
            val allowedChars = "力明永健世广志义" +
                    "兴良海山仁丰波森" +
                    "波宁贵福生龙元全" +
                    "国胜学祥才佳强宁" +
                    "发武新利清飞彬富" +
                    "顺信子杰涛鹏宇衡" +
                    "昌成康星光天达安" +
                    "岩中茂进林厚庆磊" +
                    "有坚和彪博诚先敬" +
                    "震振壮会思功松善" +
                    "群豪心邦承乐绍民" +
                    "友裕河哲江超浩亮" +
                    "政谦亨奇航达杰兴" +
                    "固之轮翰朗伯宏言" +
                    "若鸣朋斌梁方仁世" +
                    "栋维启克伦翔旭鹏" +
                    "泽晨辰士以梓竹至" +
                    "建家致树炎德行时" +
                    "泰盛雄琛钧亦丞州" +
                    "晨轩清睿宝涛华国" +
                    "亮新凯志明友才振" +
                    "伟嘉东洪建文子云" +
                    "秀娟英华慧巧美娜" +
                    "静淑惠珠翠雅芝玉" +
                    "萍红娥玲芬芳燕彩" +
                    "春菊兰凤洁梅琳素" +
                    "云莲真环雪荣爱妹" +
                    "霞香月莺媛艳瑞凡" +
                    "佳嘉琼勤珍贞莉桂" +
                    "娣叶璧璐娅琦晶妍" +
                    "茜秋珊莎锦黛青倩" +
                    "婷姣婉娴瑾颖露瑶" +
                    "怡婵雁蓓纨仪荷丹" +
                    "蓉眉君琴蕊薇菁梦" +
                    "岚苑筠柔竹霭凝晓" +
                    "欢霄枫芸菲寒欣滢" +
                    "伊亚宜可姬舒影荔" +
                    "枝思丽秀飘育馥琦" +
                    "晶妍茜秋珊莎锦黛" +
                    "青倩婷宁蓓纨苑婕" +
                    "馨瑗琰韵融园艺咏" +
                    "卿聪澜纯毓悦昭冰" +
                    "爽琬茗羽希婷倩睿" +
                    "瑜嘉君盈男萱雨乐" +
                    "欣悦雯晨珺月雪秀" +
                    "晓然冰新淑玟萌凝" +
                    "文展露静智丹宁颖" +
                    "平佳玲彤芸莉璐云" +
                    "聆芝娟超香英菲涓"
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }

        MainScope().launch {
            if (addressData.size == 0) {
                for (i in 1..100000) {
                    addressData.add(Address(name = getRandomString((2..4).random())))
                    delay(20)
                }
            }
        }
    }
}

