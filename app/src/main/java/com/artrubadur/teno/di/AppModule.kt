package com.artrubadur.teno.di

import com.artrubadur.teno.ui.screens.chat.ChatViewModel
import com.artrubadur.teno.ui.screens.connections.ConnectionsViewModel
import com.artrubadur.teno.ui.screens.home.HomeViewModel
import com.artrubadur.teno.ui.screens.tools.ToolsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        HomeViewModel(androidApplication(), get())
    }
    viewModel {
        ChatViewModel(androidApplication())
    }
    viewModel {
        ConnectionsViewModel(
            get()
        )
    }
    viewModel {
        ToolsViewModel(
            androidApplication(),
            get()
        )
    }
}
