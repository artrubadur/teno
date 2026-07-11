package com.artrubadur.tonemo.di

import com.artrubadur.tonemo.ui.screens.chat.ChatViewModel
import com.artrubadur.tonemo.ui.screens.connections.ConnectionsViewModel
import com.artrubadur.tonemo.ui.screens.home.HomeViewModel
import com.artrubadur.tonemo.ui.screens.tools.ToolsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        HomeViewModel(androidApplication())
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
