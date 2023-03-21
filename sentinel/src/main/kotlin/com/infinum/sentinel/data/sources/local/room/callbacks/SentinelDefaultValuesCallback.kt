package com.infinum.sentinel.data.sources.local.room.callbacks

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

internal class SentinelDefaultValuesCallback : RoomDatabase.Callback() {

    companion object {
        private val DEFAULT_TRIGGERS = """
        INSERT INTO "triggers" ("id","type","enabled","editable") VALUES (0,'MANUAL',1,0),
         (1,'SHAKE',1,1),
         (2,'PROXIMITY',1,1),
         (3,'FOREGROUND',1,1),
         (4,'USB_CONNECTED',1,1),
         (5,'AIRPLANE_MODE_ON',1,1);
        """.trimIndent()

        private val DEFAULT_FORMATS = """
        INSERT INTO "formats" ("id","type","selected") VALUES (0,'PLAIN',1),
         (1,'MARKDOWN',0),
         (2,'JSON',0),
         (3,'XML',0),
         (4,'HTML',0);
        """.trimIndent()

        private val DEFAULT_BUNDLE_MONITOR = """
        INSERT INTO "bundle_monitor" 
        ("id","limit","notify",
        "activity_intent_extras","activity_saved_state",
        "fragment_arguments","fragment_saved_state") 
        VALUES (0,500,0,1,1,1,1);
        """.trimIndent()

        private val DEFAULT_CRASH_MONITOR = """
        INSERT INTO "crash_monitor" 
        ("id","notify_exceptions","notify_anrs","include_all_data") 
        VALUES (0,0,0,0);
        """.trimIndent()

        private val DEFAULT_CERTIFICATE_MONITOR = """
        INSERT INTO "certificate_monitor" 
        ("id","run_on_start","run_in_background","notify_invalid_now",
        "notify_to_expire","expire_in_amount","expire_in_unit") 
        VALUES (0,0,0,0,0,0,'DAYS');
        """.trimIndent()
    }

    override fun onCreate(db: SupportSQLiteDatabase) {
        if (db.isOpen) {
            db.beginTransaction()
            db.execSQL(DEFAULT_TRIGGERS)
            db.execSQL(DEFAULT_FORMATS)
            db.execSQL(DEFAULT_BUNDLE_MONITOR)
            db.execSQL(DEFAULT_CRASH_MONITOR)
            db.execSQL(DEFAULT_CERTIFICATE_MONITOR)
            db.setTransactionSuccessful()
            db.endTransaction()
        }
    }
}
