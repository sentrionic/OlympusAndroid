package xyz.harmonyapp.olympusblog.ui.main.account.state

import android.net.Uri
import xyz.harmonyapp.olympusblog.models.AccountProperties

class AccountViewState(
    var accountProperties: AccountProperties? = null,
    var updatedImageUri: Uri? = null
)