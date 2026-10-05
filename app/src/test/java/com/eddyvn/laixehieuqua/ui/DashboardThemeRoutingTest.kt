package com.eddyvn.laixehieuqua.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardThemeRoutingTest{
    @Test fun sportCockpitUsesDedicatedLayout(){
        assertTrue(isSportCockpitLayout("SPORT_COCKPIT_V1"))
        assertFalse(isSportCockpitLayout("TFT_SPORT"))
        assertFalse(isSportCockpitLayout("PREMIUM_SEGMENTED"))
        assertFalse(isSportCockpitLayout(null))
    }
}
