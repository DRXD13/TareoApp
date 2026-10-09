package com.dangeloretis.tareoapp.data.local.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TareoDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate2To3() {
        var db = helper.createDatabase("tareo_database", 2)
        // db is at version 2
        
        db.close()

        // Re-open the database with version 3 and provide MIGRATION_2_3 as the migration process.
        db = helper.runMigrationsAndValidate("tareo_database", 3, true, TareoDatabase.MIGRATION_2_3)
        
        val cursor = db.query("SELECT * FROM sites WHERE id = 'demo_site_1'")
        assert(cursor.moveToFirst())
        assert(cursor.getString(cursor.getColumnIndex("name")) == "Sede Central Demo")
        cursor.close()
    }
}
