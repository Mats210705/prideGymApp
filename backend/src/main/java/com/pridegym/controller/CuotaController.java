package com.pridegym.controller;

import com.pridegym.dto.GenerarCuotasRequest;
import com.pridegym.dto.PagoRequest;
import com.pridegym.model.Alumno;
import com.pridegym.model.Cuota;
import com.pridegym.model.Disciplina;
import com.pridegym.model.EstadoCuota;
import com.pridegym.repository.AlumnoRepository;
import com.pridegym.repository.CuotaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cuotas")
public class CuotaController {

    private final CuotaRepository cuotaRepo;
    private final AlumnoRepository alumnoRepo;

    public CuotaController(CuotaRepository cuotaRepo, AlumnoRepository alumnoRepo) {
        this.cuotaRepo = cuotaRepo;
        this.alumnoRepo = alumnoRepo;
    }

    @GetMapping
    public List<Cuota> list(@RequestParam(required = false) Integer mes,
                            @RequestParam(required = false) Integer anio,
                            @RequestParam(required = false) EstadoCuota estado,
                            @RequestParam(required = false) String texto) {
        actualizarVencidas();
        String textoNorm = (texto != null && !texto.isBlank()) ? texto.trim() : null;
        return cuotaRepo.findFiltered(mes, anio, estado, textoNorm);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cuota> get(@PathVariable Long id) {
        return cuotaRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<Cuota> pagar(@PathVariable Long id, @RequestBody PagoRequest req) {
        return cuotaRepo.findById(id).map(c -> {
            c.setEstado(EstadoCuota.PAGADA);
            c.setFechaPago(req.getFechaPago() != null ? req.getFechaPago() : LocalDate.now());
            c.setMetodoPago(req.getMetodoPago());
            c.setObservaciones(req.getObservaciones());
            return ResponseEntity.ok(cuotaRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/revertir-pago")
    public ResponseEntity<Cuota> revertir(@PathVariable Long id) {
        return cuotaRepo.findById(id).map(c -> {
            c.setFechaPago(null);
            c.setMetodoPago(null);
            c.setEstado(c.getFechaVencimiento().isBefore(LocalDate.now())
                    ? EstadoCuota.VENCIDA : EstadoCuota.PENDIENTE);
            return ResponseEntity.ok(cuotaRepo.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/generar-mes")
    public ResponseEntity<?> generarMes(@RequestBody GenerarCuotasRequest req) {
        if (req.getMes() == null || req.getAnio() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "mes y anio son requeridos"));
        }
        LocalDate venc = req.getFechaVencimiento() != null
                ? req.getFechaVencimiento()
                : LocalDate.of(req.getAnio(), req.getMes(), 10);

        List<Alumno> activos = alumnoRepo.findByActivoTrue();
        List<Cuota> existentes = cuotaRepo.findByMesAndAnio(req.getMes(), req.getAnio());
        List<Cuota> creadas = new ArrayList<>();

        for (Alumno a : activos) {
            boolean yaTiene = existentes.stream().anyMatch(c -> c.getAlumno().getId().equals(a.getId()));
            if (yaTiene) continue;
            BigDecimal monto = a.getDisciplinas().stream()
                    .map(Disciplina::getPrecioMensual)
                    .filter(p -> p != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (monto.compareTo(BigDecimal.ZERO) == 0) continue;

            Cuota c = new Cuota();
            c.setAlumno(a);
            c.setMes(req.getMes());
            c.setAnio(req.getAnio());
            c.setMonto(monto);
            c.setFechaVencimiento(venc);
            c.setEstado(EstadoCuota.PENDIENTE);
            creadas.add(cuotaRepo.save(c));
        }

        return ResponseEntity.ok(Map.of(
                "creadas", creadas.size(),
                "existentes", existentes.size(),
                "alumnosActivos", activos.size()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!cuotaRepo.existsById(id)) return ResponseEntity.notFound().build();
        cuotaRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) Integer mes,
                                              @RequestParam(required = false) Integer anio,
                                              @RequestParam(required = false) EstadoCuota estado,
                                              @RequestParam(required = false) String texto) throws IOException {
        actualizarVencidas();
        String textoNorm = (texto != null && !texto.isBlank()) ? texto.trim() : null;
        List<Cuota> cuotas = cuotaRepo.findFiltered(mes, anio, estado, textoNorm);

        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                          "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Cuotas");

            // Estilos
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle moneyStyle = wb.createCellStyle();
            DataFormat fmt = wb.createDataFormat();
            moneyStyle.setDataFormat(fmt.getFormat("\"$\" #,##0.00"));

            CellStyle totalStyle = wb.createCellStyle();
            Font totalFont = wb.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);
            totalStyle.setDataFormat(fmt.getFormat("\"$\" #,##0.00"));
            totalStyle.setBorderTop(BorderStyle.THIN);

            CellStyle totalLabelStyle = wb.createCellStyle();
            totalLabelStyle.setFont(totalFont);
            totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalLabelStyle.setBorderTop(BorderStyle.THIN);

            // Titulo
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            String periodo = (mes != null ? meses[mes - 1] + " " : "")
                    + (anio != null ? anio : "Todos los periodos");
            titleCell.setCellValue("Pride Gym - Cuotas " + periodo);
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // Headers
            String[] headers = {"Apellido", "Nombre", "DNI", "Periodo", "Monto",
                                "Vencimiento", "Estado", "Fecha de pago", "Metodo de pago"};
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Datos
            int rowIdx = 3;
            BigDecimal totalMonto = BigDecimal.ZERO;
            BigDecimal totalPagado = BigDecimal.ZERO;
            int cantPagadas = 0, cantPendientes = 0, cantVencidas = 0;

            for (Cuota c : cuotas) {
                Row row = sheet.createRow(rowIdx++);
                Alumno a = c.getAlumno();
                row.createCell(0).setCellValue(a != null ? a.getApellido() : "");
                row.createCell(1).setCellValue(a != null ? a.getNombre() : "");
                row.createCell(2).setCellValue(a != null ? a.getDni() : "");
                row.createCell(3).setCellValue(meses[c.getMes() - 1] + " " + c.getAnio());

                Cell montoCell = row.createCell(4);
                montoCell.setCellValue(c.getMonto() != null ? c.getMonto().doubleValue() : 0);
                montoCell.setCellStyle(moneyStyle);

                row.createCell(5).setCellValue(c.getFechaVencimiento() != null
                        ? c.getFechaVencimiento().format(df) : "");
                row.createCell(6).setCellValue(c.getEstado() != null ? c.getEstado().name() : "");
                row.createCell(7).setCellValue(c.getFechaPago() != null
                        ? c.getFechaPago().format(df) : "");
                row.createCell(8).setCellValue(c.getMetodoPago() != null
                        ? c.getMetodoPago().name() : "");

                if (c.getMonto() != null) totalMonto = totalMonto.add(c.getMonto());
                if (c.getEstado() == EstadoCuota.PAGADA) {
                    cantPagadas++;
                    if (c.getMonto() != null) totalPagado = totalPagado.add(c.getMonto());
                } else if (c.getEstado() == EstadoCuota.PENDIENTE) {
                    cantPendientes++;
                } else if (c.getEstado() == EstadoCuota.VENCIDA) {
                    cantVencidas++;
                }
            }

            // Totales
            rowIdx++;
            Row totalRow = sheet.createRow(rowIdx++);
            Cell labelCell = totalRow.createCell(3);
            labelCell.setCellValue("TOTAL FILTRADO:");
            labelCell.setCellStyle(totalLabelStyle);
            Cell totalCell = totalRow.createCell(4);
            totalCell.setCellValue(totalMonto.doubleValue());
            totalCell.setCellStyle(totalStyle);

            Row cobradoRow = sheet.createRow(rowIdx++);
            Cell cobLabel = cobradoRow.createCell(3);
            cobLabel.setCellValue("YA COBRADO:");
            cobLabel.setCellStyle(totalLabelStyle);
            Cell cobVal = cobradoRow.createCell(4);
            cobVal.setCellValue(totalPagado.doubleValue());
            cobVal.setCellStyle(totalStyle);

            rowIdx++;
            sheet.createRow(rowIdx++).createCell(0).setCellValue("Resumen:");
            sheet.createRow(rowIdx++).createCell(0).setCellValue("  Pagadas: " + cantPagadas);
            sheet.createRow(rowIdx++).createCell(0).setCellValue("  Pendientes: " + cantPendientes);
            sheet.createRow(rowIdx++).createCell(0).setCellValue("  Vencidas: " + cantVencidas);
            sheet.createRow(rowIdx++).createCell(0).setCellValue("  Total: " + cuotas.size());

            // Ajustar columnas
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            wb.write(out);

            String fileName = "cuotas"
                    + (anio != null ? "-" + anio : "")
                    + (mes != null ? "-" + String.format("%02d", mes) : "")
                    + ".xlsx";

            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            h.setContentDispositionFormData("attachment", fileName);
            return ResponseEntity.ok().headers(h).body(out.toByteArray());
        }
    }

    private void actualizarVencidas() {
        LocalDate hoy = LocalDate.now();
        List<Cuota> pendientes = cuotaRepo.findByEstado(EstadoCuota.PENDIENTE);
        for (Cuota c : pendientes) {
            if (c.getFechaVencimiento() != null && c.getFechaVencimiento().isBefore(hoy)) {
                c.setEstado(EstadoCuota.VENCIDA);
                cuotaRepo.save(c);
            }
        }
    }
}
