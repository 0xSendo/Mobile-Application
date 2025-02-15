package com.example.myacademate

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.security.MessageDigest

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "academate.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USER = "users"
        private const val TABLE_TASK = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_FIRST_NAME = "first_name"
        private const val COLUMN_LAST_NAME = "last_name"
        private const val COLUMN_COURSE = "course"
        private const val COLUMN_YEAR_LEVEL = "year_level"
        private const val COLUMN_SUBJECT_NAME = "subject_name"
        private const val COLUMN_COURSE_CODE = "course_code"
        private const val COLUMN_DUE_TIME = "due_time"
        private const val COLUMN_BIRTHDATE = "birthdate"
        private const val COLUMN_IS_DONE = "is_done" // New column for task completion
    }


    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("PRAGMA foreign_keys = ON;")

        val createUserTable = """
            CREATE TABLE $TABLE_USER (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL UNIQUE,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_FIRST_NAME TEXT NOT NULL,
                $COLUMN_LAST_NAME TEXT NOT NULL,
                $COLUMN_COURSE TEXT NOT NULL,
                $COLUMN_YEAR_LEVEL TEXT NOT NULL,
                $COLUMN_BIRTHDATE TEXT NOT NULL
            )
        """
        db?.execSQL(createUserTable)


        val CREATE_TASK_TABLE = """
            CREATE TABLE $TABLE_TASK (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_SUBJECT_NAME TEXT NOT NULL,
                $COLUMN_COURSE_CODE TEXT NOT NULL,
                $COLUMN_DUE_TIME TEXT NOT NULL,
                $COLUMN_IS_DONE INTEGER DEFAULT 0, 
                FOREIGN KEY ($COLUMN_USERNAME) REFERENCES $TABLE_USER($COLUMN_USERNAME) ON DELETE CASCADE
        )
    """.trimIndent()
        Log.d("DatabaseHelper", "Executing CREATE_TASK_TABLE: $CREATE_TASK_TABLE")
        db?.execSQL(CREATE_TASK_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            db?.execSQL("PRAGMA foreign_keys = OFF;") // Temporarily disable foreign keys to avoid constraint issues
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASK") // Drop task table first
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER") // Then drop user table
            db?.execSQL("PRAGMA foreign_keys = ON;") // Re-enable foreign keys
            onCreate(db)
        }
    }

    fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun addUser(firstName: String, lastName: String, course: String, yearLevel: String, username: String, password: String, birthdate: String): Long {
        val db = writableDatabase
        val hashedPassword = hashPassword(password)
        val trimmedUsername = username.trim()

        // Check if the username is already taken
        if (isUsernameTaken(trimmedUsername)) {
            Log.d("DatabaseHelper", "Username '$trimmedUsername' is already taken.")
            return -1L
        }

        val contentValues = ContentValues().apply {
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_LAST_NAME, lastName)
            put(COLUMN_COURSE, course)
            put(COLUMN_YEAR_LEVEL, yearLevel)
            put(COLUMN_USERNAME, trimmedUsername)
            put(COLUMN_PASSWORD, hashedPassword)
            put("birthdate", birthdate)  // Inserting birthdate here
        }

        val result = db.insert(TABLE_USER, null, contentValues)
        Log.d("DatabaseHelper", "Insertion result for username '$trimmedUsername': $result")
        return result
    }



    fun deleteUser(username: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_USER, "$COLUMN_USERNAME=?", arrayOf(username))
        db.close()
        return result > 0
    }


    fun isUsernameTaken(username: String): Boolean {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isEmpty()) return true // Prevent empty username registration

        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_USER WHERE $COLUMN_USERNAME = ? COLLATE NOCASE"
        val cursor = db.rawQuery(query, arrayOf(trimmedUsername))

        var exists = false
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0 // If count > 0, username exists
        }

        Log.d("DatabaseHelper", "Username check for '$trimmedUsername': $exists")

        cursor.close()
        return exists
    }

    fun updateUser(id: Int, firstName: String, lastName: String, course: String, yearLevel: String, birthdate: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_LAST_NAME, lastName)
            put(COLUMN_COURSE, course)
            put(COLUMN_YEAR_LEVEL, yearLevel)
            if (birthdate != null) {
                put("birthdate", birthdate)
            } else {
                put("birthdate", "")  // Optionally set null or empty string if birthdate is null
            }
        }
        val result = db.update(TABLE_USER, contentValues, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }






    fun checkUserCredentials(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USER WHERE $COLUMN_USERNAME = ? COLLATE NOCASE"
        val cursor: Cursor = db.rawQuery(query, arrayOf(username.trim()))
        if (cursor.moveToFirst()) {
            val storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            val hashedPassword = hashPassword(password)
            cursor.close()
            return storedPassword == hashedPassword
        }
        cursor.close()
        return false
    }

    fun getUserData(username: String): User? {
        val db = this.readableDatabase
        val trimmedUsername = username.trim()
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USER WHERE $COLUMN_USERNAME = ? COLLATE NOCASE",
            arrayOf(trimmedUsername)
        )


        var user: User? = null
        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(COLUMN_ID)
            val usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME)
            val firstNameIndex = cursor.getColumnIndex(COLUMN_FIRST_NAME)
            val lastNameIndex = cursor.getColumnIndex(COLUMN_LAST_NAME)
            val courseIndex = cursor.getColumnIndex(COLUMN_COURSE)
            val yearLevelIndex = cursor.getColumnIndex(COLUMN_YEAR_LEVEL)
            val birthdateIndex = cursor.getColumnIndex(COLUMN_BIRTHDATE)

            if (idIndex != -1 && usernameIndex != -1 && firstNameIndex != -1 &&
                lastNameIndex != -1 && courseIndex != -1 && yearLevelIndex != -1 && birthdateIndex != -1) {

                user = User(
                    id = cursor.getInt(idIndex),
                    username = cursor.getString(usernameIndex),
                    firstName = cursor.getString(firstNameIndex),
                    lastName = cursor.getString(lastNameIndex),
                    course = cursor.getString(courseIndex),
                    yearLevel = cursor.getString(yearLevelIndex),
                    birthdate = cursor.getString(birthdateIndex)
                )
                Log.d("DatabaseHelper", "Fetched User: $user")
            } else {
                Log.e("DatabaseHelper", "One or more column indices not found")
            }
        } else {
            Log.e("DatabaseHelper", "No user found for username: $trimmedUsername")
        }
        cursor.close()
        return user
    }





    fun addTask(username: String, subjectName: String, courseCode: String, dueTime: String): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_USERNAME, username.trim())
            put(COLUMN_SUBJECT_NAME, subjectName)
            put(COLUMN_COURSE_CODE, courseCode)
            put(COLUMN_DUE_TIME, dueTime)
        }
        return db.insert(TABLE_TASK, null, contentValues)
    }

    fun getUserTasks(username: String): List<Task> {
        val db = this.readableDatabase
        val taskList = mutableListOf<Task>()
        val query = "SELECT * FROM $TABLE_TASK WHERE $COLUMN_USERNAME = ? COLLATE NOCASE"
        val cursor: Cursor = db.rawQuery(query, arrayOf(username.trim()))

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val subjectName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT_NAME))
            val courseCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COURSE_CODE))
            val dueTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_TIME))
            val isDone = cursor.getInt(cursor.getColumnIndexOrThrow("is_done")) == 1
            taskList.add(Task(id, subjectName, courseCode, dueTime, isDone))
        }
        cursor.close()
        return taskList
    }

    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_SUBJECT_NAME, task.subjectName)
            put(COLUMN_COURSE_CODE, task.courseCode)
            put(COLUMN_DUE_TIME, task.dueTime)
            put("is_done", if (task.isDone) 1 else 0)
        }
        return db.update(TABLE_TASK, contentValues, "$COLUMN_ID=?", arrayOf(task.id.toString()))
    }

    // Delete a task by ID
    fun deleteTask(taskId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_TASK, "$COLUMN_ID=?", arrayOf(taskId.toString()))
    }

    fun toggleTaskStatus(taskId: Int, isDone: Boolean): Int {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("is_done", if (isDone) 1 else 0)
        }
        return db.update(TABLE_TASK, contentValues, "$COLUMN_ID=?", arrayOf(taskId.toString()))
    }

    data class User(
        val id: Int,
        val username: String,
        val firstName: String,
        val lastName: String,
        val course: String,
        val yearLevel: String,
        val birthdate: String // Add birthdate here
    )


    data class Task(
        val id: Int,
        val subjectName: String,
        val courseCode: String,
        val dueTime: String,
        val isDone: Boolean = false,
        var completionPercentage: Int = 0
    )



}
