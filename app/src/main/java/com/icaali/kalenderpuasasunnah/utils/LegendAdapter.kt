package com.icaali.kalenderpuasasunnah.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.icaali.kalenderpuasasunnah.R
import com.icaali.kalenderpuasasunnah.TanggalModel
import com.icaali.kalenderpuasasunnah.databinding.LayoutItemLegendBinding

class LegendAdapter(
    private val listener: OnLegendedListener
) : RecyclerView.Adapter<LegendAdapter.LegendHolder>() {

    private var tanggalModel: TanggalModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendHolder {
        val binding = LayoutItemLegendBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LegendHolder(binding)
    }

    override fun getItemCount(): Int {
        return tanggalModel?.puasa_code?.count() ?: 0
    }

    override fun onBindViewHolder(holder: LegendHolder, position: Int) {
        tanggalModel?.let {
            holder.bind(it.puasa_code[position].code, listener)
        }
    }

    fun updateMonthLegend(data: TanggalModel) {
        tanggalModel = data
        notifyDataSetChanged()
    }

    class LegendHolder(
        private val binding: LayoutItemLegendBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(code: Int, listener: OnLegendedListener) {

            with(binding) {

                when (code) {

                    1 -> {
                        vcolorLegend.background =
                            AppCompatResources.getDrawable(itemView.context, R.drawable.bg_senin_kamis)
                        tvTitle.text = itemView.context.getString(R.string.puasa_senin_kamis_title)
                        tvDesc.text = itemView.context.getString(R.string.puasa_senin_kamis_desc)
                    }

                    2 -> {
                        vcolorLegend.background =
                            AppCompatResources.getDrawable(itemView.context, R.drawable.bg_ayamul_bidh)
                        tvTitle.text = itemView.context.getString(R.string.puasa_ayamul_bidh_title)
                        tvDesc.text = itemView.context.getString(R.string.puasa_ayamul_bidh_desc)
                    }

                    3 -> {
                        vcolorLegend.background =
                            AppCompatResources.getDrawable(itemView.context, R.drawable.bg_ramadhan)
                        tvTitle.text = itemView.context.getString(R.string.puasa_ramadhan_title)
                        tvDesc.text = itemView.context.getString(R.string.puasa_ramadhan_desc)
                    }

                    4 -> {
                        vcolorLegend.background =
                            AppCompatResources.getDrawable(itemView.context, R.drawable.bg_arafah)
                        tvTitle.text = itemView.context.getString(R.string.puasa_arafah_title)
                        tvDesc.text = itemView.context.getString(R.string.puasa_arafah_desc)
                    }

                    5 -> {
                        vcolorLegend.background =
                            AppCompatResources.getDrawable(itemView.context, R.drawable.bg_asyura)
                        tvTitle.text = itemView.context.getString(R.string.puasa_tasua_asyura_title)
                        tvDesc.text = itemView.context.getString(R.string.puasa_tasua_asyura_desc)
                    }
                    6 -> {
                        vcolorLegend.background =
                            AppCompatResources.getDrawable(itemView.context, R.drawable.bg_syawal)
                        tvTitle.text = itemView.context.getString(R.string.puasa_syawal_title)
                        tvDesc.text = itemView.context.getString(R.string.puasa_syawal_desc)
                    }

                    99 -> {
                        vcolorLegend.background =
                            AppCompatResources.getDrawable(itemView.context, R.drawable.bg_haram_puasa)
                        tvTitle.text = itemView.context.getString(R.string.haram_puasa_title)
                        tvDesc.text = itemView.context.getString(R.string.haram_puasa_desc)
                    }
                }

                btnShow.setOnClickListener {
                    listener.onLegendClick(code)
                }
            }
        }
    }

    interface OnLegendedListener {
        fun onLegendClick(code: Int)
    }
}