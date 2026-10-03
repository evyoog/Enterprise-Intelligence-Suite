// C67: explicit format overrides on top of the region. 'auto' (the default)
// keeps the region's own convention, so nothing changes until a user picks one.
export const DATE_FORMATS = ['auto', 'DD/MM/YYYY', 'MM/DD/YYYY', 'YYYY-MM-DD'] as const
export const TIME_FORMATS = ['auto', '12h', '24h'] as const
export const WEEK_STARTS = ['auto', 'sunday', 'monday'] as const
export type DateFormat = (typeof DATE_FORMATS)[number]
export type TimeFormat = (typeof TIME_FORMATS)[number]
export type WeekStart = (typeof WEEK_STARTS)[number]
