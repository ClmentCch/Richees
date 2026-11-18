package com.richyss.app.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.richyss.app.core.AppContainer
import com.richyss.app.core.translationFor
import com.richyss.app.data.models.FieldIssue
import com.richyss.app.data.models.ProfileValidator
import com.richyss.app.data.models.RpcProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditorUiState(
    val draft: RpcProfile? = null,
    val isNew: Boolean = true,
    val issues: List<FieldIssue> = emptyList(),
    val loaded: Boolean = false
)

class EditorViewModel(
    private val container: AppContainer,
    private val profileId: String?
) : ViewModel() {

    private val draft = MutableStateFlow<RpcProfile?>(null)
    private val issues = MutableStateFlow<List<FieldIssue>>(emptyList())
    private val isNew = MutableStateFlow(profileId.isNullOrBlank())

    val state: StateFlow<EditorUiState> = combine(draft, issues, isNew) { current, problems, new ->
        EditorUiState(
            draft = current,
            isNew = new,
            issues = problems,
            loaded = current != null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditorUiState()
    )

    init {
        viewModelScope.launch {
            val existing = profileId?.takeIf { it.isNotBlank() }?.let { container.profiles.find(it) }
            draft.value = existing ?: RpcProfile(
                id = container.profiles.newId(),
                name = "",
                activityName = "Richyss",
                icon = RpcProfile.DEFAULT_ICON,
                details = "",
                state = ""
            )
            isNew.value = existing == null
        }
    }

    fun update(transform: (RpcProfile) -> RpcProfile) {
        draft.update { current -> current?.let(transform) }
        // Editing after a failed save clears the stale issue list.
        if (issues.value.isNotEmpty()) issues.value = emptyList()
    }

    fun save(onSaved: () -> Unit) {
        val current = draft.value ?: return
        val problems = ProfileValidator.validate(current)
        issues.value = problems
        if (problems.isNotEmpty()) {
            viewModelScope.launch {
                container.messages.show(translationFor(container).editorFieldRequired)
            }
            return
        }
        viewModelScope.launch {
            container.profiles.upsert(current)
            container.messages.show(translationFor(container).editorSaved)
            onSaved()
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val current = draft.value ?: return
        viewModelScope.launch {
            container.profiles.delete(current.id)
            onDeleted()
        }
    }

    companion object {
        fun factory(container: AppContainer, profileId: String?): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { EditorViewModel(container, profileId) }
            }
    }
}
