package com.example.cs501clockin.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedLocationDaoInstrumentedTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insert_returnsGeneratedId() = runBlocking {
        val dao = db.savedLocationDao()
        val id = dao.insert(
            SavedLocationEntity(
                label = "Library",
                latitude = 42.0,
                longitude = -71.0,
                suggestedTag = "School",
                radiusMeters = 150
            )
        )
        assertTrue(id > 0L)
        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("Library", all[0].label)
        assertEquals(id, all[0].id)
    }

    @Test
    fun observeAll_ordersByLabelAscending() = runBlocking {
        val dao = db.savedLocationDao()
        dao.insert(
            SavedLocationEntity(label = "Zoo", latitude = 0.0, longitude = 0.0, suggestedTag = "Personal Care")
        )
        dao.insert(
            SavedLocationEntity(label = "Apple Store", latitude = 1.0, longitude = 1.0, suggestedTag = "Errands")
        )
        val labels = dao.observeAll().first().map { it.label }
        assertEquals(listOf("Apple Store", "Zoo"), labels)
    }

    @Test
    fun deleteById_removesRow() = runBlocking {
        val dao = db.savedLocationDao()
        val id = dao.insert(
            SavedLocationEntity(label = "Gym", latitude = 2.0, longitude = 2.0, suggestedTag = "Training")
        )
        dao.deleteById(id)
        assertEquals(0, dao.observeAll().first().size)
    }
}
