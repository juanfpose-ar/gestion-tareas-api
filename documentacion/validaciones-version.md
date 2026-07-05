# Validaciones del módulo de Versiones

Documento que describe todas las reglas de negocio y validaciones aplicadas al crear y editar versiones, con el estado de cobertura en frontend (VersionModal) y backend (VersionServiceImpl + VersionRequest).

---

## Leyenda

| Símbolo | Significado |
|---------|-------------|
| ✅ | Implementado y funcionando |
| ❌ | No implementado — laguna de validación |
| ⚠️ | Implementado parcialmente o con limitaciones |

---

## 1. Campos obligatorios

| Regla | Frontend | Backend | Notas |
|-------|----------|---------|-------|
| `titulo` es obligatorio | ✅ Validado en submit | ✅ `@NotBlank` en VersionRequest → HTTP 400 | |
| `titulo` máximo 100 caracteres | ❌ Sin límite en UI | ⚠️ Solo restricción de columna DB — devuelve HTTP 500 en lugar de 400 | Falta `@Size(max=100)` en VersionRequest |
| `fechaVencimiento` es obligatoria | ✅ Validado en submit | ✅ `@NotBlank` + guard en `parseFecha()` → HTTP 400 | |
| `tableroId` es obligatorio | N/A (viene del contexto) | ✅ `@NotNull` en VersionRequest → HTTP 400 | |

---

## 2. Validación de fecha de vencimiento

| Regla | Frontend | Backend | Notas |
|-------|----------|---------|-------|
| Fecha con formato válido | ✅ Input `type="date"` | ✅ `parseFecha()` lanza error si no parsea | |
| **Nueva versión**: fecha debe ser posterior a hoy | ✅ `min=mañana` en el input + validación en submit | ❌ No implementado | El backend acepta cualquier fecha pasada |
| **Edición**: fecha no puede ser anterior a hoy | ✅ `min=hoy` en el input + validación en submit | ❌ No implementado | Misma laguna |

---

## 3. Creación de una nueva versión

| Regla | Frontend | Backend | Notas |
|-------|----------|---------|-------|
| Estado inicial siempre `POR_HACER` | ✅ No se muestra selector de estado | ✅ Usa `POR_HACER` si `estado` viene nulo | |
| Tickets vinculables: sin versión, en POR_HACER o en EN_CURSO | ✅ Filtro en lista de tickets | ❌ No implementado | El backend acepta cualquier ticket no archivado |

---

## 4. Estado CERRADO

| Regla | Frontend | Backend | Notas |
|-------|----------|---------|-------|
| Título deshabilitado | ✅ Input `disabled` | ⚠️ No hay validación explícita — el backend actualiza el titulo si se envía | |
| Fecha deshabilitada | ✅ Input `disabled` | ⚠️ Idem | |
| Lista de tickets no modificable | ✅ Checkboxes `disabled` | ✅ Lanza error si los ticketIds cambian (mientras el estado siga siendo CERRADO) | La guarda se bypasea si en el mismo request se cambia el estado |
| Solo se puede cambiar el estado | ✅ Solo los botones de estado están activos | ⚠️ Parcial (ver fila anterior) | |
| Transición a `EN_CURSO`: requiere que no haya otra versión en curso | ✅ Botón deshabilitado con tooltip | ✅ Misma validación que para cualquier transición a EN_CURSO | |
| Transición a cualquier otro estado permitida | ✅ Todos los botones habilitados (salvo EN_CURSO con conflicto) | ✅ No hay estado machine — cualquier transición es válida | |

---

## 5. Estado FINALIZADO

| Regla | Frontend | Backend | Notas |
|-------|----------|---------|-------|
| Título editable | ✅ | ✅ | |
| Fecha editable (≥ hoy) | ✅ `min=hoy` + validación submit | ❌ No valida fecha mínima | |
| Tickets vinculables: solo completados sin versión asignada | ✅ Filtro en lista | ⚠️ Solo bloquea tickets *nuevos* no completados; no restringe de qué versión vienen | |
| Transición a `CERRADO`: requiere todos los tickets completados | ✅ Botón bloqueado con candado y contador | ✅ Valida `ticket.isCompletado()` para tickets nuevos al crear/editar | ⚠️ El backend valida `completado` (campo booleano del ticket), el frontend usa `DONE_KW` sobre `estadoNombre` — pueden divergir si el campo `completado` no se actualiza al mover un ticket a un estado "done" |
| Transición a `EN_CURSO`: requiere que no haya otra versión en curso | ✅ Botón deshabilitado con tooltip | ✅ Guard existente para EN_CURSO | |
| Transición a `POR_HACER`: siempre permitida | ✅ | ✅ Sin restricciones | |
| Transición a `EN_CURSO` o `POR_HACER` (retroceso) | ✅ Permitido | ✅ Permitido (sin state machine) | |

---

## 6. Estado EN_CURSO

| Regla | Frontend | Backend | Notas |
|-------|----------|---------|-------|
| Solo puede existir una versión EN_CURSO por tablero | ✅ Validado en submit + botones deshabilitados | ✅ `existsByTableroIdAndEstado` en crear; `existsByTableroIdAndEstadoAndIdNot` en editar | |
| Título editable | ✅ | ✅ | |
| Fecha editable (≥ hoy) | ✅ `min=hoy` + validación submit | ❌ No valida fecha mínima | |
| Tickets vinculables: sin versión o en versión POR_HACER | ✅ Filtro en lista | ❌ No implementado | El backend acepta cualquier ticket |
| Transición a `POR_HACER`: siempre permitida | ✅ | ✅ | |
| Transición a `FINALIZADO`: requiere todos los tickets completados | ✅ Botón bloqueado con candado y contador | ✅ Valida `completado` al crear/editar en FINALIZADO | ⚠️ Misma divergencia `completado` vs `DONE_KW` que en FINALIZADO |
| Transición a `CERRADO`: no permitida | ✅ Botón deshabilitado | ❌ No implementado — el backend acepta esta transición | |

---

## 7. Estado POR_HACER

| Regla | Frontend | Backend | Notas |
|-------|----------|---------|-------|
| Es el estado inicial de toda versión nueva | ✅ | ✅ | |
| Título editable | ✅ | ✅ | |
| Fecha editable (≥ hoy) | ✅ `min=hoy` + validación submit | ❌ No valida fecha mínima | |
| Tickets vinculables: sin versión o en versión EN_CURSO | ✅ Filtro en lista | ❌ No implementado | |
| Transición a `EN_CURSO`: requiere que no haya otra versión en curso | ✅ Botón deshabilitado con tooltip | ✅ Guard existente | |
| Transición a `FINALIZADO`: no permitida | ✅ Botón deshabilitado | ❌ No implementado | |
| Transición a `CERRADO`: no permitida | ✅ Botón deshabilitado | ❌ No implementado | |

---

## 8. Otras reglas generales

| Regla | Frontend | Backend | Notas |
|-------|----------|---------|-------|
| No se puede eliminar una versión CERRADA o EN_CURSO | ❌ Sin guard en UI | ❌ Cualquier versión puede eliminarse | |
| Al eliminar, los tickets vinculados quedan sin versión | N/A | ✅ El servicio desvincula los tickets antes de borrar | |
| Error "entidad no encontrada" devuelve 404 | N/A | ❌ Devuelve HTTP 500 — el handler global captura `com.gestortareas.api.exceptions.EntityNotFoundException` pero el servicio lanza `jakarta.persistence.EntityNotFoundException` | |

---

## 9. Divergencia `completado` vs `DONE_KW`

El frontend determina si un ticket está "completado" usando la función:

```typescript
const DONE_KW = ['hecho', 'terminado', 'done', 'finalizado', 'completado',
                 'cerrado', 'listo', 'resuelto', 'resolved', 'closed', 'finished'];
const isDoneTicket = (t) => DONE_KW.some(kw => t.estadoNombre.toLowerCase().includes(kw));
```

El backend usa el campo `completado: boolean` de la entidad `Ticket`.

Si el campo `completado` no se actualiza automáticamente al mover un ticket a un estado cuyo nombre contiene una de esas palabras clave, las validaciones de frontend y backend divergen. Un ticket puede aparecer como "completado" en el frontend y no serlo para el backend (o viceversa), generando errores al intentar cerrar/finalizar una versión.

**Recomendación:** Actualizar `ticket.completado = true` en el backend cada vez que el ticket se mueva a un estado cuyo nombre coincida con `DONE_KW`, o unificar el criterio en ambos lados.

---

## 10. Resumen de lagunas a resolver en el backend

| Prioridad | Laguna |
|-----------|--------|
| Alta | Fecha de vencimiento sin validación de mínimo (`@FutureOrPresent` o comparación con `LocalDate.now()`) |
| Alta | Divergencia `completado` vs `DONE_KW` — puede causar errores silenciosos al cerrar versiones |
| Media | Transiciones de estado no permitidas (`POR_HACER → FINALIZADO/CERRADO`, `EN_CURSO → CERRADO`) no bloqueadas |
| Media | Restricción de tickets por estado de versión no validada (solo completados para FINALIZADO, solo sin versión o en POR_HACER para EN_CURSO, etc.) |
| Media | Guard de CERRADO bypasseable cambiando estado y tickets en el mismo request |
| Baja | `@Size(max=100)` faltante en `titulo` de VersionRequest |
| Baja | Eliminación de versiones CERRADAS o EN_CURSO no bloqueada |
| Baja | `EntityNotFoundException` de JPA devuelve HTTP 500 en lugar de 404 |
