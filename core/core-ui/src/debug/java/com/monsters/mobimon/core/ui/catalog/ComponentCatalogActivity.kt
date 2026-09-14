package com.monsters.mobimon.core.ui.catalog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.monsters.mobimon.core.ui.ComponentGallery

/** Debug-only design review surface without live data or business commands. */
class ComponentCatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ComponentGallery() }
    }
}
