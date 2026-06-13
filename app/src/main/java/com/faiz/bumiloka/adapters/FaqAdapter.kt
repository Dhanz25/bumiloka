package com.faiz.bumiloka.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.faiz.bumiloka.databinding.ItemFaqBinding
import com.faiz.bumiloka.model.Faq

class FaqAdapter(private val faqList: List<Faq>) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    class FaqViewHolder(val binding: ItemFaqBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val faq = faqList[position]
        holder.binding.apply {
            tvQuestion.text = faq.question
            tvAnswer.text = faq.answer

            // Update visibility based on expanded state
            layoutAnswer.visibility = if (faq.isExpanded) View.VISIBLE else View.GONE
            ivExpand.rotation = if (faq.isExpanded) 270f else 90f

            cardFaq.setOnClickListener {
                faq.isExpanded = !faq.isExpanded
                
                // Simple animation or just notify
                if (faq.isExpanded) {
                    layoutAnswer.visibility = View.VISIBLE
                    ivExpand.animate().rotation(270f).setDuration(300).start()
                } else {
                    layoutAnswer.visibility = View.GONE
                    ivExpand.animate().rotation(90f).setDuration(300).start()
                }
                
                // Optional: To close other items when one is opened, 
                // you would need more complex logic here.
            }
        }
    }

    override fun getItemCount(): Int = faqList.size
}
