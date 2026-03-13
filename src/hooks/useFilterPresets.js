import { useMemo, useState } from 'react';

const STORAGE_PREFIX = 'airport.filters.';
const PRESETS_SUFFIX = '.presets';

function safeReadJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return fallback;
    const parsed = JSON.parse(raw);
    return parsed ?? fallback;
  } catch {
    return fallback;
  }
}

export function useFilterPresets(pageKey, defaultFilters) {
  const filtersKey = `${STORAGE_PREFIX}${pageKey}`;
  const presetsKey = `${STORAGE_PREFIX}${pageKey}${PRESETS_SUFFIX}`;

  const [filters, setFilters] = useState(() => safeReadJson(filtersKey, defaultFilters));
  const [presets, setPresets] = useState(() => safeReadJson(presetsKey, []));

  const persistFilters = (nextFilters) => {
    setFilters(nextFilters);
    localStorage.setItem(filtersKey, JSON.stringify(nextFilters));
  };

  const updateFilter = (key, value) => {
    const next = { ...filters, [key]: value };
    persistFilters(next);
  };

  const resetFilters = () => {
    persistFilters(defaultFilters);
  };

  const savePreset = (name) => {
    const trimmed = (name || '').trim();
    if (!trimmed) return { ok: false, reason: 'Preset name is required' };

    const existing = presets.find((p) => p.name.toLowerCase() === trimmed.toLowerCase());
    const id = existing?.id || `preset_${Date.now()}`;

    const nextPresets = [
      ...presets.filter((p) => p.id !== id),
      { id, name: trimmed, filters },
    ];

    setPresets(nextPresets);
    localStorage.setItem(presetsKey, JSON.stringify(nextPresets));
    return { ok: true };
  };

  const applyPreset = (presetId) => {
    const preset = presets.find((p) => p.id === presetId);
    if (!preset) return { ok: false, reason: 'Preset not found' };
    persistFilters({ ...defaultFilters, ...preset.filters });
    return { ok: true, name: preset.name };
  };

  const deletePreset = (presetId) => {
    const nextPresets = presets.filter((p) => p.id !== presetId);
    setPresets(nextPresets);
    localStorage.setItem(presetsKey, JSON.stringify(nextPresets));
  };

  const hasActiveFilters = useMemo(() => {
    return Object.keys(defaultFilters).some((k) => String(filters[k] ?? '') !== String(defaultFilters[k] ?? ''));
  }, [defaultFilters, filters]);

  return {
    filters,
    setFilters: persistFilters,
    updateFilter,
    resetFilters,
    presets,
    savePreset,
    applyPreset,
    deletePreset,
    hasActiveFilters,
  };
}
