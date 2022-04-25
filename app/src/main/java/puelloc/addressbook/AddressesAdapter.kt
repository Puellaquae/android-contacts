package puelloc.addressbook

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

const val ADDRESS_ID_MESSAGE = "puelloc.addressbook@ADDRESS_ID_MESSAGE"
const val ADDRESS_POSITION_MESSAGE = "puelloc.addressbook@ADDRESS_POSITON_MESSAGE"

class AddressesAdapter(val data: AddressDB) :
    RecyclerView.Adapter<AddressesAdapter.AddressItemViewHolder>() {
    class AddressItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var name = itemView.findViewById<TextView>(R.id.name)
        var address: Address? = null
            set(value) {
                field = value
                if (value != null) {
                    name.text = value.name
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.address_item, parent, false)
        return AddressItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddressItemViewHolder, position: Int) {
        val address = data[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, ContactDetailActivity::class.java).apply {
                putExtra(ADDRESS_ID_MESSAGE, address.id)
                putExtra(ADDRESS_POSITION_MESSAGE, position)
            }
            it.context.startActivity(intent)
        }
        holder.address = address
    }

    override fun getItemCount(): Int {
        return data.size
    }
}