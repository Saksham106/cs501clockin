package com.example.cs501clockin.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionDaoInstrumentedTest {

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
    fun upsert_then_observeSession_returnsEntity() = runBlocking {
        val dao = db.sessionDao()
        val entity = SessionEntity(
            id = 100L,
            tag = "Work",
            startTimeMillis = 1L,
            endTimeMillis = 2L,
            notes = "note",
            edited = false
        )
        dao.upsert(entity)
        assertEquals(entity, dao.observeSession(100L).first())
    }

    @Test
    fun observeSessions_ordersByStartTimeMillisDescending() = runBlocking {
        val dao = db.sessionDao()
        dao.upsert(
            SessionEntity(1L, "A", startTimeMillis = 1000L, endTimeMillis = 2000L, notes = null, edited = false)
        )
        dao.upsert(
            SessionEntity(2L, "B", startTimeMillis = 3000L, endTimeMillis = 4000L, notes = null, edited = false)
        )
        val list = dao.observeSessions().first()
        assertEquals(2, list.size)
        assertEquals(3000L, list[0].startTimeMillis)
        assertEquals(1000L, list[1].startTimeMillis)
    }

    @Test
    fun deleteById_removesRow() = runBlocking {
        val dao = db.sessionDao()
        dao.upsert(
            SessionEntity(1L, "A", 0L, 1L, null, false)
        )
        dao.deleteById(1L)
        assertNull(dao.observeSession(1L).first())
    }

    @Test
    fun update_modifiesRow() = runBlocking {
        val dao = db.sessionDao()
        val original = SessionEntity(5L, "School", 10L, 20L, null, edited = false)
        dao.upsert(original)
        dao.update(original.copy(edited = true, notes = "updated"))
        val row = dao.observeSession(5L).first()
        assertEquals(true, row?.edited)
        assertEquals("updated", row?.notes)
    }
}
