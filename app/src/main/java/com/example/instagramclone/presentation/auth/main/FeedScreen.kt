package com.example.instagramclone.presentation.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.instagramclone.presentation.auth.main.BottomNavigationItem
import com.example.instagramclone.presentation.auth.main.BottomNavigationMenu
import com.example.instagramclone.viewModel.IgViewModel

@Composable
fun FeedScreen(navController: NavController, vm: IgViewModel){
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Feed Screen")

        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.FEED,
            navController = navController)
    }
}