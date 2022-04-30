package puelloc.addressbook

import androidx.lifecycle.*
import puelloc.addressbook.data.AddressDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import puelloc.addressbook.data.Address

class AddressListViewModel(private val addressDao: AddressDao) : ViewModel() {
    val allItems: LiveData<List<Address>> = addressDao.getAddresses().asLiveData()

    val filterString = MutableLiveData<String>()

    init {
        filterString.value = ""
    }

    val filteredAddresses = filterString.switchMap {
        search("%${it.replace(' ', '%')}%")
    }

    fun search(query: String): LiveData<List<Address>> {
        return addressDao.search(query).asLiveData()
    }

    fun retrieveItemById(id: Int): LiveData<Address> {
        return addressDao.getAddressById(id).asLiveData()
    }

    fun size(): LiveData<Int> {
        return addressDao.getCount().asLiveData()
    }

    fun update(address: Address) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addressDao.update(address)
            }
        }
    }

    fun insert(address: Address) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addressDao.insert(address)
            }
        }
    }

    fun delete(address: Address) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addressDao.delete(address)
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addressDao.deleteAll()
            }
        }
    }
}