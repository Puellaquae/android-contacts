package puelloc.addressbook.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM address ORDER BY ruby")
    fun getAddresses(): Flow<List<Address>>

    @Query("SELECT * FROM address ORDER BY ruby LIMIT 1 OFFSET :index ")
    fun getAddress(index: Int): Flow<Address>

    @Query("SELECT * FROM address WHERE name LIKE :query OR phone LIKE :query OR email LIKE :query OR ruby LIKE :query ORDER BY ruby")
    fun search(query: String): Flow<List<Address>>

    @Query("SELECT COUNT(*) FROM address")
    fun getCount(): Int

    @Insert
    fun insert(address: Address)

    @Query("SELECT * FROM address WHERE id == :id")
    fun getAddressById(id: Int): Flow<Address>

    @Delete
    fun delete(address: Address)

    @Update
    fun update(address: Address)
}