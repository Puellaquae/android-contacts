package puelloc.addressbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.WindowCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContactDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_detail)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val addressId = intent.getIntExtra(ADDRESS_ID_MESSAGE, -1)
        val position = intent.getIntExtra(ADDRESS_POSITION_MESSAGE, -1)
        if (addressId == -1 || position == -1) {
            Toast.makeText(applicationContext, "Error Intent Load", Toast.LENGTH_SHORT).show()
            finish()
        }
        val addressDB = AddressDB()
        val address = addressDB.getById(addressId)

        val appBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        appBar.title = address.name
        appBar.setNavigationOnClickListener {
            finish()
        }
        appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_delete -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.delete_contact)
                        .setMessage(getString(R.string.delete_contact_msg, address.name))
                        .setNeutralButton(R.string.cancel) { _, _ ->
                            // Do Nothing
                        }.setPositiveButton(R.string.ok) { _, _ ->
                            addressDB.delete(address, position)
                            finish()
                        }.create().show()
                    true
                }
                else -> false
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.edit_contact_button)
        fab.setOnClickListener {
            val dialog = ContactDialog(addressDB, true, position, address)
            val transaction = supportFragmentManager.beginTransaction()
            transaction
                .add(android.R.id.content, dialog)
                .addToBackStack("contact dialog")
                .commit()
        }
    }
}