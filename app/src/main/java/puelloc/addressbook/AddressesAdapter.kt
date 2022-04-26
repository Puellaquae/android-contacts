package puelloc.addressbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.zhanghai.android.fastscroll.PopupTextProvider
import puelloc.addressbook.data.Address
import puelloc.addressbook.databinding.AddressItemBinding

const val ADDRESS_ID_MESSAGE = "puelloc.addressbook@ADDRESS_ID_MESSAGE"

class AddressesAdapter :
    ListAdapter<Address, AddressesAdapter.AddressItemViewHolder>(DiffCallback), PopupTextProvider {
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Address>() {
            override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    }

    class AddressItemViewHolder(val binding: AddressItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(address: Address) {
            binding.name.text = address.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressItemViewHolder {
        return AddressItemViewHolder(
            AddressItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AddressItemViewHolder, position: Int) {
        val address = getItem(position)
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, ContactDetailActivity::class.java).apply {
                putExtra(ADDRESS_ID_MESSAGE, address.id)
            }
            it.context.startActivity(intent)
        }
        holder.bind(address)
    }

    override fun getPopupText(position: Int): String = getItem(position).ruby[0].uppercase()
}