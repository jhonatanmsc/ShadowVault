package com.example.shadowvault.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.shadowvault.models.FileListViewModel

class FileListViewModelFactory(private val path: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FileListViewModel(path) as T
    }
}