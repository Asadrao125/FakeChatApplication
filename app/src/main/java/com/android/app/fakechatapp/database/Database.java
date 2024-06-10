package com.android.app.fakechatapp.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import com.android.app.fakechatapp.models.Call;
import com.android.app.fakechatapp.models.Chat;
import com.android.app.fakechatapp.models.Status;
import com.android.app.fakechatapp.models.User;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Database {
    String DB_PATH = "data/data/com.android.app.fakechatapp/databases/";
    String DB_NAME = "fake_chat_database.sqlite";
    Context activity;
    SQLiteDatabase sqLiteDatabase;

    public Database(Context activity) {
        this.activity = activity;
    }

    public void createDatabase() {
        boolean dBExist = false;
        try {
            dBExist = checkDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!dBExist) {
            try {
                sqLiteDatabase = activity.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
                sqLiteDatabase.close();
                copyDatabaseTable();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void copyDatabaseTable() throws IOException {
        InputStream myInput = activity.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    private boolean checkDatabase() {
        SQLiteDatabase checkDB = null;
        String myPath = DB_PATH + DB_NAME;
        try {
            try {
                checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            //no database exists...
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    public void open() {
        sqLiteDatabase = activity.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
    }

    public void close() {
        sqLiteDatabase.close();
    }

    //============================ Start Custom Methods Crud ====================================

    /*
    * CREATE TABLE "Users" (
	"userId"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"profileImage"	BLOB,
	"name"	TEXT,
	"aboutInfo"	TEXT,
	"lastSeen"	TEXT,
	"phoneNo"	TEXT,
	"date"	TEXT,
	"isVerified"	INTEGER,
	"isArchive"	INTEGER,
	"encryptedText"	TEXT,
	"lastMessage"	TEXT);
    */

    /*
    * CREATE TABLE "Users" (
	"userId"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"profileImage"	TEXT,
	"name"	TEXT,
	"aboutInfo"	TEXT,
	"lastSeen"	TEXT,
	"phoneNo"	TEXT,
	"date"	TEXT,
	"isVerified"	INTEGER,
	"isArchive"	INTEGER,
	"encryptedText"	TEXT,
	"lastMessage"	TEXT,
	"lastMsgTime"	TEXT);
    */

    public long insertUser(User user) {
        long rowId = -1;
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put("profileImage", user.getProfileImage());
            cv.put("name", user.getName());
            cv.put("aboutInfo", user.getAboutInfo());
            cv.put("lastSeen", user.getLastSeen());
            cv.put("phoneNo", user.getPhoneNo());
            cv.put("date", user.getDate());
            cv.put("isVerified", user.isVerified());
            cv.put("isArchive", user.isArchive());
            cv.put("encryptedText", user.getEncryptedText());
            cv.put("lastMessage", user.getLastMessage());
            cv.put("lastMsgTime", user.getLastMsgTime());
            rowId = sqLiteDatabase.insert("Users", null, cv);
            close();
        } catch (SQLiteException e) {
            Toast.makeText(activity, "Database exception", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        System.out.println("-- Record inserted rowId : " + rowId);
        return rowId;
    }   //end insertUser

    public ArrayList<User> getAllUsers(int isArchivee) {
        open();
        ArrayList<User> users = new ArrayList<>();
        User temp;
        String query;
        String query1 = "select * from Users";
        String query2 = "SELECT * FROM Users WHERE isArchive = '" + isArchivee + "'";

        if (isArchivee == -1)
            query = query1;
        else
            query = query2;


        System.out.println("--query in getAllUsers : " + query);
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex("userId"));
                @SuppressLint("Range") String profileImage = cursor.getString(cursor.getColumnIndex("profileImage"));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range") String aboutInfo = cursor.getString(cursor.getColumnIndex("aboutInfo"));
                @SuppressLint("Range") String lastSeen = cursor.getString(cursor.getColumnIndex("lastSeen"));
                @SuppressLint("Range") String phoneNo = cursor.getString(cursor.getColumnIndex("phoneNo"));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") int isVerified = cursor.getInt(cursor.getColumnIndex("isVerified"));
                @SuppressLint("Range") int isArchive = cursor.getInt(cursor.getColumnIndex("isArchive"));
                @SuppressLint("Range") String encryptedText = cursor.getString(cursor.getColumnIndex("encryptedText"));
                @SuppressLint("Range") String lastMessage = cursor.getString(cursor.getColumnIndex("lastMessage"));
                @SuppressLint("Range") String lastMsgTime = cursor.getString(cursor.getColumnIndex("lastMsgTime"));

                temp = new User(userId, profileImage, name, aboutInfo, lastSeen, phoneNo, date, isVerified, isArchive, encryptedText, lastMessage, lastMsgTime);

                users.add(temp);
                temp = null;
            }
            while (cursor.moveToNext());
            close();
            return users;
        }
        close();
        return null;
    }   //======end getAllUsers()===========

    public int getArchiveCount(int isArchivee) {
        open();
        int count = 0;
        String query = "SELECT COUNT(*) FROM Users WHERE isArchive = '" + isArchivee + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        close();

        return count;
    }

    public User getSingleUser(int uId) {
        open();
        User temp;
        //String query = "select * from Users";
        String query = "select * from Users WHERE userId = '" + uId + "'";

        System.out.println("--query in getSingleUser : " + query);
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int userId = cursor.getInt(cursor.getColumnIndex("userId"));
                @SuppressLint("Range") String profileImage = cursor.getString(cursor.getColumnIndex("profileImage"));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
                @SuppressLint("Range") String aboutInfo = cursor.getString(cursor.getColumnIndex("aboutInfo"));
                @SuppressLint("Range") String lastSeen = cursor.getString(cursor.getColumnIndex("lastSeen"));
                @SuppressLint("Range") String phoneNo = cursor.getString(cursor.getColumnIndex("phoneNo"));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") int isVerified = cursor.getInt(cursor.getColumnIndex("isVerified"));
                @SuppressLint("Range") int isArchive = cursor.getInt(cursor.getColumnIndex("isArchive"));
                @SuppressLint("Range") String encryptedText = cursor.getString(cursor.getColumnIndex("encryptedText"));
                @SuppressLint("Range") String lastMessage = cursor.getString(cursor.getColumnIndex("lastMessage"));
                @SuppressLint("Range") String lastMsgTime = cursor.getString(cursor.getColumnIndex("lastMsgTime"));

                temp = new User(userId, profileImage, name, aboutInfo, lastSeen, phoneNo, date, isVerified, isArchive, encryptedText, lastMessage, lastMsgTime);
            }
            while (cursor.moveToNext());
            close();
            return temp;
        }
        close();
        return null;
    }   //======end getSingleUser()===========

    public void updateUserArchiveStatus(int userId, int isArchive) {
        open();
        ContentValues cv = new ContentValues();
        cv.put("isArchive", isArchive);
        String where = "userId" + "=" + "'" + userId + "'";
        try {
            int rows = sqLiteDatabase.update("Users", cv, where, null);
            System.out.println("-- rows updated: " + rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }

    public void updateLastMessageAndTime(int userId, String lastMsg, String lastMsgTime) {
        open();
        ContentValues cv = new ContentValues();
        cv.put("lastMessage", lastMsg);
        cv.put("lastMsgTime", lastMsgTime);
        String where = "userId" + "=" + "'" + userId + "'";
        try {
            int rows = sqLiteDatabase.update("Users", cv, where, null);
            System.out.println("-- rows updated: " + rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }

    public void deleteUser(int userId) {
        open();
        String query = "DELETE FROM Users WHERE userId = '" + userId + "'";
        sqLiteDatabase.execSQL(query);
        close();
    }

    /*
    CREATE TABLE "Chat" (
	"messageId"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"receiverName"	TEXT,
	"message"	TEXT,
	"time"	TEXT,
	"senderId"	INTEGER,
	"receiverId"	INTEGER,
	"viewType"	INTEGER,
	"date"	TEXT,
	"imagePath"	TEXT,
	"filePath"	TEXT);
    */

    public long insertChat(Chat chat) {
        long rowId = -1;
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put("receiverName", chat.getReceiverName());
            cv.put("message", chat.getMessage());
            cv.put("time", chat.getTime());
            cv.put("senderId", chat.getSenderId());
            cv.put("receiverId", chat.getReceiverId());
            cv.put("viewType", chat.getViewType());
            cv.put("date", chat.getDate());
            cv.put("imagePath", chat.getImagePath());
            cv.put("filePath", chat.getFilePath());
            rowId = sqLiteDatabase.insert("Chat", null, cv);
            close();
        } catch (SQLiteException e) {
            Toast.makeText(activity, "Database exception", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        System.out.println("-- Record inserted rowId : " + rowId);
        return rowId;
    }   //end insertChat

    public ArrayList<Chat> getUserChats(int sendId) {
        open();
        ArrayList<Chat> chats = new ArrayList<>();
        Chat temp;
        String query = "select * from Chat where senderId = '" + sendId + "'";
        System.out.println("--query in getUserChats : " + query);
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int messageId = cursor.getInt(cursor.getColumnIndex("messageId"));
                @SuppressLint("Range") String receiverName = cursor.getString(cursor.getColumnIndex("receiverName"));
                @SuppressLint("Range") String message = cursor.getString(cursor.getColumnIndex("message"));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));
                @SuppressLint("Range") int senderId = cursor.getInt(cursor.getColumnIndex("senderId"));
                @SuppressLint("Range") int receiverId = cursor.getInt(cursor.getColumnIndex("receiverId"));
                @SuppressLint("Range") int viewType = cursor.getInt(cursor.getColumnIndex("viewType"));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex("imagePath"));
                @SuppressLint("Range") String filePath = cursor.getString(cursor.getColumnIndex("filePath"));
                temp = new Chat(messageId, receiverName, message, time, senderId, receiverId, viewType, date, imagePath, filePath);
                chats.add(temp);
                temp = null;
            }
            while (cursor.moveToNext());
            close();
            return chats;
        }
        close();
        return null;
    }//======end getUserChats()===========

    public void deleteChat(int senderId) {
        open();
        String query = "DELETE FROM Chat WHERE senderId = '" + senderId + "'";
        sqLiteDatabase.execSQL(query);
        close();
    }

    /*
    * CREATE TABLE "Calls" (
	"callId"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"callReceiverId"	INTEGER,
	"userName"	TEXT,
	"callDirection"	TEXT,
	"date"	TEXT,
	"time"	TEXT,
	"callType"	TEXT,
	"callDuration"	TEXT,
	"callProfileImage"	BLOB);
    */

    /*
    *CREATE TABLE "Calls" (
	"callId"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"callReceiverId"	INTEGER,
	"userName"	TEXT,
	"callDirection"	TEXT,
	"date"	TEXT,
	"time"	TEXT,
	"callType"	TEXT,
	"callDuration"	TEXT,
	"callProfileImage"	TEXT);
    */

    public long insertCall(Call call) {
        long rowId = -1;
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put("callReceiverId", call.getCallReceiverId());
            cv.put("userName", call.getUserName());
            cv.put("callDirection", call.getCallDirection());
            cv.put("date", call.getDate());
            cv.put("time", call.getTime());
            cv.put("callType", call.getCallType());
            cv.put("callDuration", call.getCallDuration());
            cv.put("callProfileImage", call.getCallProfileImage());
            rowId = sqLiteDatabase.insert("Calls", null, cv);
            close();
        } catch (SQLiteException e) {
            Toast.makeText(activity, "Database exception", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        System.out.println("-- Record inserted rowId : " + rowId);
        return rowId;
    }   //end insertCall

    public ArrayList<Call> getAllCalls() {
        open();
        ArrayList<Call> calls = new ArrayList<>();
        Call temp;
        String query = "SELECT c.callId, c.callReceiverId, c.userName, c.callDirection, c.date, c.time, c.callType, c.callDuration, c.callProfileImage " +
                "FROM Calls c " +
                "INNER JOIN (SELECT MAX(callId) AS maxCallId FROM Calls GROUP BY callReceiverId) t " +
                "ON c.callId = t.maxCallId " +
                "ORDER BY c.callId DESC";

        System.out.println("--query in getAllCalls : " + query);
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int callId = cursor.getInt(cursor.getColumnIndex("callId"));
                @SuppressLint("Range") int callReceiverId = cursor.getInt(cursor.getColumnIndex("callReceiverId"));
                @SuppressLint("Range") String userName = cursor.getString(cursor.getColumnIndex("userName"));
                @SuppressLint("Range") String callDirection = cursor.getString(cursor.getColumnIndex("callDirection"));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));
                @SuppressLint("Range") String callType = cursor.getString(cursor.getColumnIndex("callType"));
                @SuppressLint("Range") String callDuration = cursor.getString(cursor.getColumnIndex("callDuration"));
                @SuppressLint("Range") String callProfileImage = cursor.getString(cursor.getColumnIndex("callProfileImage"));
                temp = new Call(callId, callReceiverId, userName, callDirection, date, time, callType, callDuration, callProfileImage);
                calls.add(temp);
                temp = null;
            }
            while (cursor.moveToNext());
            close();
            return calls;
        }
        close();
        return null;
    } //======end getAllCalls()===========

    public ArrayList<Call> getAllCallsByUser(int userId) {
        open();
        ArrayList<Call> calls = new ArrayList<>();
        Call temp;
        String query = "SELECT * FROM Calls WHERE callReceiverId = '" + userId + "' ORDER BY callId DESC";
        System.out.println("--query in getAllCallsByUser : " + query);
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int callId = cursor.getInt(cursor.getColumnIndex("callId"));
                @SuppressLint("Range") int callReceiverId = cursor.getInt(cursor.getColumnIndex("callReceiverId"));
                @SuppressLint("Range") String userName = cursor.getString(cursor.getColumnIndex("userName"));
                @SuppressLint("Range") String callDirection = cursor.getString(cursor.getColumnIndex("callDirection"));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));
                @SuppressLint("Range") String callType = cursor.getString(cursor.getColumnIndex("callType"));
                @SuppressLint("Range") String callDuration = cursor.getString(cursor.getColumnIndex("callDuration"));
                @SuppressLint("Range") String callProfileImage = cursor.getString(cursor.getColumnIndex("callProfileImage"));
                temp = new Call(callId, callReceiverId, userName, callDirection, date, time, callType, callDuration, callProfileImage);
                calls.add(temp);
                temp = null;
            }
            while (cursor.moveToNext());
            close();
            return calls;
        }
        close();
        return null;
    } //======end getAllCallsByUser()===========

    public void updateCallDuration(String duration, long callId) {
        open();
        ContentValues cv = new ContentValues();
        cv.put("callDuration", duration);
        String where = "callId" + "=" + "'" + callId + "'";
        try {
            int rows = sqLiteDatabase.update("Calls", cv, where, null);
            System.out.println("-- rows updated: " + rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }

    public void updateCallTime(String time, long callId) {
        open();
        ContentValues cv = new ContentValues();
        cv.put("callDuration", time);
        String where = "callId" + "=" + "'" + callId + "'";
        try {
            int rows = sqLiteDatabase.update("Calls", cv, where, null);
            System.out.println("-- rows updated: " + rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }

    /*
    * CREATE TABLE "Statuses" (
	"statusId"	INTEGER PRIMARY KEY AUTOINCREMENT,
	"statusUploaderId"	INTEGER,
	"userName"	TEXT,
	"statusMessage"	TEXT,
	"date"	TEXT,
	"time"	TEXT,
	"isSeen"	INTEGER,
	"statusUploaderProfile"	TEXT,
	"statusColor"	INTEGER,
	"isMute"	INTEGER,
	"statusType"	INTEGER,
	"imagePath"	TEXT);
    */

    public long insertStatus(Status status) {
        long rowId = -1;
        try {
            open();
            ContentValues cv = new ContentValues();
            cv.put("statusUploaderId", status.getStatusUploaderId());
            cv.put("userName", status.getUserName());
            cv.put("statusMessage", status.getStatusMessage());
            cv.put("date", status.getDate());
            cv.put("time", status.getTime());
            cv.put("isSeen", status.isSeen());
            cv.put("statusUploaderProfile", status.getStatusUploaderProfile());
            cv.put("statusColor", status.getStatusColor());
            cv.put("isMute", status.isMute());
            cv.put("statusType", status.getStatusType());
            cv.put("imagePath", status.getImagePath());
            rowId = sqLiteDatabase.insert("Statuses", null, cv);
            close();
        } catch (SQLiteException e) {
            Toast.makeText(activity, "Database exception", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        System.out.println("-- Record inserted rowId : " + rowId);
        return rowId;
    }   //end insertStatus

    public ArrayList<Status> getAllStatus(int isSeenOrNot) {
        open();
        ArrayList<Status> statuses = new ArrayList<>();
        Status temp;
        String query;
        if (isSeenOrNot == -1)
            query = "SELECT * FROM Statuses ORDER BY statusId DESC";
        else {
            //query = "SELECT * FROM Statuses WHERE isSeen = '" + isSeenOrNot + "' ORDER BY statusId DESC";
            query = "SELECT * FROM Statuses WHERE isSeen = '" + isSeenOrNot + "' AND isMute != 1 ORDER BY statusId DESC";
        }
        System.out.println("--query in getAllStatus : " + query);
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int statusId = cursor.getInt(cursor.getColumnIndex("statusId"));
                @SuppressLint("Range") int statusUploaderId = cursor.getInt(cursor.getColumnIndex("statusUploaderId"));
                @SuppressLint("Range") String userName = cursor.getString(cursor.getColumnIndex("userName"));
                @SuppressLint("Range") String statusMessage = cursor.getString(cursor.getColumnIndex("statusMessage"));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));
                @SuppressLint("Range") int isSeen = cursor.getInt(cursor.getColumnIndex("isSeen"));
                @SuppressLint("Range") String statusUploaderProfile = cursor.getString(cursor.getColumnIndex("statusUploaderProfile"));
                @SuppressLint("Range") int statusColor = cursor.getInt(cursor.getColumnIndex("statusColor"));
                @SuppressLint("Range") int isMute = cursor.getInt(cursor.getColumnIndex("isMute"));
                @SuppressLint("Range") int statusType = cursor.getInt(cursor.getColumnIndex("statusType"));
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex("imagePath"));
                temp = new Status(statusId, statusUploaderId, userName, statusMessage, date, time, isSeen, statusUploaderProfile, statusColor, isMute, statusType, imagePath);
                statuses.add(temp);
                temp = null;
            }
            while (cursor.moveToNext());
            close();
            return statuses;
        }
        close();
        return null;
    } //end getAllStatus()

    public ArrayList<Status> getAllMutedStatus(int isMuteOrNot) {
        open();
        ArrayList<Status> statuses = new ArrayList<>();
        Status temp;
        String query = "SELECT * FROM Statuses WHERE isMute = '" + isMuteOrNot + "' ORDER BY statusId DESC";
        System.out.println("--query in getAllMutedStatus : " + query);
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int statusId = cursor.getInt(cursor.getColumnIndex("statusId"));
                @SuppressLint("Range") int statusUploaderId = cursor.getInt(cursor.getColumnIndex("statusUploaderId"));
                @SuppressLint("Range") String userName = cursor.getString(cursor.getColumnIndex("userName"));
                @SuppressLint("Range") String statusMessage = cursor.getString(cursor.getColumnIndex("statusMessage"));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("time"));
                @SuppressLint("Range") int isSeen = cursor.getInt(cursor.getColumnIndex("isSeen"));
                @SuppressLint("Range") String statusUploaderProfile = cursor.getString(cursor.getColumnIndex("statusUploaderProfile"));
                @SuppressLint("Range") int statusColor = cursor.getInt(cursor.getColumnIndex("statusColor"));
                @SuppressLint("Range") int isMute = cursor.getInt(cursor.getColumnIndex("isMute"));
                @SuppressLint("Range") int statusType = cursor.getInt(cursor.getColumnIndex("isMute"));
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex("imagePath"));
                temp = new Status(statusId, statusUploaderId, userName, statusMessage, date, time, isSeen, statusUploaderProfile, statusColor, isMute, statusType, imagePath);
                statuses.add(temp);
                temp = null;
            }
            while (cursor.moveToNext());
            close();
            return statuses;
        }
        close();
        return null;
    } //end getAllMutedStatus()

    public void updateStatus(int isSeen, int statusId) {
        open();
        ContentValues cv = new ContentValues();
        cv.put("isSeen", isSeen);
        String where = "statusId" + "=" + "'" + statusId + "'";
        try {
            int rows = sqLiteDatabase.update("Statuses", cv, where, null);
            System.out.println("-- rows updated: " + rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close();
    }
}