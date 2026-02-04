'use client'

import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, getErrorMessage } from '@/lib/api'
import { useToast } from '@/hooks/use-toast'
import { formatCurrency } from '@/lib/utils'
import type { Service } from '@/types'

import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Plus, Pencil, Trash2, Loader2, Clock } from 'lucide-react'

export default function ServicosPage() {
  const { toast } = useToast()
  const queryClient = useQueryClient()
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingService, setEditingService] = useState<Service | null>(null)
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    durationMinutes: 30,
    bufferMinutes: 0,
    price: 0,
  })

  const { data: services, isLoading } = useQuery({
    queryKey: ['admin-services'],
    queryFn: async () => {
      const { data } = await api.get<Service[]>('/api/admin/services')
      return data
    },
  })

  const createMutation = useMutation({
    mutationFn: async (data: typeof formData) => {
      await api.post('/api/admin/services', data)
    },
    onSuccess: () => {
      toast({ title: 'Serviço criado com sucesso' })
      queryClient.invalidateQueries({ queryKey: ['admin-services'] })
      setDialogOpen(false)
      resetForm()
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro',
        description: getErrorMessage(error),
      })
    },
  })

  const updateMutation = useMutation({
    mutationFn: async ({ id, data }: { id: string; data: typeof formData }) => {
      await api.put(`/api/admin/services/${id}`, data)
    },
    onSuccess: () => {
      toast({ title: 'Serviço atualizado com sucesso' })
      queryClient.invalidateQueries({ queryKey: ['admin-services'] })
      setDialogOpen(false)
      resetForm()
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro',
        description: getErrorMessage(error),
      })
    },
  })

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/api/admin/services/${id}`)
    },
    onSuccess: () => {
      toast({ title: 'Serviço removido' })
      queryClient.invalidateQueries({ queryKey: ['admin-services'] })
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro',
        description: getErrorMessage(error),
      })
    },
  })

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      durationMinutes: 30,
      bufferMinutes: 0,
      price: 0,
    })
    setEditingService(null)
  }

  const handleEdit = (service: Service) => {
    setEditingService(service)
    setFormData({
      name: service.name,
      description: service.description || '',
      durationMinutes: service.durationMinutes,
      bufferMinutes: service.bufferMinutes || 0,
      price: service.price,
    })
    setDialogOpen(true)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (editingService) {
      updateMutation.mutate({ id: editingService.id, data: formData })
    } else {
      createMutation.mutate(formData)
    }
  }

  const handleNewService = () => {
    resetForm()
    setDialogOpen(true)
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Serviços</h1>
        <Button onClick={handleNewService}>
          <Plus className="h-4 w-4 mr-2" />
          Novo Serviço
        </Button>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin" />
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {services?.map((service) => (
            <Card key={service.id}>
              <CardHeader className="pb-2">
                <div className="flex items-start justify-between">
                  <CardTitle className="text-lg">{service.name}</CardTitle>
                  <Badge variant={service.active ? 'success' : 'secondary'}>
                    {service.active ? 'Ativo' : 'Inativo'}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                {service.description && (
                  <p className="text-sm text-slate-500 mb-3">{service.description}</p>
                )}
                <div className="flex items-center gap-2 text-sm text-slate-500 mb-2">
                  <Clock className="h-4 w-4" />
                  <span>{service.durationMinutes} min</span>
                  {(service.bufferMinutes ?? 0) > 0 && (
                    <span className="text-xs">(+ {service.bufferMinutes} buffer)</span>
                  )}
                </div>
                <div className="flex items-center justify-between mt-4">
                  <span className="text-lg font-bold">{formatCurrency(service.price)}</span>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => handleEdit(service)}
                    >
                      <Pencil className="h-4 w-4" />
                    </Button>
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => deleteMutation.mutate(service.id)}
                      disabled={deleteMutation.isPending}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Service Form Dialog */}
      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editingService ? 'Editar Serviço' : 'Novo Serviço'}
            </DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Nome</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="description">Descrição</Label>
              <Input
                id="description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="duration">Duração (min)</Label>
                <Input
                  id="duration"
                  type="number"
                  min={15}
                  value={formData.durationMinutes}
                  onChange={(e) =>
                    setFormData({ ...formData, durationMinutes: parseInt(e.target.value) })
                  }
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="buffer">Buffer (min)</Label>
                <Input
                  id="buffer"
                  type="number"
                  min={0}
                  value={formData.bufferMinutes}
                  onChange={(e) =>
                    setFormData({ ...formData, bufferMinutes: parseInt(e.target.value) })
                  }
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="price">Preço (R$)</Label>
              <Input
                id="price"
                type="number"
                step="0.01"
                min={0}
                value={formData.price}
                onChange={(e) =>
                  setFormData({ ...formData, price: parseFloat(e.target.value) })
                }
                required
              />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                Cancelar
              </Button>
              <Button
                type="submit"
                disabled={createMutation.isPending || updateMutation.isPending}
              >
                {(createMutation.isPending || updateMutation.isPending) && (
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                )}
                {editingService ? 'Salvar' : 'Criar'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
