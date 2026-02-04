'use client'

import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { api, getErrorMessage } from '@/lib/api'
import { useBookingStore } from '@/store/booking'
import { useToast } from '@/hooks/use-toast'
import { formatCurrency, formatDate, formatTime } from '@/lib/utils'
import type { Service, Barber, TimeSlot, DayAvailability } from '@/types'

// Função para aplicar máscara de telefone brasileiro
function formatPhone(value: string): string {
  const numbers = value.replace(/\D/g, '').slice(0, 11)
  if (numbers.length <= 2) return numbers
  if (numbers.length <= 6) return `(${numbers.slice(0, 2)}) ${numbers.slice(2)}`
  if (numbers.length <= 10) return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 6)}-${numbers.slice(6)}`
  return `(${numbers.slice(0, 2)}) ${numbers.slice(2, 7)}-${numbers.slice(7)}`
}

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { ArrowLeft, ArrowRight, Clock, Scissors, User, Calendar, Check, Loader2 } from 'lucide-react'
import { format, addDays, startOfMonth, endOfMonth, eachDayOfInterval, isSameDay, isToday, isBefore } from 'date-fns'
import { ptBR } from 'date-fns/locale'

export default function AgendarPage() {
  const router = useRouter()
  const { toast } = useToast()
  const {
    step,
    selectedService,
    selectedBarber,
    selectedDate,
    selectedSlot,
    clientData,
    setStep,
    setService,
    setBarber,
    setDate,
    setSlot,
    setClientData,
    reset,
  } = useBookingStore()

  const [currentMonth, setCurrentMonth] = useState(new Date())

  const { data: services, isLoading: loadingServices } = useQuery({
    queryKey: ['services'],
    queryFn: async () => {
      const { data } = await api.get<Service[]>('/api/public/services')
      return data
    },
  })

  const { data: barbers } = useQuery({
    queryKey: ['barbers'],
    queryFn: async () => {
      const { data } = await api.get<Barber[]>('/api/public/barbers')
      return data
    },
  })

  const { data: monthAvailability } = useQuery({
    queryKey: ['monthAvailability', selectedService?.id, selectedBarber?.id, currentMonth],
    queryFn: async () => {
      const { data } = await api.get<DayAvailability[]>('/api/public/availability/month', {
        params: {
          serviceId: selectedService?.id,
          barberId: selectedBarber?.id,
          year: currentMonth.getFullYear(),
          month: currentMonth.getMonth() + 1,
        },
      })
      return data
    },
    enabled: !!selectedService && step >= 3,
  })

  const { data: slots, isLoading: loadingSlots } = useQuery({
    queryKey: ['slots', selectedService?.id, selectedBarber?.id, selectedDate],
    queryFn: async () => {
      const { data } = await api.get<TimeSlot[]>('/api/public/availability/slots', {
        params: {
          serviceId: selectedService?.id,
          barberId: selectedBarber?.id,
          date: format(selectedDate!, 'yyyy-MM-dd'),
        },
      })
      return data
    },
    enabled: !!selectedService && !!selectedDate,
  })

  const createBooking = useMutation({
    mutationFn: async () => {
      const { data } = await api.post('/api/booking', {
        serviceId: selectedService?.id,
        barberId: selectedSlot?.barberId,
        startTime: selectedSlot?.dateTime,
        clientName: clientData.name,
        clientPhone: clientData.phone,
        clientEmail: clientData.email || undefined,
        notes: clientData.notes || undefined,
      })
      return data
    },
    onSuccess: (data) => {
      reset()
      router.push(`/confirmacao/${data.cancellationToken}`)
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro ao agendar',
        description: getErrorMessage(error),
      })
    },
  })

  const handleNext = () => {
    if (step === 1 && selectedService) setStep(2)
    else if (step === 2) setStep(3)
    else if (step === 3 && selectedDate) setStep(4)
    else if (step === 4 && selectedSlot) setStep(5)
    else if (step === 5 && clientData.name && clientData.phone) {
      createBooking.mutate()
    }
  }

  const handleBack = () => {
    if (step > 1) setStep(step - 1)
  }

  const canProceed = () => {
    switch (step) {
      case 1: return !!selectedService
      case 2: return true
      case 3: return !!selectedDate
      case 4: return !!selectedSlot
      case 5: return clientData.name.length >= 2 && clientData.phone.length >= 10
      default: return false
    }
  }

  const days = eachDayOfInterval({
    start: startOfMonth(currentMonth),
    end: endOfMonth(currentMonth),
  })

  const isDayAvailable = (date: Date) => {
    if (isBefore(date, new Date()) && !isToday(date)) return false
    const dateStr = format(date, 'yyyy-MM-dd')
    return monthAvailability?.find(d => d.date === dateStr)?.hasAvailableSlots ?? false
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center gap-2">
            <Scissors className="h-6 w-6" />
            <span className="font-semibold text-lg">Barbearia</span>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto">
          {/* Progress Steps */}
          <div className="mb-8">
            <div className="flex justify-between">
              {[1, 2, 3, 4, 5].map((s) => (
                <div
                  key={s}
                  className={`flex items-center ${s < 5 ? 'flex-1' : ''}`}
                >
                  <div
                    className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${
                      s <= step
                        ? 'bg-primary text-white'
                        : 'bg-slate-200 text-slate-500'
                    }`}
                  >
                    {s < step ? <Check className="h-4 w-4" /> : s}
                  </div>
                  {s < 5 && (
                    <div
                      className={`flex-1 h-1 mx-2 ${
                        s < step ? 'bg-primary' : 'bg-slate-200'
                      }`}
                    />
                  )}
                </div>
              ))}
            </div>
            <div className="flex justify-between mt-2 text-xs text-slate-500">
              <span>Serviço</span>
              <span>Profissional</span>
              <span>Data</span>
              <span>Horário</span>
              <span>Dados</span>
            </div>
          </div>

          {/* Step 1: Service Selection */}
          {step === 1 && (
            <Card>
              <CardHeader>
                <CardTitle>Escolha o Serviço</CardTitle>
                <CardDescription>Selecione o serviço desejado</CardDescription>
              </CardHeader>
              <CardContent>
                {loadingServices ? (
                  <div className="flex justify-center py-8">
                    <Loader2 className="h-8 w-8 animate-spin" />
                  </div>
                ) : (
                  <div className="grid gap-3">
                    {services?.map((service) => (
                      <button
                        key={service.id}
                        onClick={() => setService(service)}
                        className={`p-4 rounded-lg border text-left transition-colors ${
                          selectedService?.id === service.id
                            ? 'border-primary bg-primary/5'
                            : 'border-slate-200 hover:border-slate-300'
                        }`}
                      >
                        <div className="flex justify-between items-start">
                          <div>
                            <h3 className="font-medium">{service.name}</h3>
                            {service.description && (
                              <p className="text-sm text-slate-500 mt-1">
                                {service.description}
                              </p>
                            )}
                            <div className="flex items-center gap-2 mt-2 text-sm text-slate-500">
                              <Clock className="h-4 w-4" />
                              <span>{service.durationMinutes} min</span>
                            </div>
                          </div>
                          <span className="font-semibold">
                            {formatCurrency(service.price)}
                          </span>
                        </div>
                      </button>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          {/* Step 2: Barber Selection */}
          {step === 2 && (
            <Card>
              <CardHeader>
                <CardTitle>Escolha o Profissional</CardTitle>
                <CardDescription>
                  Selecione um profissional ou deixe em branco para qualquer um
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid gap-3">
                  <button
                    onClick={() => setBarber(null)}
                    className={`p-4 rounded-lg border text-left transition-colors ${
                      selectedBarber === null
                        ? 'border-primary bg-primary/5'
                        : 'border-slate-200 hover:border-slate-300'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-slate-200 flex items-center justify-center">
                        <User className="h-5 w-5 text-slate-500" />
                      </div>
                      <div>
                        <h3 className="font-medium">Qualquer profissional</h3>
                        <p className="text-sm text-slate-500">
                          Primeiro horário disponível
                        </p>
                      </div>
                    </div>
                  </button>
                  {barbers?.map((barber) => (
                    <button
                      key={barber.id}
                      onClick={() => setBarber(barber)}
                      className={`p-4 rounded-lg border text-left transition-colors ${
                        selectedBarber?.id === barber.id
                          ? 'border-primary bg-primary/5'
                          : 'border-slate-200 hover:border-slate-300'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                          <User className="h-5 w-5 text-primary" />
                        </div>
                        <h3 className="font-medium">{barber.name}</h3>
                      </div>
                    </button>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Step 3: Date Selection */}
          {step === 3 && (
            <Card>
              <CardHeader>
                <CardTitle>Escolha a Data</CardTitle>
                <CardDescription>
                  Selecione uma data disponível
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between mb-4">
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => setCurrentMonth(addDays(currentMonth, -30))}
                  >
                    <ArrowLeft className="h-4 w-4" />
                  </Button>
                  <span className="font-medium">
                    {format(currentMonth, 'MMMM yyyy', { locale: ptBR })}
                  </span>
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => setCurrentMonth(addDays(currentMonth, 30))}
                  >
                    <ArrowRight className="h-4 w-4" />
                  </Button>
                </div>
                <div className="grid grid-cols-7 gap-1 text-center text-sm">
                  {['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'].map((d) => (
                    <div key={d} className="py-2 font-medium text-slate-500">
                      {d}
                    </div>
                  ))}
                  {Array.from({ length: days[0].getDay() }).map((_, i) => (
                    <div key={`empty-${i}`} />
                  ))}
                  {days.map((day) => {
                    const available = isDayAvailable(day)
                    const selected = selectedDate && isSameDay(day, selectedDate)
                    return (
                      <button
                        key={day.toISOString()}
                        onClick={() => available && setDate(day)}
                        disabled={!available}
                        className={`py-2 rounded-md transition-colors ${
                          selected
                            ? 'bg-primary text-white'
                            : available
                            ? 'hover:bg-slate-100'
                            : 'text-slate-300 cursor-not-allowed'
                        }`}
                      >
                        {format(day, 'd')}
                      </button>
                    )
                  })}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Step 4: Time Selection */}
          {step === 4 && (
            <Card>
              <CardHeader>
                <CardTitle>Escolha o Horário</CardTitle>
                <CardDescription>
                  {selectedDate && format(selectedDate, "EEEE, d 'de' MMMM", { locale: ptBR })}
                </CardDescription>
              </CardHeader>
              <CardContent>
                {loadingSlots ? (
                  <div className="flex justify-center py-8">
                    <Loader2 className="h-8 w-8 animate-spin" />
                  </div>
                ) : (
                  <div className="grid grid-cols-3 sm:grid-cols-4 gap-2">
                    {slots?.filter(s => s.available).map((slot) => (
                      <button
                        key={`${slot.dateTime}-${slot.barberId}`}
                        onClick={() => setSlot(slot)}
                        className={`p-3 rounded-md border text-center transition-colors ${
                          selectedSlot?.dateTime === slot.dateTime &&
                          selectedSlot?.barberId === slot.barberId
                            ? 'border-primary bg-primary/5'
                            : 'border-slate-200 hover:border-slate-300'
                        }`}
                      >
                        <div className="font-medium">{slot.time}</div>
                        {!selectedBarber && (
                          <div className="text-xs text-slate-500 mt-1">
                            {slot.barberName}
                          </div>
                        )}
                      </button>
                    ))}
                  </div>
                )}
                {slots && slots.filter(s => s.available).length === 0 && (
                  <p className="text-center text-slate-500 py-8">
                    Nenhum horário disponível nesta data
                  </p>
                )}
              </CardContent>
            </Card>
          )}

          {/* Step 5: Client Data */}
          {step === 5 && (
            <Card>
              <CardHeader>
                <CardTitle>Seus Dados</CardTitle>
                <CardDescription>
                  Informe seus dados para confirmar o agendamento
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Nome *</Label>
                  <Input
                    id="name"
                    value={clientData.name}
                    onChange={(e) => setClientData({ name: e.target.value })}
                    placeholder="Seu nome completo"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="phone">Telefone *</Label>
                  <Input
                    id="phone"
                    value={clientData.phone}
                    onChange={(e) => setClientData({ phone: formatPhone(e.target.value) })}
                    placeholder="(11) 99999-9999"
                    maxLength={15}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="email">Email (opcional)</Label>
                  <Input
                    id="email"
                    type="email"
                    value={clientData.email}
                    onChange={(e) => setClientData({ email: e.target.value })}
                    placeholder="seu@email.com"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="notes">Observações (opcional)</Label>
                  <Input
                    id="notes"
                    value={clientData.notes}
                    onChange={(e) => setClientData({ notes: e.target.value })}
                    placeholder="Alguma observação?"
                  />
                </div>

                {/* Summary */}
                <div className="mt-6 p-4 bg-slate-100 rounded-lg">
                  <h4 className="font-medium mb-2">Resumo</h4>
                  <div className="space-y-1 text-sm">
                    <p><strong>Serviço:</strong> {selectedService?.name}</p>
                    <p><strong>Profissional:</strong> {selectedSlot?.barberName}</p>
                    <p>
                      <strong>Data:</strong>{' '}
                      {selectedDate && format(selectedDate, "d 'de' MMMM", { locale: ptBR })}
                    </p>
                    <p><strong>Horário:</strong> {selectedSlot?.time}</p>
                    <p><strong>Valor:</strong> {selectedService && formatCurrency(selectedService.price)}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Navigation Buttons */}
          <div className="flex justify-between mt-6">
            <Button
              variant="outline"
              onClick={handleBack}
              disabled={step === 1}
            >
              <ArrowLeft className="h-4 w-4 mr-2" />
              Voltar
            </Button>
            <Button
              onClick={handleNext}
              disabled={!canProceed() || createBooking.isPending}
            >
              {createBooking.isPending ? (
                <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              ) : step === 5 ? (
                <Check className="h-4 w-4 mr-2" />
              ) : (
                <ArrowRight className="h-4 w-4 mr-2" />
              )}
              {step === 5 ? 'Confirmar Agendamento' : 'Continuar'}
            </Button>
          </div>
        </div>
      </main>
    </div>
  )
}
