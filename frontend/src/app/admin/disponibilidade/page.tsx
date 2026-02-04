'use client'

import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, getErrorMessage } from '@/lib/api'
import { useToast } from '@/hooks/use-toast'
import { useAuthStore } from '@/store/auth'
import type { WorkingHours, TimeBlock, DayOfWeek } from '@/types'
import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Plus, Trash2, Loader2, Clock } from 'lucide-react'

const DAYS_OF_WEEK: { value: DayOfWeek; label: string }[] = [
  { value: 'MONDAY', label: 'Segunda-feira' },
  { value: 'TUESDAY', label: 'Terça-feira' },
  { value: 'WEDNESDAY', label: 'Quarta-feira' },
  { value: 'THURSDAY', label: 'Quinta-feira' },
  { value: 'FRIDAY', label: 'Sexta-feira' },
  { value: 'SATURDAY', label: 'Sábado' },
  { value: 'SUNDAY', label: 'Domingo' },
]

export default function DisponibilidadePage() {
  const { toast } = useToast()
  const queryClient = useQueryClient()
  const { user } = useAuthStore()
  const [blockDialogOpen, setBlockDialogOpen] = useState(false)
  const [blockForm, setBlockForm] = useState({
    startTime: '',
    endTime: '',
    reason: '',
  })

  const { data: workingHours, isLoading: loadingHours } = useQuery({
    queryKey: ['working-hours', user?.id],
    queryFn: async () => {
      const { data } = await api.get<WorkingHours[]>(`/api/admin/me/working-hours`)
      return data
    },
  })

  const { data: timeBlocks, isLoading: loadingBlocks } = useQuery({
    queryKey: ['time-blocks'],
    queryFn: async () => {
      const today = new Date()
      const startDate = format(today, 'yyyy-MM-dd')
      const endDate = format(new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000), 'yyyy-MM-dd')
      const { data } = await api.get<TimeBlock[]>('/api/admin/time-blocks', {
        params: { startDate, endDate },
      })
      return data
    },
  })

  const updateHoursMutation = useMutation({
    mutationFn: async (workingHours: Partial<WorkingHours>[]) => {
      await api.put('/api/admin/me/working-hours', { workingHours })
    },
    onSuccess: () => {
      toast({ title: 'Horários atualizados' })
      queryClient.invalidateQueries({ queryKey: ['working-hours'] })
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro',
        description: getErrorMessage(error),
      })
    },
  })

  const createBlockMutation = useMutation({
    mutationFn: async (data: typeof blockForm & { barberId: string }) => {
      await api.post('/api/admin/time-blocks', data)
    },
    onSuccess: () => {
      toast({ title: 'Bloqueio criado' })
      queryClient.invalidateQueries({ queryKey: ['time-blocks'] })
      setBlockDialogOpen(false)
      setBlockForm({ startTime: '', endTime: '', reason: '' })
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro',
        description: getErrorMessage(error),
      })
    },
  })

  const deleteBlockMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/api/admin/time-blocks/${id}`)
    },
    onSuccess: () => {
      toast({ title: 'Bloqueio removido' })
      queryClient.invalidateQueries({ queryKey: ['time-blocks'] })
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro',
        description: getErrorMessage(error),
      })
    },
  })

  const handleHoursChange = (day: DayOfWeek, field: string, value: string | boolean) => {
    const current = workingHours?.find((h) => h.dayOfWeek === day)
    const updated = {
      dayOfWeek: day,
      startTime: current?.startTime || '09:00',
      endTime: current?.endTime || '18:00',
      isWorking: current?.isWorking ?? true,
      [field]: value,
    }

    const allHours = DAYS_OF_WEEK.map((d) => {
      if (d.value === day) return updated
      const existing = workingHours?.find((h) => h.dayOfWeek === d.value)
      return {
        dayOfWeek: d.value,
        startTime: existing?.startTime || '09:00',
        endTime: existing?.endTime || '18:00',
        isWorking: existing?.isWorking ?? (d.value !== 'SUNDAY'),
      }
    })

    updateHoursMutation.mutate(allHours)
  }

  const handleCreateBlock = (e: React.FormEvent) => {
    e.preventDefault()
    if (user?.id) {
      createBlockMutation.mutate({
        ...blockForm,
        barberId: user.id,
      })
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Disponibilidade</h1>

      {/* Working Hours */}
      <Card>
        <CardHeader>
          <CardTitle>Horário de Trabalho</CardTitle>
          <CardDescription>
            Configure seus horários de expediente por dia da semana
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loadingHours ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin" />
            </div>
          ) : (
            <div className="space-y-4">
              {DAYS_OF_WEEK.map((day) => {
                const hours = workingHours?.find((h) => h.dayOfWeek === day.value)
                const isWorking = hours?.isWorking ?? (day.value !== 'SUNDAY')

                return (
                  <div
                    key={day.value}
                    className="flex items-center gap-4 p-3 rounded-lg border"
                  >
                    <div className="w-32">
                      <span className="font-medium">{day.label}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        checked={isWorking}
                        onChange={(e) =>
                          handleHoursChange(day.value, 'isWorking', e.target.checked)
                        }
                        className="h-4 w-4"
                      />
                      <span className="text-sm text-slate-500">Trabalha</span>
                    </div>
                    {isWorking && (
                      <>
                        <div className="flex items-center gap-2">
                          <Clock className="h-4 w-4 text-slate-400" />
                          <Input
                            type="time"
                            value={hours?.startTime || '09:00'}
                            onChange={(e) =>
                              handleHoursChange(day.value, 'startTime', e.target.value)
                            }
                            className="w-28"
                          />
                          <span>até</span>
                          <Input
                            type="time"
                            value={hours?.endTime || '18:00'}
                            onChange={(e) =>
                              handleHoursChange(day.value, 'endTime', e.target.value)
                            }
                            className="w-28"
                          />
                        </div>
                      </>
                    )}
                  </div>
                )
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Time Blocks */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle>Bloqueios</CardTitle>
            <CardDescription>
              Bloqueie horários específicos (almoço, folgas, etc.)
            </CardDescription>
          </div>
          <Button onClick={() => setBlockDialogOpen(true)}>
            <Plus className="h-4 w-4 mr-2" />
            Novo Bloqueio
          </Button>
        </CardHeader>
        <CardContent>
          {loadingBlocks ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin" />
            </div>
          ) : timeBlocks && timeBlocks.length > 0 ? (
            <div className="space-y-3">
              {timeBlocks.map((block) => (
                <div
                  key={block.id}
                  className="flex items-center justify-between p-3 rounded-lg border"
                >
                  <div>
                    <p className="font-medium">{block.reason}</p>
                    <p className="text-sm text-slate-500">
                      {format(new Date(block.startTime), "dd/MM/yyyy 'às' HH:mm", {
                        locale: ptBR,
                      })}{' '}
                      até{' '}
                      {format(new Date(block.endTime), "dd/MM/yyyy 'às' HH:mm", {
                        locale: ptBR,
                      })}
                    </p>
                  </div>
                  <Button
                    variant="outline"
                    size="icon"
                    onClick={() => deleteBlockMutation.mutate(block.id)}
                    disabled={deleteBlockMutation.isPending}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-center text-slate-500 py-8">Nenhum bloqueio cadastrado</p>
          )}
        </CardContent>
      </Card>

      {/* Block Dialog */}
      <Dialog open={blockDialogOpen} onOpenChange={setBlockDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Novo Bloqueio</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleCreateBlock} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="startTime">Início</Label>
              <Input
                id="startTime"
                type="datetime-local"
                value={blockForm.startTime}
                onChange={(e) => setBlockForm({ ...blockForm, startTime: e.target.value })}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="endTime">Fim</Label>
              <Input
                id="endTime"
                type="datetime-local"
                value={blockForm.endTime}
                onChange={(e) => setBlockForm({ ...blockForm, endTime: e.target.value })}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="reason">Motivo</Label>
              <Input
                id="reason"
                value={blockForm.reason}
                onChange={(e) => setBlockForm({ ...blockForm, reason: e.target.value })}
                placeholder="Ex: Almoço, Folga, Manutenção"
                required
              />
            </div>
            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => setBlockDialogOpen(false)}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={createBlockMutation.isPending}>
                {createBlockMutation.isPending && (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                )}
                Criar
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
