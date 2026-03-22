package perozzi.gib

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import perozzi.gib.ui.navigation.GibApp
import perozzi.gib.ui.theme.GibTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as GibApplication).container
        setContent {
            GibTheme {
                GibApp(container = container)
            }
        }
    }
}
