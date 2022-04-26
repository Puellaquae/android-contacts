package puelloc.addressbook

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import puelloc.addressbook.data.Address
import puelloc.addressbook.databinding.ContactDialogBinding

class ContactDialog(
    private val addressVM: AddressListViewModel,
    private val isChange: Boolean = false,
    private val oldAddress: Address? = null
) :
    DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val newContact = inflater.inflate(R.layout.contact_fullscreen, container, false)
        val appBar = newContact.findViewById<MaterialToolbar>(R.id.topAppBar)
        val name = newContact.findViewById<TextInputEditText>(R.id.name_input)
        val phone = newContact.findViewById<TextInputEditText>(R.id.phone_input)
        val email = newContact.findViewById<TextInputEditText>(R.id.email_input)
        enterTransition = androidx.transition.Slide(Gravity.END)
        exitTransition = androidx.transition.Slide(Gravity.END)
        appBar.title = getString(
            if (isChange) {
                R.string.modify_contact
            } else {
                R.string.new_contact
            }
        )
        if (isChange) {
            name.setText(oldAddress!!.name)
            phone.setText(oldAddress.phone)
            email.setText(oldAddress.email)
        }
        appBar.setNavigationOnClickListener {
            closeFullscreenDialog()
        }
        appBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_save -> {
                    saveChange(name.text.toString(), phone.text.toString(), email.text.toString())
                    closeFullscreenDialog()
                    true
                }
                else -> false
            }
        }
        return newContact
    }

    private fun closeFullscreenDialog() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        parentFragmentManager.popBackStack()
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return activity?.let {
            val newContact = ContactDialogBinding.inflate(requireActivity().layoutInflater)
            val name = newContact.nameInput
            val phone = newContact.phoneInput
            val email = newContact.emailInput
            val dialog = MaterialAlertDialogBuilder(it)
                .setTitle(
                    if (isChange) {
                        R.string.new_contact
                    } else {
                        R.string.modify_contact
                    }
                )
                .setView(newContact.root)
                .setNeutralButton(R.string.cancel) { _, _ ->
                    // Do Nothing
                }.setPositiveButton(R.string.ok) { _, _ ->
                    saveChange(name.text.toString(), phone.text.toString(), email.text.toString())
                }.create()
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun saveChange(name: String, phone: String, email: String) {
        if (name != "") {
            val newAddress = Address(
                name = name,
                phone = phone,
                email = email
            )
            if (isChange) {
                addressVM.update(newAddress.copy(id = oldAddress!!.id))
            } else {
                addressVM.insert(newAddress)
            }
        }
    }
}