package com.example.goalgetta.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.goalgetta.R
import com.example.goalgetta.databinding.TaskListItemBinding
import com.example.goalgetta.model.Goal
import com.example.goalgetta.ui.TitleView.Companion.DONE
import com.example.goalgetta.ui.TitleView.Companion.NORMAL
import com.example.goalgetta.utils.DateConverter
import com.example.goalgetta.utils.GOAL_ID

class CoursesAdapter(): ListAdapter<Goal, CoursesAdapter.CourseViewHolder>(DiffCallback) {

    class CourseViewHolder(private var binding: TaskListItemBinding,
    private val onCheckBoxClicked: (Goal) -> Unit) : RecyclerView.ViewHolder(binding.root){
        fun bind (goal: Goal){
            binding.taskTitle.text =goal.title
            binding.dueDate.text = DateConverter.convertMillisToString(goal.dueDate)
            binding.priority.setImageResource(if (goal.isPriority) R.drawable.ic_priority_yes else R.drawable.ic_priority_no)

            when {
                goal.isCompleted -> {
                    //Completed
                    binding.checkBox.isChecked = true
                    binding.taskTitle.state = DONE

                }
                else -> {
                    //Active
                    binding.checkBox.isChecked = false
                    binding.taskTitle.state = NORMAL
                }
            }
            binding.checkBox.setOnClickListener {
                val updateGoal = Goal(
                    goalId = goal.goalId,
                      title = goal.title,
                    isCompleted = !goal.isCompleted,
                    isPriority = goal.isPriority,
                    dueDate = goal.dueDate,
                    description = goal.description
                )
                onCheckBoxClicked(updateGoal)
            }

        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem.goalId == newItem.goalId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val inflater = LayoutInflater.from (parent.context)
        val binding = TaskListItemBinding.inflate(inflater, parent,false)
        return CourseViewHolder(binding){
            // implement the onItemChecked
        }
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val goalItem = getItem(position)
        holder.bind(goalItem)
       /* holder.itemView.setOnClickListener {
            val detailIntent = Intent(holder.itemView.context, DetailTaskActivity::class.java)
            detailIntent.putExtra(GOAL_ID, goalItem.goalId)
           holder.itemView.context.startActivity(detailIntent)

        }*/
    }
    /*private var onItemClickListener: ((Goal) -> Unit)? = null
    fun setOnItemClickListener (listener: (Goal)-> Unit) {
        onItemClickListener = listener
    }*/

}