

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.goalgetta.Database.Dao.GoalDao
import com.example.goalgetta.model.Goal


@Database(entities = [Goal::class], version = 1, exportSchema = false)
abstract class GoalDatabase: RoomDatabase() {

    abstract fun goalDao(): GoalDao



    companion object {

        @Volatile
        private var INSTANCE: GoalDatabase? = null

        fun getInstance(context: Context): GoalDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        GoalDatabase::class.java,
                        "goal_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }

    }
}
