package demo.embeddedsystem.ui.frags

import android.os.Bundle
import android.preference.PreferenceFragment
import androidx.annotation.Nullable
import demo.embeddedsystem.R

class PrefsFragment : PreferenceFragment() {
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make sure default values are applied.  In a real app, you would
        // want this in a shared function that is used to retrieve the
        // SharedPreferences wherever they are needed.
//        PreferenceManager.setDefaultValues(getActivity(),
//                R.xml.advanced_preferences, false);
        addPreferencesFromResource(R.xml.settings)
    }
}