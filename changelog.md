# Changelog

### Added
- Item task auto-detection across inventory, equipment, bank, seed vault, and group storage.
- Equivalent item detection for Barrows gear: degraded versions (100/75/50/25/0) now treated as base items.
- New All Barrows Gear preset including every Ahrim’s, Dharok’s, Guthan’s, Karil’s, Torag’s, and Verac’s item.
- Quest prerequisites button: each quest task has an **Add prereqs** option to insert requirements beneath it.
- Shift+Click removal to delete a task and its children.
- Completion cascading: parent quest completion toggles propagate to children.
- Dropdown quest selector with natural quest names.
- Right-click **Add prereqs** for quest tasks with automatic insertion and indentation.
- Right-click menu to mark all child tasks complete or incomplete.
- Manual completion toggling for preset tasks.
- Configurable color setting for completion chat messages.
- Export and import goals to JSON with file filtering and full UI refresh.
- Preset Goal Lists with an “Add from Preset…” button (including Quest Cape, Ironman progressions, Void, F2P, Fast Travel, etc.).
- Full Void Armor, Free-to-Play Quests, and Fast Travel Unlocks presets.
- Expanded and reworked Early, Mid, and Late Ironman presets.
- Inline editing support for goal titles and manual task descriptions.
- Ellipsized titles with tooltips on hover for goal cards and task rows.

### Changed
- Quest detection stabilized and debounced with scheduled UI refresh to prevent login lag.
- Task and goal panels auto-refresh after quest or item detection without requiring navigation.
- Chat completion messages rewritten as proper `GAMEMESSAGE`s with color customization support.
- UI polish including a more compact prerequisites button, consistent ActionBar styling, unified headers, and improved readability.
- Right-click context menus reorganized with a grouped **Move** submenu and cleaner option labels.
- Search input redesigned to toggle open/close behavior.
- Preset prerequisite expansion now prevents duplicate additions.
- Task rows updated with consistent styling, icon warming, and inline editing capabilities.
- Home panel buttons stacked vertically; header divider thickness increased for clarity.
- Goal card typography fixed with reserved progress text width to prevent clipping.

### Fixed
- Fixed chat completion messages: now appear reliably as `GAMEMESSAGE`s with configured colors; raw `<col>` tags removed.
- Shield of Arrav preset partner-finding step properly indented as a subtask.
- Fixed sidebar and panels not refreshing after quest or item detection (no longer requires re-entering a goal or relogging).
- Fixed Home and Goal panel refresh issues, including blank panels, stale data, and child task refresh not propagating.
- Fixed item icons failing to appear until goal entry; now preload on startup, login, and after JSON import.
- Fixed Export and Import buttons (now fully functional).
- Fixed Undo/Redo buttons: restricted to Goal view and visual overlaps resolved.
- Fixed mouse and keyboard input handling for selection, copy/paste, and shortcuts.
- Fixed empty goals remaining in list; now automatically removed if left without tasks.
- Fixed overlapping Export button layout issue.
- Fixed quest tasks not showing correct status on login until entering the goal.
