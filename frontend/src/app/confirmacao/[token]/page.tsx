'use client'

import { useQuery, useMutation } from '@tanstack/react-query'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { api, getErrorMessage } from '@/lib/api'
import { formatCurrency, formatDate, formatTime } from '@/lib/utils'
import { useToast } from '@/hooks/use-toast'
import type { PublicAppointment } from '@/types'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Scissors, Calendar, Clock, User, MapPin, CheckCircle, XCircle, Loader2 } from 'lucide-react'
import { useState } from 'react'

export default function ConfirmacaoPage() {
  const params = useParams()
  const router = useRouter()
  const { toast } = useToast()
  const token = params.token as string
  const [showCancelDialog, setShowCancelDialog] = useState(false)

  const { data: appointment, isLoading, refetch } = useQuery({
    queryKey: ['appointment', token],
    queryFn: async () => {
      const { data } = await api.get<PublicAppointment>(`/api/booking/${token}`)
      return data
    },
  })

  const cancelMutation = useMutation({
    mutationFn: async () => {
      const { data } = await api.post(`/api/booking/${token}/cancel`)
      return data
    },
    onSuccess: () => {
      toast({
        title: 'Agendamento cancelado',
        description: 'Seu agendamento foi cancelado com sucesso.',
      })
      refetch()
      setShowCancelDialog(false)
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro ao cancelar',
        description: getErrorMessage(error),
      })
    },
  })

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    )
  }

  if (!appointment) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-4">
        <XCircle className="h-16 w-16 text-destructive mb-4" />
        <h1 className="text-2xl font-bold mb-2">Agendamento não encontrado</h1>
        <p className="text-slate-500 mb-4">
          O link pode estar incorreto ou o agendamento foi removido.
        </p>
        <Link href="/agendar">
          <Button>Fazer novo agendamento</Button>
        </Link>
      </div>
    )
  }

  const startDate = new Date(appointment.startTime)

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-white border-b">
        <div className="container mx-auto px-4 py-4">
          <Link href="/" className="flex items-center gap-2">
            <Scissors className="h-6 w-6" />
            <span className="font-semibold text-lg">Barbearia</span>
          </Link>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        <div className="max-w-lg mx-auto">
          <div className="text-center mb-8">
            {appointment.canCancel ? (
              <>
                <CheckCircle className="h-16 w-16 text-green-500 mx-auto mb-4" />
                <h1 className="text-2xl font-bold mb-2">Agendamento Confirmado!</h1>
                <p className="text-slate-500">
                  Seu horário está reservado. Guarde este link para gerenciar seu agendamento.
                </p>
              </>
            ) : (
              <>
                <XCircle className="h-16 w-16 text-slate-400 mx-auto mb-4" />
                <h1 className="text-2xl font-bold mb-2">Agendamento Cancelado</h1>
                <p className="text-slate-500">
                  Este agendamento foi cancelado.
                </p>
              </>
            )}
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Detalhes do Agendamento</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <Scissors className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p className="font-medium">{appointment.serviceName}</p>
                  <p className="text-sm text-slate-500">
                    {appointment.serviceDuration} minutos
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <User className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p className="font-medium">{appointment.barberName}</p>
                  <p className="text-sm text-slate-500">Profissional</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <Calendar className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p className="font-medium">{formatDate(startDate)}</p>
                  <p className="text-sm text-slate-500">Data</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <Clock className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <p className="font-medium">{formatTime(startDate)}</p>
                  <p className="text-sm text-slate-500">Horário</p>
                </div>
              </div>

              <div className="pt-4 border-t">
                <div className="flex justify-between items-center">
                  <span className="font-medium">Valor</span>
                  <span className="text-xl font-bold">
                    {formatCurrency(appointment.price)}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>

          {appointment.canCancel && (
            <div className="flex gap-3 mt-6">
              <Link href={`/reagendar/${token}`} className="flex-1">
                <Button variant="outline" className="w-full">
                  Reagendar
                </Button>
              </Link>
              <Dialog open={showCancelDialog} onOpenChange={setShowCancelDialog}>
                <DialogTrigger asChild>
                  <Button variant="destructive" className="flex-1">
                    Cancelar
                  </Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Cancelar Agendamento</DialogTitle>
                    <DialogDescription>
                      Tem certeza que deseja cancelar este agendamento? Esta ação não pode ser desfeita.
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter>
                    <Button
                      variant="outline"
                      onClick={() => setShowCancelDialog(false)}
                    >
                      Voltar
                    </Button>
                    <Button
                      variant="destructive"
                      onClick={() => cancelMutation.mutate()}
                      disabled={cancelMutation.isPending}
                    >
                      {cancelMutation.isPending && (
                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      )}
                      Confirmar Cancelamento
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            </div>
          )}

          <div className="mt-6 text-center">
            <Link href="/agendar">
              <Button variant="link">Fazer novo agendamento</Button>
            </Link>
          </div>
        </div>
      </main>
    </div>
  )
}
