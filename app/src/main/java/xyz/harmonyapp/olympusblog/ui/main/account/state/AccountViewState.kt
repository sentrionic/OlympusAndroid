package xyz.harmonyapp.olympusblog.ui.main.account.state

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import xyz.harmonyapp.olympusblog.models.AccountProperties

const val ACCOUNT_VIEW_STATE_BUNDLE_KEY = "xyz.harmonyapp.olympusblog.ui.main.account.state.AccountViewState"

@Parcelize
class AccountViewState(
    var accountProperties: AccountProperties? = null,
    var updatedImageUri: Uri? = null
) : Parcelable