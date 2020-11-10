package xyz.harmonyapp.olympusblog.ui.main.profile.state

import xyz.harmonyapp.olympusblog.utils.StateEvent

sealed class ProfileStateEvent : StateEvent {

    class ProfileSearchEvent : ProfileStateEvent() {
        override fun errorInfo(): String {
            return "Error searching for profiles."
        }

        override fun toString(): String {
            return "ProfileSearchEvent"
        }
    }

    class ToggleFollowEvent : ProfileStateEvent() {
        override fun errorInfo(): String {
            return "Error following author."
        }

        override fun toString(): String {
            return "ToggleFollowEvent"
        }
    }

    class None : ProfileStateEvent() {
        override fun errorInfo(): String {
            return "None."
        }
    }
}