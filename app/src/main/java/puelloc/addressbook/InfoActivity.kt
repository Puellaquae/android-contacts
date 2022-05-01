package puelloc.addressbook

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import puelloc.addressbook.data.Address
import puelloc.addressbook.data.AddressDatabase
import puelloc.addressbook.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val viewModel =
            AddressListViewModel(AddressDatabase.getDatabase(applicationContext).addressDao())

        viewModel.size().observe(this) {
            binding.sizeInfo.text = getString(R.string.database_size, it)
        }

        fun addContacts(size: Int) {
            for (i in 1..size) {
                viewModel.insert(Address(name = Utils.giveMeAChineseName()))
            }
            Toast.makeText(applicationContext, getString(R.string.added_random_items, size), Toast.LENGTH_SHORT).show()
        }

        binding.newItemButton10.setOnClickListener {
            addContacts(10)
        }

        binding.newItemButton100.setOnClickListener {
            addContacts(100)
        }

        binding.newItemButton1000.setOnClickListener {
            addContacts(1000)
        }

        binding.fullscreenSwitch.isChecked = !notUseFullScreenDialog
        binding.fullscreenSwitch.setOnCheckedChangeListener { _, b ->
            notUseFullScreenDialog = !b
        }

        binding.deleteAll.setOnClickListener {
            viewModel.deleteAll()
        }

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }
}