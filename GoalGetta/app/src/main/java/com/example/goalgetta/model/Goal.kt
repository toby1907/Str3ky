package com.example.goalgetta.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Time
import java.util.*
@Entity
data class Goal(@PrimaryKey(autoGenerate = true) val goalId: Int = 0,
                @ColumnInfo(name = "title") val title:String,
                @ColumnInfo(name = "isCompleted") val isCompleted: Boolean = false,
                @ColumnInfo(name = "isPriority") val isPriority: Boolean = false,
                @ColumnInfo(name = "description") val description: String,
                @ColumnInfo(name = "due_date") val dueDate : Long)


/*@Entity(tableName = "todo_table")
data class Todo(@PrimaryKey(autoGenerate = true) val id : Int = 0,
                @ColumnInfo(name = "title") val title: String,
                @ColumnInfo(name = "details") val details : String,
                @ColumnInfo(name = "done") val done : Boolean),
                 @ColumnInfo(name = "isCompleted")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "isPriority")
    val isPriority: Boolean = false,

    @ColumnInfo(name = "dueDateMillis")
    val dueDateMillis: Long


    @Dao
interface TodoDao {

    @Query("SELECT * from todo_table order by done desc")
    fun getAllTodos() : LiveData<List<Todo>>

    @Insert
    suspend fun insert(todo : Todo)

    @Update
    suspend fun update(todo: Todo)

    @Query("DELETE FROM todo_table")
    fun deleteAll()
}


class TodoRepository(private val todoDao: TodoDao) {

    val todos: LiveData<List<Todo>> = todoDao.getAllTodos()

    @WorkerThread
    suspend fun insert(todo : Todo){
        todoDao.insert(todo)
    }

    @WorkerThread
    suspend fun update(todo: Todo){
        todoDao.update(todo)
    }
}*/

