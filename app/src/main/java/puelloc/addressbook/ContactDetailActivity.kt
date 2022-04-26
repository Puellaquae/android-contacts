package puelloc.addressbook

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import puelloc.addressbook.data.AddressDatabase
import puelloc.addressbook.databinding.ActivityContactDetailBinding

var showedPermissionInfo = false

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

        // Register the permissions callback, which handles the user's response to the
        // system permissions dialog. Save the return value, an instance of
        // ActivityResultLauncher. You can use either a val, as shown in this snippet,
        // or a lateinit var in your onAttach() or onCreate() method.
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

        val readCallLogPermissionResult =
            applicationContext.checkSelfPermission(Manifest.permission.READ_CALL_LOG)
        if (readCallLogPermissionResult == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            if (!showedPermissionInfo) {
                Toast.makeText(
                    applicationContext,
                    R.string.calllog_permission_info,
                    Toast.LENGTH_LONG
                ).show()
                showedPermissionInfo = true
            }
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                Manifest.permission.READ_CALL_LOG
            )
        }
    }
}