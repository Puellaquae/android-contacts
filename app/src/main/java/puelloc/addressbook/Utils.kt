package puelloc.addressbook

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

class Utils {
    companion object {
        private val random by lazy { Random(Calendar.getInstance().time.time) }

        fun giveMeAChineseName(): String {
            val familyNames = ("赵钱孙李周吴郑王" +
                    "冯陈褚卫蒋沈韩杨" +
                    "朱秦尤许何吕施张" +
                    "孔曹严华金魏陶姜" +
                    "戚谢邹喻柏水窦章" +
                    "云苏潘葛奚范彭郎" +
                    "鲁韦昌马苗凤花方" +
                    "俞任袁柳酆鲍史唐" +
                    "费廉岑薛雷贺倪汤" +
                    "滕殷罗毕郝邬安常" +
                    "乐于时傅皮卞齐康" +
                    "伍余元卜顾孟平黄" +
                    "和穆萧尹姚邵湛汪" +
                    "祁毛禹狄米贝明臧" +
                    "计伏成戴谈宋茅庞" +
                    "熊纪舒屈项祝董梁" +
                    "杜阮蓝闵席季麻强" +
                    "贾路娄危江童颜郭" +
                    "梅盛林刁锺徐邱骆" +
                    "高夏蔡田樊胡凌霍" +
                    "虞万支柯昝管卢莫" +
                    "经房裘缪干解应宗" +
                    "丁宣贲邓郁单杭洪" +
                    "包诸左石崔吉钮龚" +
                    "程嵇邢滑裴陆荣翁" +
                    "荀羊於惠甄麹家封" +
                    "芮羿储靳汲邴糜松" +
                    "井段富巫乌焦巴弓" +
                    "牧隗山谷车侯宓蓬" +
                    "全郗班仰秋仲伊宫" +
                    "甯仇栾暴甘钭厉戎" +
                    "祖武符刘景詹束龙" +
                    "叶幸司韶郜黎蓟薄" +
                    "印宿白怀蒲邰从鄂" +
                    "索咸籍赖卓蔺屠蒙" +
                    "池乔阴鬱胥能苍双" +
                    "闻莘党翟谭贡劳逄" +
                    "姬申扶堵冉宰郦雍" +
                    "郤璩桑桂濮牛寿通" +
                    "边扈燕冀郏浦尚农" +
                    "温别庄晏柴瞿阎充" +
                    "慕连茹习宦艾鱼容" +
                    "向古易慎戈廖庾终" +
                    "暨居衡步都耿满弘" +
                    "匡国文寇广禄阙东" +
                    "欧殳沃利蔚越夔隆" +
                    "师巩厍聂晁勾敖融" +
                    "冷訾辛阚那简饶空" +
                    "曾毋沙乜养鞠须丰" +
                    "巢关蒯相查后荆红" +
                    "游竺权逯盖益桓公").windowed(1) + ("万俟司马上官欧阳" +
                    "夏侯诸葛闻人东方" +
                    "赫连皇甫尉迟公羊" +
                    "澹台公冶宗政濮阳" +
                    "淳于单于太叔申屠" +
                    "鲜于闾丘司徒司空" +
                    "亓官司寇仉督子车" +
                    "颛孙端木巫马公西" +
                    "漆雕乐正壤驷公良" +
                    "拓跋夹谷宰父穀梁" +
                    "晋楚闫法汝鄢涂钦" +
                    "段干百里东郭南门" +
                    "呼延归海羊舌微生" +
                    "岳帅缑亢况後有琴" +
                    "梁丘左丘东门西门" +
                    "商牟佘佴伯赏南宫" +
                    "墨哈谯笪年爱阳佟第五言福").windowed(2)

            val names = "力明永健世广志义" +
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

            return familyNames.random(random) + (1..(1..2).random(random)).map { names.random(random) }
                .joinToString("")
        }

        private tailrec fun Context.getActivity(): Activity? = this as? Activity
            ?: (this as? ContextWrapper)?.baseContext?.getActivity()

        private val showedPermissionInfo: HashMap<String, Boolean> = HashMap()

        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher. You can use either a val, as shown in this snippet,
        // or a lateinit var in your onAttach() or onCreate() method.
        lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

        fun needPermission(
            context: Context,
            permission: String,
            infoResId: Int? = null
        ) {
            val activity = context.getActivity() ?: return

            val readCallLogPermissionResult =
                context.checkSelfPermission(permission)
            if (readCallLogPermissionResult == PackageManager.PERMISSION_GRANTED) {
                // You can use the API that requires the permission.
            } else if (infoResId != null && shouldShowRequestPermissionRationale(
                    activity,
                    permission
                )
            ) {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                if (showedPermissionInfo[permission] != true) {
                    Toast.makeText(
                        context,
                        infoResId,
                        Toast.LENGTH_LONG
                    ).show()
                    showedPermissionInfo[permission] = true
                }
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}