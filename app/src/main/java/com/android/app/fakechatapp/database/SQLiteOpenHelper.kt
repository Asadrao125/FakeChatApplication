package com.android.app.fakechatapp.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.android.app.fakechatapp.models.Call
import com.android.app.fakechatapp.models.Chat
import com.android.app.fakechatapp.models.Status
import com.android.app.fakechatapp.models.User

class MyDatabase(context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private fun createAllTables(db: SQLiteDatabase) {
        val userQuery = ("CREATE TABLE " + TABLE_USERS + " ("
                + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PROFILE_IMAGE + " TEXT,"
                + NAME + " TEXT,"
                + ABOUT_INFO + " TEXT,"
                + LAST_SEEN + " TEXT,"
                + PHONE_NO + " TEXT,"
                + DATE + " TEXT,"
                + IS_VERIFIED + " INTEGER,"
                + IS_ARCHIVE + " INTEGER,"
                + ENCRYPTED_TEXT + " TEXT,"
                + LAST_MESSAGE + " TEXT,"
                + LAST_MSG_TIME + " TEXT)")

        val chatQuery = ("CREATE TABLE " + TABLE_CHAT + " ("
                + MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RECEIVER_NAME + " TEXT,"
                + MESSAGE + " TEXT,"
                + TIME + " TEXT,"
                + SENDER_ID + " INTEGER,"
                + RECEIVER_ID + " INTEGER,"
                + VIEW_TYPE + " INTEGER,"
                + CHAT_DATE + " TEXT,"
                + IMAGE_PATH + " TEXT,"
                + FILE_PATH + " TEXT)")

        val callQuery = ("CREATE TABLE " + TABLE_CALL + " ("
                + CALL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CALL_RECEIVER_ID + " INTEGER,"
                + USER_NAME + " TEXT,"
                + CALL_DIRECTION + " TEXT,"
                + CALL_DATE + " TEXT,"
                + CALL_TIME + " TEXT,"
                + CALL_TYPE + " TEXT,"
                + CALL_DURATION + " TEXT,"
                + CALL_PROFILE_IMAGE + " TEXT)")

        val statusQuery = ("CREATE TABLE " + TABLE_STATUS + " ("
                + STATUS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + STATUS_UPLOADER_ID + " INTEGER,"
                + STATUS_USER_NAME + " TEXT,"
                + STATUS_MESSAGE + " TEXT,"
                + STATUS_DATE + " TEXT,"
                + STATUS_TIME + " TEXT,"
                + IS_SEEN + " INTEGER,"
                + STATUS_UPLOADER_PROFILE + " TEXT,"
                + STATUS_COLOR + " INTEGER,"
                + IS_MUTE + " INTEGER,"
                + STATUS_TYPE + " INTEGER,"
                + IMAGE_PATH + " TEXT)")

        db.execSQL(userQuery)
        db.execSQL(chatQuery)
        db.execSQL(callQuery)
        db.execSQL(statusQuery)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createAllTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CALL")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STATUS")
        onCreate(db)
    }

    /* USER METHODS */

    fun insertUser(user: User): Long {
        var rowId: Long = -1
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(PROFILE_IMAGE, user.profileImage)
        values.put(NAME, user.name)
        values.put(ABOUT_INFO, user.aboutInfo)
        values.put(LAST_SEEN, user.lastSeen)
        values.put(PHONE_NO, user.phoneNo)
        values.put(DATE, user.date)
        values.put(IS_VERIFIED, user.isVerified)
        values.put(IS_ARCHIVE, user.isArchive)
        values.put(ENCRYPTED_TEXT, user.encryptedText)
        values.put(LAST_MESSAGE, user.lastMessage)
        values.put(LAST_MSG_TIME, user.lastMsgTime)
        try {
            rowId = db.insert(TABLE_USERS, null, values)
            println("-- User Inserted RowId : $rowId")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        db.close()
        return rowId
    }

    fun getAllUsers(): ArrayList<User> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS", null)
        val arrDBVideos: ArrayList<User> = ArrayList()
        while (cursor.moveToNext()) {
            val model = User(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getInt(7),
                cursor.getInt(8),
                cursor.getString(9),
                cursor.getString(10),
                cursor.getString(11)
            )
            arrDBVideos.add(model)
        }
        cursor.close()
        return arrDBVideos
    }

    @SuppressLint("Recycle")
    fun getAllUsers(isArchive: Int): ArrayList<User> {
        val db = this.readableDatabase
        val users = ArrayList<User>()
        var temp: User?
        val query: String
        val query1 = "select * from Users"
        val query2 = "SELECT * FROM Users WHERE isArchive = '$isArchive'"
        query = if (isArchive == -1) query1 else query2
        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                temp = User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    cursor.getInt(8),
                    cursor.getString(9),
                    cursor.getString(10),
                    cursor.getString(11)
                )

                users.add(temp)
                temp = null
            } while (cursor.moveToNext())
        }
        return users
    }

    fun getArchiveCount(isArchive: Int): Int {
        var count = 0
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM Users WHERE isArchive = '$isArchive'"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        close()
        return count
    }

    @SuppressLint("Recycle")
    fun getSingleUser(uId: Int): User {
        var temp = User()
        val db = this.readableDatabase
        val query = "select * from Users WHERE userId = '$uId'"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                temp = User(
                    cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getString(5),
                    cursor.getString(6), cursor.getInt(7), cursor.getInt(8),
                    cursor.getString(9), cursor.getString(10), cursor.getString(11)
                )
            } while (cursor.moveToNext())
        }
        return temp
    }

    fun updateUserArchiveStatus(userId: Int, isArchive: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        val where = "userId='$userId'"
        values.put(IS_ARCHIVE, isArchive)
        try {
            val rows = db.update(TABLE_USERS, values, where, null)
            println("-- SQL Rows Updated: $rows")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        db.close()
    }

    fun updateLastMessageAndTime(userId: Int, lastMsg: String, lastMsgTime: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        val where = "userId='$userId'"
        values.put(LAST_MESSAGE, lastMsg)
        values.put(LAST_MSG_TIME, lastMsgTime)
        try {
            val rows: Int = db.update(TABLE_USERS, values, where, null)
            println("-- SQL Rows Updated: $rows")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        db.close()
    }

    fun deleteUser(userId: Int) {
        val db = this.writableDatabase
        val query = "DELETE FROM $TABLE_USERS WHERE userId = '$userId'"
        db.execSQL(query)
        db.close()
    }

    /* CHAT METHODS */

    fun insertChat(chat: Chat): Long {
        var rowId: Long = -1
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(RECEIVER_NAME, chat.receiverName)
        values.put(MESSAGE, chat.message)
        values.put(TIME, chat.time)
        values.put(SENDER_ID, chat.senderId)
        values.put(RECEIVER_ID, chat.receiverId)
        values.put(VIEW_TYPE, chat.viewType)
        values.put(CHAT_DATE, chat.date)
        values.put(IMAGE_PATH, chat.imagePath)
        values.put(FILE_PATH, chat.filePath)
        try {
            rowId = db.insert(TABLE_CHAT, null, values)
            println("-- Chat Inserted RowId : $rowId")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        db.close()
        return rowId
    }

    @SuppressLint("Recycle")
    fun getUserChats(sendId: Int): ArrayList<Chat> {
        val db = this.readableDatabase
        val chats = ArrayList<Chat>()
        var temp: Chat?
        val query = "select * from Chat where senderId = '$sendId'"
        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                temp = Chat(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9)
                )
                chats.add(temp)
                temp = null
            } while (cursor.moveToNext())
        }
        return chats
    }

    fun deleteChat(senderId: Int) {
        val db = this.writableDatabase
        val query = "DELETE FROM Chat WHERE senderId = '$senderId'"
        db.execSQL(query)
        close()
    }

    /* CALL METHODS */

    fun insertCall(call: Call): Long {
        var rowId: Long = -1
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(CALL_RECEIVER_ID, call.callReceiverId)
        values.put(USER_NAME, call.userName)
        values.put(CALL_DIRECTION, call.callDirection)
        values.put(CALL_DATE, call.date)
        values.put(CALL_TIME, call.time)
        values.put(CALL_TYPE, call.callType)
        values.put(CALL_DURATION, call.callDuration)
        values.put(CALL_PROFILE_IMAGE, call.callProfileImage)
        try {
            rowId = db.insert(TABLE_CALL, null, values)
            println("-- Call Inserted RowId : $rowId")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        db.close()
        return rowId
    }

    @SuppressLint("Recycle")
    fun getAllCalls(): ArrayList<Call> {
        val db = this.readableDatabase
        val chats = ArrayList<Call>()
        var temp: Call?

        val query =
            "SELECT c.callId, c.callReceiverId, c.userName, c.callDirection, c.date, c.time, c.callType, c.callDuration, c.callProfileImage " +
                    "FROM Calls c " +
                    "INNER JOIN (SELECT MAX(callId) AS maxCallId FROM Calls GROUP BY callReceiverId) t " +
                    "ON c.callId = t.maxCallId " +
                    "ORDER BY c.callId DESC"

        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                temp = Call(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
                )
                chats.add(temp)
                temp = null
            } while (cursor.moveToNext())
        }
        return chats
    }

    fun getAllCallsByUser(userId: Int): ArrayList<Call> {
        val db = this.readableDatabase
        val chats = ArrayList<Call>()
        var temp: Call?

        val query = "SELECT * FROM Calls WHERE callReceiverId = '$userId' ORDER BY callId DESC"

        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                temp = Call(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
                )
                chats.add(temp)
                temp = null
            } while (cursor.moveToNext())
        }
        return chats
    }

    fun updateCallDuration(duration: String, callId: Long) {
        val db = this.writableDatabase
        val values = ContentValues()
        val where = "callId='$callId'"
        values.put(CALL_DURATION, duration)
        try {
            val rows: Int = db.update(TABLE_CALL, values, where, null)
            println("-- Call Duration Updated RowId : $rows")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        close()
    }

    fun updateCallTime(time: String, callId: Long) {
        val db = this.writableDatabase
        val values = ContentValues()
        val where = "callId='$callId'"
        values.put(CALL_DURATION, time)
        try {
            val rows: Int = db.update(TABLE_CALL, values, where, null)
            println("-- Call Time Updated: $rows")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        close()
    }

    /* STATUS METHODS */

    fun insertStatus(status: Status): Long {
        var rowId: Long = -1
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(STATUS_UPLOADER_ID, status.statusUploaderId)
        values.put(STATUS_USER_NAME, status.userName)
        values.put(STATUS_MESSAGE, status.statusMessage)
        values.put(STATUS_DATE, status.date)
        values.put(STATUS_TIME, status.time)
        values.put(IS_SEEN, status.isSeen)
        values.put(STATUS_UPLOADER_PROFILE, status.statusUploaderProfile)
        values.put(STATUS_COLOR, status.statusColor)
        values.put(IS_MUTE, status.isMute)
        values.put(STATUS_TYPE, status.statusType)
        values.put(STATUS_IMAGE_PATH, status.imagePath)
        try {
            rowId = db.insert(TABLE_STATUS, null, values)
            println("-- Status Inserted RowId : $rowId")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        db.close()
        return rowId
    }

    @SuppressLint("Recycle")
    fun getAllStatus(isSeenOrNot: Int): ArrayList<Status> {
        val db = this.writableDatabase
        val statuses = ArrayList<Status>()
        var temp: Status?
        val query: String =
            if (isSeenOrNot == -1) "SELECT * FROM Statuses ORDER BY statusId DESC" else {
                "SELECT * FROM Statuses WHERE isSeen = '$isSeenOrNot' AND isMute != 1 ORDER BY statusId DESC"
            }
        val cursor: Cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                temp = Status(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getInt(6),
                    cursor.getString(7),
                    cursor.getInt(8),
                    cursor.getInt(9),
                    cursor.getInt(10),
                    cursor.getString(11)
                )
                statuses.add(temp)
                temp = null
            } while (cursor.moveToNext())
        }
        return statuses
    }

    fun updateStatus(isSeen: Int, statusId: Int) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("isSeen", isSeen)
        val where = "statusId='$statusId'"
        try {
            val rows: Int = db.update("Statuses", cv, where, null)
            println("-- rows updated: $rows")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        db.close()
    }

    companion object {
        private const val DB_NAME = "fake_chat_database"
        private const val DB_VERSION = 1

        //TABLES
        private const val TABLE_USERS = "Users"
        private const val TABLE_CHAT = "Chat"
        private const val TABLE_CALL = "Calls"
        private const val TABLE_STATUS = "Statuses"

        // KEYS USER
        private const val USER_ID = "userId"
        private const val PROFILE_IMAGE = "profileImage"
        private const val NAME = "name"
        private const val ABOUT_INFO = "aboutInfo"
        private const val LAST_SEEN = "lastSeen"
        private const val PHONE_NO = "phoneNo"
        private const val DATE = "date"
        private const val IS_VERIFIED = "isVerified"
        private const val IS_ARCHIVE = "isArchive"
        private const val ENCRYPTED_TEXT = "encryptedText"
        private const val LAST_MESSAGE = "lastMessage"
        private const val LAST_MSG_TIME = "lastMsgTime"

        // KEYS CHAT
        private const val MESSAGE_ID = "messageId"
        private const val RECEIVER_NAME = "receiverName"
        private const val MESSAGE = "message"
        private const val TIME = "time"
        private const val SENDER_ID = "senderId"
        private const val RECEIVER_ID = "receiverId"
        private const val VIEW_TYPE = "viewType"
        private const val CHAT_DATE = "date"
        private const val IMAGE_PATH = "imagePath"
        private const val FILE_PATH = "filePath"

        // KEYS CALLS
        private const val CALL_ID = "callId"
        private const val CALL_RECEIVER_ID = "callReceiverId"
        private const val USER_NAME = "userName"
        private const val CALL_DIRECTION = "callDirection"
        private const val CALL_DATE = "date"
        private const val CALL_TIME = "time"
        private const val CALL_TYPE = "callType"
        private const val CALL_DURATION = "callDuration"
        private const val CALL_PROFILE_IMAGE = "callProfileImage"

        // KEYS STATUS
        private const val STATUS_ID = "statusId"
        private const val STATUS_UPLOADER_ID = "statusUploaderId"
        private const val STATUS_USER_NAME = "userName"
        private const val STATUS_MESSAGE = "statusMessage"
        private const val STATUS_DATE = "date"
        private const val STATUS_TIME = "time"
        private const val IS_SEEN = "isSeen"
        private const val STATUS_UPLOADER_PROFILE = "statusUploaderProfile"
        private const val STATUS_COLOR = "statusColor"
        private const val IS_MUTE = "isMute"
        private const val STATUS_TYPE = "statusType"
        private const val STATUS_IMAGE_PATH = "imagePath"
    }
}