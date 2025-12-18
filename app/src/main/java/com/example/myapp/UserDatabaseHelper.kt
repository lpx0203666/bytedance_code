package com.example.myapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.ArrayList

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "User.db"
        private const val DATABASE_VERSION = 5
        const val TABLE_NAME = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_NICKNAME = "nickname"
        const val COLUMN_AVATAR_URI = "avatar_uri"
        const val COLUMN_LAST_LOGIN = "last_login"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_NICKNAME + " TEXT,"
                + COLUMN_AVATAR_URI + " TEXT,"
                + COLUMN_LAST_LOGIN + " INTEGER" + ")")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addUser(username: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USERNAME, username)
        values.put(COLUMN_PASSWORD, password)
        values.put(COLUMN_NICKNAME, username) // Default nickname is username
        values.put(COLUMN_LAST_LOGIN, System.currentTimeMillis())
        
        // Use insert with conflict strategy if needed, but simple insert throws or returns -1 on error
        // However, to enforce unique username but allow dup nickname, UNIQUE constraint on username is key.
        // We added UNIQUE to username in onCreate.
        
        try {
            val id = db.insertOrThrow(TABLE_NAME, null, values)
            db.close()
            return id
        } catch (e: Exception) {
            db.close()
            return -1
        }
    }

    fun checkUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val columns = arrayOf(COLUMN_ID)
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password)
        val cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
        val count = cursor.count
        cursor.close()
        db.close()
        return count > 0
    }
    
    fun updateLastLogin(username: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_LAST_LOGIN, System.currentTimeMillis())
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        db.update(TABLE_NAME, values, selection, selectionArgs)
        db.close()
    }
    
    fun isUserExists(username: String): Boolean {
        val db = this.readableDatabase
        val columns = arrayOf(COLUMN_ID)
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
        val count = cursor.count
        cursor.close()
        db.close()
        return count > 0
    }
    
    fun getUserNickname(username: String): String {
        val db = this.readableDatabase
        val columns = arrayOf(COLUMN_NICKNAME)
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
        var nickname = username
        if (cursor.moveToFirst()) {
            val nickIndex = cursor.getColumnIndex(COLUMN_NICKNAME)
            if (nickIndex != -1) {
                val nick = cursor.getString(nickIndex)
                if (nick != null && nick.isNotEmpty()) {
                    nickname = nick
                }
            }
        }
        cursor.close()
        db.close()
        return nickname
    }

    fun getUserAvatar(username: String): String? {
        val db = this.readableDatabase
        val columns = arrayOf(COLUMN_AVATAR_URI)
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null)
        var uri: String? = null
        if (cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(COLUMN_AVATAR_URI)
            if (idx != -1) {
                uri = cursor.getString(idx)
            }
        }
        cursor.close()
        db.close()
        return uri
    }
    
    // Simple data class for User Info
    data class UserInfo(val username: String, val nickname: String, val avatarUri: String?, val lastLogin: Long)

    fun getAllUsers(): List<UserInfo> {
        val userList = ArrayList<UserInfo>()
        val selectQuery = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_LAST_LOGIN DESC"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val usernameIdx = cursor.getColumnIndex(COLUMN_USERNAME)
                val nicknameIdx = cursor.getColumnIndex(COLUMN_NICKNAME)
                val avatarIdx = cursor.getColumnIndex(COLUMN_AVATAR_URI)
                val lastLoginIdx = cursor.getColumnIndex(COLUMN_LAST_LOGIN)

                if (usernameIdx != -1) {
                    val username = cursor.getString(usernameIdx)
                    val nickname = if (nicknameIdx != -1) cursor.getString(nicknameIdx) else username
                    val avatarUri = if (avatarIdx != -1) cursor.getString(avatarIdx) else null
                    val lastLogin = if (lastLoginIdx != -1) cursor.getLong(lastLoginIdx) else 0L
                    
                    userList.add(UserInfo(username, nickname, avatarUri, lastLogin))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return userList
    }
    
    fun updateUserNickname(username: String, newNickname: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NICKNAME, newNickname)
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val count = db.update(TABLE_NAME, values, selection, selectionArgs)
        db.close()
        return count > 0
    }
    
    fun updateUserPassword(username: String, newPassword: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_PASSWORD, newPassword)
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val count = db.update(TABLE_NAME, values, selection, selectionArgs)
        db.close()
        return count > 0
    }

    fun updateUserAvatar(username: String, avatarUri: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_AVATAR_URI, avatarUri)
        val selection = "$COLUMN_USERNAME = ?"
        val selectionArgs = arrayOf(username)
        val count = db.update(TABLE_NAME, values, selection, selectionArgs)
        db.close()
        return count > 0
    }
}
