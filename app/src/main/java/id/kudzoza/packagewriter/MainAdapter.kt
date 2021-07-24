package id.kudzoza.packagewriter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.kudzoza.packagewriter.databinding.ItemBinding

/**
 * Created by Kudzoza
 * on 24/07/2021
 **/

class MainAdapter : RecyclerView.Adapter<MainAdapter.VH>() {

    val items = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        ItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val data = items[position]
        holder.binding.apply {
            text.text = data
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root)
}