package puelloc.addressbook

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.room.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.debounce
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType

@OptIn(FlowPreview::class)
class AddressDB(context: Context? = null) {
    companion object {
        private var addressDaoSingleton: AddressDao? = null
        private var bindAdapter: AddressesAdapter? = null
    }

    var bindAdapter: AddressesAdapter?
        get() {
            return AddressDB.bindAdapter
        }
        set(value) {
            AddressDB.bindAdapter = value
        }
    private val dao: AddressDao
    private val scope = MainScope()

    init {
        if (addressDaoSingleton == null) {
            val db = Room.databaseBuilder(context!!, AppDatabase::class.java, "address-books")
                .allowMainThreadQueries().build()
            addressDaoSingleton = db.addressDao()
        }
        dao = addressDaoSingleton!!
        diffFlow().debounce(300).onEach { calcDiffAndUpdateUi() }.launchIn(scope)
    }

    private var dataCache: List<Address> = listOf()
    private var cacheStart: Int = 0
    private var cacheEnd: Int = 0

    private var preloadDataCache: List<Address> = listOf()
    private var preloadCacheStart: Int = 0
    private var preloadCacheEnd: Int = 0

    private var preloadJob: Job? = null

    var cacheSize: Int = 1024
    private val cachePreloadBorder: Int = cacheSize / 256

    val addresses: List<Address>
        get() {
            return dao.getAddresses()
        }

    val size: Int get() = dao.getCount()

    private fun loadCache(start: Int, length: Int) {
        dataCache = dao.getAddresses(start, length)
        cacheStart = start
        cacheEnd = cacheStart + dataCache.size
    }

    private fun reloadCache() {
        val start = cacheStart
        val end = minOf(cacheSize + start, dao.getCount())
        dataCache = dao.getAddresses(start, end - start)
        cacheStart = start
        cacheEnd = end
    }

    private fun preloadCache(nextBlock: Boolean = true) {
        val (start, end) = if (nextBlock) {
            val start = cacheEnd
            val end = minOf(dao.getCount(), start + cacheSize)
            start to end
        } else {
            val end = cacheStart
            val start = maxOf(0, end - cacheSize)
            start to end
        }
        preloadDataCache = dao.getAddresses(start, end - start)
        preloadCacheStart = start
        preloadCacheEnd = preloadCacheStart + preloadDataCache.size
    }

    operator fun get(i: Int): Address {
        if (i in cacheStart until cacheEnd) {
            if (preloadJob == null) {
                if (i - cacheStart <= cachePreloadBorder) {
                    preloadJob =
                        scope.launch { withContext(Dispatchers.IO) { preloadCache(false) } }
                } else if (cacheEnd - i <= cachePreloadBorder) {
                    preloadJob = scope.launch { withContext(Dispatchers.IO) { preloadCache() } }
                }
            }
            return dataCache[i - cacheStart]
        } else if (i in preloadCacheStart until preloadCacheEnd) {
            dataCache = preloadDataCache
            cacheStart = preloadCacheStart
            cacheEnd = preloadCacheEnd
            preloadJob = null
            return dataCache[i - cacheStart]
        } else {
            preloadJob?.cancel()
            preloadJob = null
            val start = maxOf(0, i - cacheSize / 2)
            val end = minOf(dao.getCount(), start + cacheSize)
            val length = end - start
            loadCache(start, length)
            return dataCache[i - cacheStart]
        }
    }

    fun getById(id: Int): Address = dao.getAddressById(id)

    private suspend fun calcDiffAndUpdateUi() {
        var result: DiffUtil.DiffResult
        withContext(Dispatchers.IO) {
            val oldData = dataCache
            loadCache(
                cacheStart,
                maxOf(minOf(cacheStart + cacheSize, dao.getCount()), cacheEnd) - cacheStart
            )
            result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldData.size

                override fun getNewListSize(): Int = dataCache.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    oldData[oldItemPosition].id == dataCache[newItemPosition].id

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean =
                    oldData[oldItemPosition].hashCode() == dataCache[newItemPosition].hashCode()
            })
        }
        result.dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                bindAdapter?.notifyItemRangeInserted(position + cacheStart, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                bindAdapter?.notifyItemRangeRemoved(position + cacheStart, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                bindAdapter?.notifyItemMoved(fromPosition + cacheStart, toPosition + cacheStart)
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                bindAdapter?.notifyItemRangeChanged(position + cacheStart, count, payload)
            }
        })
    }

    private var calcDiff: (() -> Unit)? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun diffFlow(): Flow<Unit> = callbackFlow {
        calcDiff = { offer(Unit) }
        awaitClose { calcDiff = null }
    }

    fun add(address: Address) {
        val pos = dao.getPossiblePositionOfRuby(address.ruby)
        dao.add(address)
        reloadCache()
        bindAdapter?.notifyItemInserted(pos)
    }

    fun update(address: Address, position: Int) {
        var pos = dao.getPossiblePositionOfRuby(address.ruby)
        if (pos > position) {
            pos -= 1
        }
        val inPlace = pos == position
        dao.update(address)
        reloadCache()
        if (!inPlace) {
            bindAdapter?.notifyItemMoved(position, pos)
        }
        bindAdapter?.notifyItemChanged(pos)
    }

    fun delete(address: Address, positionHint: Int) {
        dao.delete(address)
        reloadCache()
        bindAdapter?.notifyItemRangeRemoved(positionHint, 1)
    }
}

private fun getRuby(str: String): String {
    val format = HanyuPinyinOutputFormat().apply {
        toneType = HanyuPinyinToneType.WITHOUT_TONE
    }
    val ruby = str.map {
        if (it in (('a'..'z') + ('A'..'Z'))) {
            it.lowercase()
        } else {
            val pinyin = PinyinHelper.toHanyuPinyinStringArray(it, format)[0]
            if (pinyin == "none") {
                ""
            } else {
                pinyin
            }
        }
    }.joinToString("")
    return if (ruby == "") {
        "#${str}"
    } else {
        ruby
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

@Dao
interface AddressDao {
    @Query("SELECT * FROM address ORDER BY ruby")
    fun getAddresses(): List<Address>

    @Query("SELECT * FROM address ORDER BY ruby LIMIT :length OFFSET :start")
    fun getAddresses(start: Int, length: Int): List<Address>

    @Query("SELECT * FROM address ORDER BY ruby LIMIT 1 OFFSET :index ")
    fun getAddress(index: Int): Address

    @Query("SELECT COUNT(*) FROM address")
    fun getCount(): Int

    @Insert
    fun add(address: Address): Long

    @Query("SELECT * FROM address WHERE id == :id")
    fun getAddressById(id: Int): Address

    @Query("SELECT COUNT(*) FROM address WHERE ruby <= :ruby")
    fun getPossiblePositionOfRuby(ruby: String): Int

    @Delete
    fun delete(address: Address)

    @Update
    fun update(address: Address)
}

@Database(entities = [Address::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun addressDao(): AddressDao
}
