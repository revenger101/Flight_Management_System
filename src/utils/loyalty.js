export const LOYALTY_TIERS = [
  { name: 'Bronze', min: 0, max: 4999 },
  { name: 'Silver', min: 5000, max: 14999 },
  { name: 'Gold', min: 15000, max: 29999 },
  { name: 'Platinum', min: 30000, max: Infinity },
];

export function getStatusForMiles(miles = 0) {
  const m = Number(miles || 0);
  return LOYALTY_TIERS.find((t) => m >= t.min && m <= t.max)?.name || 'Bronze';
}

export function getNextTierProgress(miles = 0) {
  const m = Number(miles || 0);
  const idx = LOYALTY_TIERS.findIndex((t) => m >= t.min && m <= t.max);
  const tier = LOYALTY_TIERS[Math.max(0, idx)];
  const next = LOYALTY_TIERS[idx + 1] || null;

  if (!next) {
    return {
      currentTier: tier.name,
      nextTier: null,
      progressPct: 100,
      remainingMiles: 0,
    };
  }

  const span = next.min - tier.min;
  const progressed = m - tier.min;
  const progressPct = Math.max(0, Math.min(100, Math.round((progressed / span) * 100)));

  return {
    currentTier: tier.name,
    nextTier: next.name,
    progressPct,
    remainingMiles: Math.max(0, next.min - m),
  };
}
