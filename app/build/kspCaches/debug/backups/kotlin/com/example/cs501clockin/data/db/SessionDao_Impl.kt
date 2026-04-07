package com.example.cs501clockin.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SessionDao_Impl(
  __db: RoomDatabase,
) : SessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSessionEntity: EntityInsertAdapter<SessionEntity>

  private val __deleteAdapterOfSessionEntity: EntityDeleteOrUpdateAdapter<SessionEntity>

  private val __updateAdapterOfSessionEntity: EntityDeleteOrUpdateAdapter<SessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSessionEntity = object : EntityInsertAdapter<SessionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `sessions` (`id`,`tag`,`startTimeMillis`,`endTimeMillis`,`notes`,`edited`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SessionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.tag)
        statement.bindLong(3, entity.startTimeMillis)
        val _tmpEndTimeMillis: Long? = entity.endTimeMillis
        if (_tmpEndTimeMillis == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndTimeMillis)
        }
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpNotes)
        }
        val _tmp: Int = if (entity.edited) 1 else 0
        statement.bindLong(6, _tmp.toLong())
      }
    }
    this.__deleteAdapterOfSessionEntity = object : EntityDeleteOrUpdateAdapter<SessionEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `sessions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SessionEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfSessionEntity = object : EntityDeleteOrUpdateAdapter<SessionEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `sessions` SET `id` = ?,`tag` = ?,`startTimeMillis` = ?,`endTimeMillis` = ?,`notes` = ?,`edited` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SessionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.tag)
        statement.bindLong(3, entity.startTimeMillis)
        val _tmpEndTimeMillis: Long? = entity.endTimeMillis
        if (_tmpEndTimeMillis == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEndTimeMillis)
        }
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpNotes)
        }
        val _tmp: Int = if (entity.edited) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.id)
      }
    }
  }

  public override suspend fun upsert(entity: SessionEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfSessionEntity.insert(_connection, entity)
  }

  public override suspend fun delete(entity: SessionEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfSessionEntity.handle(_connection, entity)
  }

  public override suspend fun update(entity: SessionEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfSessionEntity.handle(_connection, entity)
  }

  public override fun observeSessions(): Flow<List<SessionEntity>> {
    val _sql: String = "SELECT * FROM sessions ORDER BY startTimeMillis DESC"
    return createFlow(__db, false, arrayOf("sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTag: Int = getColumnIndexOrThrow(_stmt, "tag")
        val _columnIndexOfStartTimeMillis: Int = getColumnIndexOrThrow(_stmt, "startTimeMillis")
        val _columnIndexOfEndTimeMillis: Int = getColumnIndexOrThrow(_stmt, "endTimeMillis")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfEdited: Int = getColumnIndexOrThrow(_stmt, "edited")
        val _result: MutableList<SessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTag: String
          _tmpTag = _stmt.getText(_columnIndexOfTag)
          val _tmpStartTimeMillis: Long
          _tmpStartTimeMillis = _stmt.getLong(_columnIndexOfStartTimeMillis)
          val _tmpEndTimeMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndTimeMillis)) {
            _tmpEndTimeMillis = null
          } else {
            _tmpEndTimeMillis = _stmt.getLong(_columnIndexOfEndTimeMillis)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpEdited: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfEdited).toInt()
          _tmpEdited = _tmp != 0
          _item =
              SessionEntity(_tmpId,_tmpTag,_tmpStartTimeMillis,_tmpEndTimeMillis,_tmpNotes,_tmpEdited)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeSession(id: Long): Flow<SessionEntity?> {
    val _sql: String = "SELECT * FROM sessions WHERE id = ? LIMIT 1"
    return createFlow(__db, false, arrayOf("sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTag: Int = getColumnIndexOrThrow(_stmt, "tag")
        val _columnIndexOfStartTimeMillis: Int = getColumnIndexOrThrow(_stmt, "startTimeMillis")
        val _columnIndexOfEndTimeMillis: Int = getColumnIndexOrThrow(_stmt, "endTimeMillis")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfEdited: Int = getColumnIndexOrThrow(_stmt, "edited")
        val _result: SessionEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTag: String
          _tmpTag = _stmt.getText(_columnIndexOfTag)
          val _tmpStartTimeMillis: Long
          _tmpStartTimeMillis = _stmt.getLong(_columnIndexOfStartTimeMillis)
          val _tmpEndTimeMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndTimeMillis)) {
            _tmpEndTimeMillis = null
          } else {
            _tmpEndTimeMillis = _stmt.getLong(_columnIndexOfEndTimeMillis)
          }
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpEdited: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfEdited).toInt()
          _tmpEdited = _tmp != 0
          _result =
              SessionEntity(_tmpId,_tmpTag,_tmpStartTimeMillis,_tmpEndTimeMillis,_tmpNotes,_tmpEdited)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: Long) {
    val _sql: String = "DELETE FROM sessions WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
