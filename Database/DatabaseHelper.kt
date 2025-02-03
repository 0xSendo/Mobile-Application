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
        }

        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASK")

            val CREATE_USER_TABLE = """
        CREATE TABLE $TABLE_USER (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USERNAME TEXT NOT NULL UNIQUE,
            $COLUMN_PASSWORD TEXT NOT NULL,
            $COLUMN_FIRST_NAME TEXT NOT NULL,
            $COLUMN_LAST_NAME TEXT NOT NULL,
            $COLUMN_COURSE TEXT NOT NULL,
            $COLUMN_YEAR_LEVEL TEXT NOT NULL
        )
    """
            Log.d("DatabaseHelper", "Executing CREATE_USER_TABLE: $CREATE_USER_TABLE")
            db?.execSQL(CREATE_USER_TABLE)

            val CREATE_TASK_TABLE = """
        CREATE TABLE $TABLE_TASK (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USERNAME TEXT NOT NULL,
            $COLUMN_SUBJECT_NAME TEXT NOT NULL,
            $COLUMN_COURSE_CODE TEXT NOT NULL,
            $COLUMN_DUE_TIME TEXT NOT NULL
        )
    """
            Log.d("DatabaseHelper", "Executing CREATE_TASK_TABLE: $CREATE_TASK_TABLE")
            db?.execSQL(CREATE_TASK_TABLE)
        }
    
        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            if (oldVersion < newVersion) {
                db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
                db?.execSQL("DROP TABLE IF EXISTS $TABLE_TASK")
                onCreate(db)
            }
        }
    
        fun hashPassword(password: String): String {
            val bytes = password.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    
        fun addUser(firstName: String, lastName: String, course: String, yearLevel: String, username: String, password: String): Long {
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
            }
    
            val result = db.insert(TABLE_USER, null, contentValues)
            Log.d("DatabaseHelper", "Insertion result for username '$trimmedUsername': $result")
            return result
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
            val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_USER WHERE $COLUMN_USERNAME = ?", arrayOf(username))

            var user: User? = null
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(COLUMN_ID)
                val usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME)
                val firstNameIndex = cursor.getColumnIndex(COLUMN_FIRST_NAME)
                val lastNameIndex = cursor.getColumnIndex(COLUMN_LAST_NAME)
                val courseIndex = cursor.getColumnIndex(COLUMN_COURSE)
                val yearLevelIndex = cursor.getColumnIndex(COLUMN_YEAR_LEVEL)

                if (idIndex != -1 && usernameIndex != -1 && firstNameIndex != -1 && lastNameIndex != -1 && courseIndex != -1 && yearLevelIndex != -1) {
                    user = User(
                        id = cursor.getInt(idIndex),
                        username = cursor.getString(usernameIndex),
                        firstName = cursor.getString(firstNameIndex),
                        lastName = cursor.getString(lastNameIndex),
                        course = cursor.getString(courseIndex),
                        yearLevel = cursor.getString(yearLevelIndex)
                    )
                }
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
                val subjectName = cursor.getString(cursor.getColumnIndex(COLUMN_SUBJECT_NAME))
                val courseCode = cursor.getString(cursor.getColumnIndex(COLUMN_COURSE_CODE))
                val dueTime = cursor.getString(cursor.getColumnIndex(COLUMN_DUE_TIME))
                taskList.add(Task(subjectName, courseCode, dueTime))
            }
            cursor.close()
            return taskList
        }
    
        data class User(
            val id: Int,
            val username: String,
            val firstName: String,
            val lastName: String,
            val course: String,
            val yearLevel: String
        )
    
    
        data class Task(val subjectName: String, val courseCode: String, val dueTime: String)
    }
