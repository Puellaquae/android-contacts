package puelloc.addressbook

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import puelloc.addressbook.data.AddressDatabase
import puelloc.addressbook.databinding.ActivityContactDetailBinding

class ContactDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityContactDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val addressId = intent.getIntExtra(ADDRESS_ID_MESSAGE, -1)
        if (addressId == -1) {
            Toast.makeText(applicationContext, "Error Intent Load", Toast.LENGTH_SHORT).show()
            finish()
        }

        val adapter = ContactItemsAdapter()
        binding.contactItemList.adapter = adapter

        val addressVM = AddressListViewModel(AddressDatabase.getDatabase().addressDao())
        addressVM.retrieveItemById(addressId).observe(this) { address ->
            if (address != null) {
                binding.topAppBar.title = address.name
                adapter.submitList(address.getContactItems(applicationContext))
                binding.topAppBar.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_delete -> {
                            MaterialAlertDialogBuilder(this)
                                .setTitle(R.string.delete_contact)
                                .setMessage(getString(R.string.delete_contact_msg, address.name))
                                .setNeutralButton(R.string.cancel) { _, _ ->
                                    // Do Nothing
                                }.setPositiveButton(R.string.ok) { _, _ ->
                                    addressVM.delete(address)
                                    finish()
                                }.create().show()
                            true
                        }
                        else -> false
                    }
                }
                binding.editContactButton.setOnClickListener {
                    val dialog = ContactDialog(addressVM, true, address)
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction
                        .add(android.R.id.content, dialog)
                        .addToBackStack("contact dialog")
                        .commit()
                }
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        Utils.needPermission(this, Manifest.permission.READ_CALL_LOG, infoResId = R.string.calllog_permission_info)
    }
}