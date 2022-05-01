package puelloc.addressbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import puelloc.addressbook.data.AddressDatabase
import puelloc.addressbook.databinding.ActivityMainBinding

const val CONTACT_FRAGMENT_TAG = "contact dialog"
var notUseFullScreenDialog = false

class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val originFABBottomMargin =
            (binding!!.newContactButton.layoutParams as MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding!!.root) { v: View, insets: WindowInsetsCompat ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val layout = v.layoutParams as MarginLayoutParams
            layout.bottomMargin = imeHeight
            v.layoutParams = layout
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding!!.newContactButton) { v: View, insets: WindowInsetsCompat ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val barHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            if (!imeVisible) {
                val layout = v.layoutParams as MarginLayoutParams
                layout.bottomMargin = barHeight + originFABBottomMargin
                v.layoutParams = layout
            } else {
                val layout = v.layoutParams as MarginLayoutParams
                layout.bottomMargin = originFABBottomMargin
                v.layoutParams = layout
            }
            WindowInsetsCompat.CONSUMED
        }

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
            if (notUseFullScreenDialog) {
                dialog.show(supportFragmentManager, CONTACT_FRAGMENT_TAG)
            } else {
                val transaction = supportFragmentManager.beginTransaction()
                transaction
                    .add(android.R.id.content, dialog)
                    .addToBackStack(CONTACT_FRAGMENT_TAG)
                    .commit()
            }
        }

        FastScrollerBuilder(binding!!.addressesList).setPadding(0, 16, 0, 64).useMd2Style().build()

        binding!!.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_debug -> {
                    val intent = Intent(applicationContext, InfoActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        Utils.requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
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

