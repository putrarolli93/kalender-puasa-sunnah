package com.icaali.kalenderpuasasunnah.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.icaali.kalenderpuasasunnah.R
import com.icaali.kalenderpuasasunnah.model.TanggalModel
import kotlinx.android.synthetic.main.layout_item_legend.view.*

class LegendAdapter(val listener: OnLegendedListener) : RecyclerView.Adapter<LegendAdapter.LegendHolder>() {

    private var tanggalModel: TanggalModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendHolder {
        return LegendHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_legend, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return tanggalModel?.puasaCode?.count() ?: 0
    }

    override fun onBindViewHolder(holder: LegendHolder, position: Int) {
        this.tanggalModel?.let {
            holder.bind(it.puasaCode[position].code, listener)
        }
    }

    fun updateMonthLegend(data: TanggalModel) {
        this.tanggalModel = data
        notifyDataSetChanged()
    }

    class LegendHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(code: Int, listener: OnLegendedListener) {
            view.apply {
                when(code) {
                    1 -> {
                        vcolorLegend.background = getDrawable(this.context, R.drawable.bg_senin_kamis)
                        tvTitle.text = "Puasa Senin Kamis"
                        tvDesc.text = "Puasa yang dilaksanakan setiap senin & kamis"
                    }
                    2 -> {
                        vcolorLegend.background = getDrawable(this.context, R.drawable.bg_ayamul_bidh)
                        tvTitle.text = "Puasa Ayyamul Bidh"
                        tvDesc.text = "Puasa 3 hari setiap pertengahan Bulan Hijriah"
                    }
                    3 -> {
                        vcolorLegend.background = getDrawable(this.context, R.drawable.bg_ramadhan)
                        tvTitle.text = "Puasa Ramadhan"
                        tvDesc.text = "Puasa Wajib umat islam pada bulan Ramadhan"
                    }
                    4 -> {
                        vcolorLegend.background = getDrawable(this.context, R.drawable.bg_arafah)
                        tvTitle.text = "Puasa Arafah"
                        tvDesc.text = "Puasa Sunnah 9 Dzulhijjah(Bagi yang tidak Haji)"
                    }
                    5 -> {
                        vcolorLegend.background = getDrawable(this.context, R.drawable.bg_asyura)
                        tvTitle.text = "Puasa Asyura"
                        tvDesc.text = "Puasa Sunnah 1 Muharram & 1 hari sebelum/sesudah"
                    }
                    8 -> {
                        vcolorLegend.background = getDrawable(this.context, R.drawable.bg_haram_puasa)
                        tvTitle.text = "Haram Berpuasa/Hari Tasyrik"
                        tvDesc.text = "Idul Fitri, Idul adha & Hari Tasyrik"
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