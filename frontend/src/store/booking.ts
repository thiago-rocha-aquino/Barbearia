import { create } from 'zustand'
import type { Service, Barber, TimeSlot } from '@/types'

interface BookingState {
  step: number
  selectedService: Service | null
  selectedBarber: Barber | null
  selectedDate: Date | null
  selectedSlot: TimeSlot | null
  clientData: {
    name: string
    phone: string
    email: string
    notes: string
  }
  setStep: (step: number) => void
  setService: (service: Service | null) => void
  setBarber: (barber: Barber | null) => void
  setDate: (date: Date | null) => void
  setSlot: (slot: TimeSlot | null) => void
  setClientData: (data: Partial<BookingState['clientData']>) => void
  reset: () => void
}

const initialState = {
  step: 1,
  selectedService: null,
  selectedBarber: null,
  selectedDate: null,
  selectedSlot: null,
  clientData: {
    name: '',
    phone: '',
    email: '',
    notes: '',
  },
}

export const useBookingStore = create<BookingState>((set) => ({
  ...initialState,

  setStep: (step) => set({ step }),

  setService: (service) =>
    set({
      selectedService: service,
      selectedSlot: null,
    }),

  setBarber: (barber) =>
    set({
      selectedBarber: barber,
      selectedSlot: null,
    }),

  setDate: (date) =>
    set({
      selectedDate: date,
      selectedSlot: null,
    }),

  setSlot: (slot) => set({ selectedSlot: slot }),

  setClientData: (data) =>
    set((state) => ({
      clientData: { ...state.clientData, ...data },
    })),

  reset: () => set(initialState),
}))
