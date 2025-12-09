package com.example.shadowvault.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class FileListViewModel(
    private val savedStateHandle: String
) : ViewModel() {

    private val _files = MutableStateFlow<List<File>>(emptyList())
    val files: StateFlow<List<File>> = _files

    private val _selected = MutableStateFlow<Set<File>>(emptySet())
    val selected: StateFlow<Set<File>> = _selected

    fun load(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val root = File(path)
            val list = root.listFiles()?.toList() ?: emptyList()
            _files.value = list
        }
    }

    fun toggleSelection(file: File) {
        val current = _selected.value.toMutableSet()
        if (current.contains(file)) current.remove(file)
        else current.add(file)
        _selected.value = current
    }

    fun clearSelection() {
        _selected.value = emptySet()
    }

    fun selectAll() {
        _selected.value = _files.value.toSet()
    }

    fun deleteSelected() {
        viewModelScope.launch(Dispatchers.IO) {
            val toDelete = _selected.value.toList()
            toDelete.forEach { it.delete() }
            val updated = _files.value.filter { it !in toDelete }
            _files.value = updated
            _selected.value = emptySet()
        }
    }

    fun renameFile(file: File, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newFile = File(file.parent, newName)
            if (file.renameTo(newFile)) {
                _files.value = _files.value.map { if (it == file) newFile else it }
                _selected.value = setOf(newFile)
            }
        }
    }
}
