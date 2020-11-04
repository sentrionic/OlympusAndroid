package xyz.harmonyapp.olympusblog.utils

import java.text.SimpleDateFormat
import java.util.*

class DateUtils {

    companion object {

        private val TAG: String = "AppDebug"
        private val formatter = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
        private val parser =  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

        // dates from server look like this: "2019-07-23T03:28:01.406944Z"
        fun formatDate(date: String): String {
            try {
                return formatter.format(parser.parse(date))
            } catch (e: Exception) {
                throw Exception(e)
            }
        }
    }
}