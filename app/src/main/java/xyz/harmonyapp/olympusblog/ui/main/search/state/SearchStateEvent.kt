package xyz.harmonyapp.olympusblog.ui.main.search.state

import xyz.harmonyapp.olympusblog.utils.StateEvent

sealed class SearchStateEvent : StateEvent {

    class ProfileSearchEvent : SearchStateEvent() {
        override fun errorInfo(): String {
            return "Error searching for profiles."
        }

        override fun toString(): String {
            return "ProfileSearchEvent"
        }
    }

    class ToggleFollowEvent : SearchStateEvent() {
        override fun errorInfo(): String {
            return "Error following author."
        }

        override fun toString(): String {
            return "ToggleFollowEvent"
        }
    }

    class GetAuthorArticlesEvent : SearchStateEvent() {
        override fun errorInfo(): String {
            return "Error retrieving articles."
        }

        override fun toString(): String {
            return "GetAuthorArticlesEvent"
        }
    }

    class GetAuthorFavoritesEvent : SearchStateEvent() {
        override fun errorInfo(): String {
            return "Error retrieving articles."
        }

        override fun toString(): String {
            return "GetAuthorFavoritesEvent"
        }
    }

    class None : SearchStateEvent() {
        override fun errorInfo(): String {
            return "None."
        }
    }
}