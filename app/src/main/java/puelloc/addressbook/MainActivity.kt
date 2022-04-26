package puelloc.addressbook

import android.Manifest
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import puelloc.addressbook.data.AddressDatabase
import puelloc.addressbook.databinding.ActivityMainBinding

const val CONTACT_FRAGMENT_TAG = "contact dialog"

class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
                .setWhitelistedRestrictedPermissions(setOf(Manifest.permission.READ_CALL_LOG))
        }

        val addressDB = AddressDatabase.getDatabase(applicationContext)
        val addressDao = addressDB.addressDao()
        val viewModel = AddressListViewModel(addressDao)

        val adapter = AddressesAdapter()
        binding!!.addressesList.adapter = adapter

        viewModel.filteredAddresses.observe(this) { items ->
            adapter.submitList(items)
        }

        val searchItem = binding!!.topAppBar.menu.findItem(R.id.app_bar_search)
        val search = searchItem.actionView as SearchView

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterString.value = query
                }
                search.clearFocus()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterString.value = query
                }
                return true
            }
        })

        binding!!.newContactButton.setOnClickListener {
            search.clearFocus()
            val dialog = ContactDialog(viewModel)
            val transaction = supportFragmentManager.beginTransaction()
            transaction
                .add(android.R.id.content, dialog)
                .addToBackStack(CONTACT_FRAGMENT_TAG)
                .commit()
        }

        FastScrollerBuilder(binding!!.addressesList).setPadding(0, 16, 0, 64).useMd2Style().build()
    }

    override fun onBackPressed() {
        val searchItem = binding!!.topAppBar.menu.findItem(R.id.app_bar_search)
        if (searchItem.isActionViewExpanded && supportFragmentManager.backStackEntryCount == 0) {
            searchItem.collapseActionView()
        } else {
            super.onBackPressed()
        }
    }
}

