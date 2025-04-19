
package app.lovable.contractfinder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.lovable.contractfinder.databinding.ItemContractBinding
import app.lovable.contractfinder.model.ContractRecord

class ContractListAdapter : RecyclerView.Adapter<ContractListAdapter.ContractViewHolder>() {
    
    private var contracts: List<ContractRecord> = emptyList()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder {
        val binding = ItemContractBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContractViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        val contract = contracts[position]
        holder.bind(contract)
    }
    
    override fun getItemCount(): Int = contracts.size
    
    fun updateData(newContracts: List<ContractRecord>) {
        contracts = newContracts
        notifyDataSetChanged()
    }
    
    class ContractViewHolder(private val binding: ItemContractBinding) : 
            RecyclerView.ViewHolder(binding.root) {
        
        fun bind(contract: ContractRecord) {
            binding.apply {
                textContract.text = contract.contract
                textContractAccount.text = contract.contractAccount
                textIbcName.text = contract.ibcName
                textPortfolio.text = contract.portfolio
                textCbOffer.text = contract.cbOffer
                textRebateOffer.text = contract.rebateOffer
                textPwoScheme.text = contract.pwoScheme
            }
        }
    }
}
