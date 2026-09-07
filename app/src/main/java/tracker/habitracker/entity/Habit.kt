package tracker.habitracker.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val name: String,
    val progressRow: Int = 0,
    @TypeConverters(Converters::class)
    val history: List<Boolean> = List(7) { false }
)

class Converters {
    @TypeConverter
    fun fromString(value: String): List<Boolean> {
        return value.split(",").map { it == "1" }
    }

    @TypeConverter
    fun fromList(list: List<Boolean>): String {
        return list.joinToString(",") { if (it) "1" else "0" }
    }
}