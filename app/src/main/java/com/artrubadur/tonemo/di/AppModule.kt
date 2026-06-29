package com.artrubadur.tonemo.di

import com.artrubadur.tonemo.ui.screens.chat.ChatViewModel
import com.artrubadur.tonemo.ui.screens.connections.ConnectionsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        ChatViewModel(
            get(), get()
        )
    }
    viewModel {
        ConnectionsViewModel(
            get()
        )
    }
}
