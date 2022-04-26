package puelloc.addressbook

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import puelloc.addressbook.data.Address
import puelloc.addressbook.databinding.ContactItemBinding
import java.text.SimpleDateFormat
import java.util.*

typealias ContactItem = Pair<String, String>

const val TAG_PHONE = "phone"
const val TAG_EMAIL = "email"
const val TAG_RUBY = "ruby"
const val TAG_CALL_IN = "callIn"
const val TAG_CALL_OUT = "callOut"
const val TAG_CALL_MISSED = "callMissed"
const val TAG_CALL_REJECTED = "callRejected"
const val TAG_CALL = "call"

fun Address.getContactItems(context: Context): List<ContactItem> {
    val list = ArrayList<ContactItem>()

    if (phone != null && phone != "") {
        list.add(TAG_PHONE to phone)
    }
    if (email != null && email != "") {
        list.add(TAG_EMAIL to email)
    }
    list.add(TAG_RUBY to ruby)
    val checkPermissionResult = context.checkSelfPermission(Manifest.permission.READ_CALL_LOG)
    if (checkPermissionResult == PackageManager.PERMISSION_GRANTED) {
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE
        )
        val callLogCursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC",
            null
        )!!

        while (callLogCursor.moveToNext()) {
            val number =
                callLogCursor.getString(callLogCursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            val type =
                callLogCursor.getString(callLogCursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                    .toInt()
            val date = Date(
                callLogCursor.getString(callLogCursor.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    .toLong()
            )
            val format = SimpleDateFormat.getDateTimeInstance()
            if (number == phone && number != "") {
                list.add(
                    when (type) {
                        CallLog.Calls.OUTGOING_TYPE -> TAG_CALL_OUT
                        CallLog.Calls.INCOMING_TYPE -> TAG_CALL_IN
                        CallLog.Calls.MISSED_TYPE -> TAG_CALL_MISSED
                        CallLog.Calls.REJECTED_TYPE -> TAG_CALL_REJECTED
                        else -> TAG_CALL
                    } to format.format(date)
                )
            }
        }

        callLogCursor.close()
    }
    return list
}

val TAG_MAP: Map<String, Pair<Int, Int>> = mapOf(
    TAG_PHONE to (R.string.phone to R.drawable.ic_baseline_phone_24),
    TAG_EMAIL to (R.string.email to R.drawable.ic_baseline_email_24),
    TAG_CALL_IN to (R.string.call_in to R.drawable.ic_baseline_call_received_24),
    TAG_CALL_OUT to (R.string.call_out to R.drawable.ic_baseline_call_made_24),
    TAG_CALL_MISSED to (R.string.call_missed to R.drawable.ic_baseline_call_missed_24),
    TAG_CALL_REJECTED to (R.string.call_rejected to R.drawable.ic_baseline_call_missed_24),
    TAG_CALL to (R.string.call to R.drawable.ic_baseline_phone_24),
)

class ContactItemsAdapter :
    ListAdapter<ContactItem, ContactItemsAdapter.ContactItemViewHolder>(DiffCallback) {
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ContactItem>() {
            override fun areItemsTheSame(oldItem: ContactItem, newItem: ContactItem): Boolean {
                return oldItem.first == newItem.first
            }

            override fun areContentsTheSame(oldItem: ContactItem, newItem: ContactItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ContactItemViewHolder(private val binding: ContactItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactItem) {
            binding.itemValue.text = item.second
            if (TAG_MAP.containsKey(item.first)) {
                val (strId, drawableId) = TAG_MAP[item.first]!!
                binding.itemTag.setText(strId)
                binding.itemIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        binding.root.context,
                        drawableId
                    )
                )
            } else {
                binding.itemTag.text = item.first
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactItemViewHolder {
        return ContactItemViewHolder(
            ContactItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}