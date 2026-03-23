package perozzi.gib.data.local

import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DayEntryEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class GibDatabase : RoomDatabase() {
    abstract fun dayEntryDao(): DayEntryDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS day_entries_new (
                dateEpochDay INTEGER NOT NULL PRIMARY KEY,
                breakfastParts TEXT NOT NULL,
                lunchParts TEXT NOT NULL,
                dinnerParts TEXT NOT NULL,
                postDinnerParts TEXT NOT NULL,
                alcoholDrinks INTEGER NOT NULL,
                exerciseLevel INTEGER NOT NULL,
                weight REAL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO day_entries_new (
                dateEpochDay,
                breakfastParts,
                lunchParts,
                dinnerParts,
                postDinnerParts,
                alcoholDrinks,
                exerciseLevel,
                weight
            )
            SELECT
                dateEpochDay,
                breakfastParts,
                lunchParts,
                dinnerParts,
                postDinnerParts,
                CASE WHEN drankAlcohol = 1 THEN 1 ELSE 0 END,
                exerciseLevel,
                weight
            FROM day_entries
            """.trimIndent()
        )
        db.execSQL("DROP TABLE day_entries")
        db.execSQL("ALTER TABLE day_entries_new RENAME TO day_entries")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS day_entries_new (
                dateEpochDay INTEGER NOT NULL PRIMARY KEY,
                breakfastParts TEXT NOT NULL,
                lunchParts TEXT NOT NULL,
                dinnerParts TEXT NOT NULL,
                postDinnerParts TEXT NOT NULL,
                exerciseLevel INTEGER NOT NULL,
                weight REAL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO day_entries_new (
                dateEpochDay,
                breakfastParts,
                lunchParts,
                dinnerParts,
                postDinnerParts,
                exerciseLevel,
                weight
            )
            SELECT
                dateEpochDay,
                breakfastParts,
                lunchParts,
                dinnerParts,
                postDinnerParts,
                exerciseLevel,
                weight
            FROM day_entries
            """.trimIndent()
        )
        db.execSQL("DROP TABLE day_entries")
        db.execSQL("ALTER TABLE day_entries_new RENAME TO day_entries")
    }
}
