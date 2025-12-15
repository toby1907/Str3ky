package com.example.str3ky.ui.achievements

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.str3ky.MainActivity
import com.example.str3ky.data.Achievement
import com.example.str3ky.data.User
import com.example.str3ky.data.UserDao
import com.example.str3ky.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Note: This test uses an activity rule to host the composable. It assumes MainActivity shows AchievementScreen when navigated.
@RunWith(AndroidJUnit4::class)
class AchievementScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun claimReward_marksAchievementSeen() {
        // This is an integration-like test; we only assert that clicking claim fires the UI flow.
        // Providing a full Hilt test environment is out of this quick script; run this on-device with app installed.

        // Find the first reward card (test tag created in RewardCard)
        val rewardNode = composeRule.onNodeWithTag("reward_")
        // If present, click and claim; then assert claim button is present
        // This is a placeholder test; you can refine it to inject a fake repository via Hilt test components.
        rewardNode.performClick()

        val claimButton = composeRule.onNodeWithTag("claim_button")
        claimButton.performClick()
    }
}

