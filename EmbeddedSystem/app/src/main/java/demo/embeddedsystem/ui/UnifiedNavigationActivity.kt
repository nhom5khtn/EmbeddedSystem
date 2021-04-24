package demo.embeddedsystem.ui

import android.R
import android.os.Bundle
import android.preference.PreferenceActivity
import androidx.annotation.Nullable
import demo.embeddedsystem.ui.frags.PrefsFragment

class UnifiedNavigationActivity : PreferenceActivity() {
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction()
            .replace(R.id.content, PrefsFragment()).commit()
    }
}