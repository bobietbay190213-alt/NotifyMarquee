package com.notifymarquee.data.database

  import android.content.Context
  import androidx.room.Database
  import androidx.room.Room
  import androidx.room.RoomDatabase
  import androidx.room.migration.Migration
  import androidx.sqlite.db.SupportSQLiteDatabase

  @Database(entities = [NotificationEntity::class], version = 2, exportSchema = false)
  abstract class AppDatabase : RoomDatabase() {
      abstract fun dao(): NotificationDao

      companion object {
          @Volatile private var INSTANCE: AppDatabase? = null

          val MIGRATION_1_2 = object : Migration(1, 2) {
              override fun migrate(db: SupportSQLiteDatabase) {
                  db.execSQL(
                      "CREATE INDEX IF NOT EXISTS index_notifications_timestamp ON notifications(timestamp)"
                  )
              }
          }

          fun get(ctx: Context) = INSTANCE ?: synchronized(this) {
              INSTANCE ?: Room.databaseBuilder(
                  ctx.applicationContext,
                  AppDatabase::class.java,
                  "nm.db"
              )
                  .addMigrations(MIGRATION_1_2)
                  .fallbackToDestructiveMigration()
                  .build()
                  .also { INSTANCE = it }
          }
      }
  }
  