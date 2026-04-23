package com.pridegym.config;

import com.pridegym.model.*;
import com.pridegym.repository.AlumnoRepository;
import com.pridegym.repository.CuotaRepository;
import com.pridegym.repository.DisciplinaRepository;
import com.pridegym.repository.RutinaRepository;
import com.pridegym.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final DisciplinaRepository disciplinaRepo;
    private final AlumnoRepository alumnoRepo;
    private final CuotaRepository cuotaRepo;
    private final RutinaRepository rutinaRepo;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsuarioRepository usuarioRepo,
                      DisciplinaRepository disciplinaRepo,
                      AlumnoRepository alumnoRepo,
                      CuotaRepository cuotaRepo,
                      RutinaRepository rutinaRepo,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepo = usuarioRepo;
        this.disciplinaRepo = disciplinaRepo;
        this.alumnoRepo = alumnoRepo;
        this.cuotaRepo = cuotaRepo;
        this.rutinaRepo = rutinaRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepo.count() == 0) {
            usuarioRepo.save(new Usuario(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "Administrador Pride Gym"
            ));
        }

        if (disciplinaRepo.count() == 0) {
            Disciplina boxeo = disciplinaRepo.save(new Disciplina("Boxeo", new BigDecimal("15000.00"), "Clases de boxeo amateur y profesional"));
            Disciplina mma = disciplinaRepo.save(new Disciplina("MMA", new BigDecimal("18000.00"), "Artes marciales mixtas"));
            Disciplina muay = disciplinaRepo.save(new Disciplina("Muay Thai", new BigDecimal("15000.00"), "El arte de las ocho extremidades"));
            Disciplina bjj = disciplinaRepo.save(new Disciplina("Brazilian Jiu-Jitsu", new BigDecimal("17000.00"), "Lucha en el suelo y sumisiones"));
            Disciplina kick = disciplinaRepo.save(new Disciplina("Kickboxing", new BigDecimal("14000.00"), "Boxeo con patadas"));
            Disciplina funcional = disciplinaRepo.save(new Disciplina("Funcional", new BigDecimal("12000.00"), "Entrenamiento funcional y acondicionamiento"));

            if (alumnoRepo.count() == 0) {
                Alumno a1 = crearAlumno("Juan", "Perez", "35123456", "juan.perez@mail.com", "3624111222",
                        LocalDate.of(1995, 5, 12), Set.of(boxeo, muay));
                Alumno a2 = crearAlumno("Maria", "Gonzalez", "38456789", "maria.g@mail.com", "3624222333",
                        LocalDate.of(1999, 8, 3), Set.of(kick, funcional));
                Alumno a3 = crearAlumno("Lucas", "Fernandez", "40112233", "lucas.f@mail.com", "3624333444",
                        LocalDate.of(2001, 2, 20), Set.of(mma, bjj));
                Alumno a4 = crearAlumno("Sofia", "Lopez", "42998877", "sofi.lopez@mail.com", "3624444555",
                        LocalDate.of(2003, 11, 7), Set.of(boxeo));
                Alumno a5 = crearAlumno("Martin", "Rodriguez", "33445566", "martin.r@mail.com", "3624555666",
                        LocalDate.of(1990, 1, 15), Set.of(mma, muay, funcional));
                Alumno a6 = crearAlumno("Camila", "Diaz", "41223344", "cami.d@mail.com", "3624666777",
                        LocalDate.of(2002, 6, 25), Set.of(kick));
                Alumno a7 = crearAlumno("Nicolas", "Sosa", "37889900", "nico.sosa@mail.com", "3624777888",
                        LocalDate.of(1997, 9, 18), Set.of(bjj, funcional));
                Alumno a8 = crearAlumno("Valentina", "Torres", "43556677", "vale.torres@mail.com", "3624888999",
                        LocalDate.of(2004, 4, 2), Set.of(boxeo, kick));

                List<Alumno> alumnos = alumnoRepo.saveAll(List.of(a1, a2, a3, a4, a5, a6, a7, a8));

                LocalDate hoy = LocalDate.now();
                int mesActual = hoy.getMonthValue();
                int anioActual = hoy.getYear();

                int mesAnterior = mesActual == 1 ? 12 : mesActual - 1;
                int anioAnterior = mesActual == 1 ? anioActual - 1 : anioActual;

                for (Alumno a : alumnos) {
                    BigDecimal monto = a.getDisciplinas().stream()
                            .map(Disciplina::getPrecioMensual)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Cuota mes anterior - pagada
                    Cuota cuotaAnterior = new Cuota();
                    cuotaAnterior.setAlumno(a);
                    cuotaAnterior.setMes(mesAnterior);
                    cuotaAnterior.setAnio(anioAnterior);
                    cuotaAnterior.setMonto(monto);
                    cuotaAnterior.setFechaVencimiento(LocalDate.of(anioAnterior, mesAnterior, 10));
                    cuotaAnterior.setFechaPago(LocalDate.of(anioAnterior, mesAnterior, 8));
                    cuotaAnterior.setEstado(EstadoCuota.PAGADA);
                    cuotaAnterior.setMetodoPago(MetodoPago.EFECTIVO);
                    cuotaAnterior.setDisciplinas(new HashSet<>(a.getDisciplinas()));
                    cuotaRepo.save(cuotaAnterior);

                    // Cuota mes actual
                    Cuota cuotaActual = new Cuota();
                    cuotaActual.setAlumno(a);
                    cuotaActual.setMes(mesActual);
                    cuotaActual.setAnio(anioActual);
                    cuotaActual.setMonto(monto);
                    cuotaActual.setFechaVencimiento(LocalDate.of(anioActual, mesActual, 10));
                    cuotaActual.setDisciplinas(new HashSet<>(a.getDisciplinas()));

                    // 4 pagadas, 4 pendientes
                    if (a.getId() % 2 == 0) {
                        cuotaActual.setEstado(EstadoCuota.PAGADA);
                        cuotaActual.setFechaPago(LocalDate.of(anioActual, mesActual, 5));
                        cuotaActual.setMetodoPago(MetodoPago.TRANSFERENCIA);
                    } else {
                        cuotaActual.setEstado(EstadoCuota.PENDIENTE);
                    }
                    cuotaRepo.save(cuotaActual);
                }

                // Rutinas demo
                if (rutinaRepo.count() == 0) {
                    // Rutina generica de boxeo - principiante
                    Rutina rBoxPrinc = new Rutina();
                    rBoxPrinc.setNombre("Boxeo Principiante - Tecnica Basica");
                    rBoxPrinc.setDescripcion("Rutina introductoria para alumnos nuevos. Enfoque en postura, jab y guardia.");
                    rBoxPrinc.setNivel(NivelRutina.PRINCIPIANTE);
                    rBoxPrinc.setDisciplina(boxeo);
                    rBoxPrinc.setEjercicios(new ArrayList<>(Arrays.asList(
                            crearEjercicio(1, "Saltar soga", 3, "3 min", "60s", "Calentamiento inicial"),
                            crearEjercicio(2, "Shadow boxing - jab y guardia", 5, "2 min", "60s", "Foco en postura y pie adelantado"),
                            crearEjercicio(3, "Sacos - jab directo", 4, "2 min", "90s", "Mantener guardia alta"),
                            crearEjercicio(4, "Abdominales", 3, "20", "45s", "Crunch tradicional"),
                            crearEjercicio(5, "Plancha", 3, "45 seg", "30s", "Core activado")
                    )));
                    rutinaRepo.save(rBoxPrinc);

                    // Rutina de MMA - intermedio
                    Rutina rMmaInt = new Rutina();
                    rMmaInt.setNombre("MMA Intermedio - Combinaciones");
                    rMmaInt.setDescripcion("Trabajo de combinaciones de golpeo con transicion a clinch y derribo.");
                    rMmaInt.setNivel(NivelRutina.INTERMEDIO);
                    rMmaInt.setDisciplina(mma);
                    rMmaInt.setEjercicios(new ArrayList<>(Arrays.asList(
                            crearEjercicio(1, "Movilidad articular general", 1, "10 min", "-", "Hombros, cadera, rodillas"),
                            crearEjercicio(2, "Shadow - 1-2-codo-rodilla", 5, "3 min", "60s", "Fluidez en la combinacion"),
                            crearEjercicio(3, "Sacos - combinaciones + derribo sombra", 6, "3 min", "90s", "Cambio de nivel realista"),
                            crearEjercicio(4, "Burpees", 4, "15", "45s", "Sin pausa entre reps"),
                            crearEjercicio(5, "Sprawls", 4, "20", "45s", "Explosivo, cadera al piso")
                    )));
                    rutinaRepo.save(rMmaInt);

                    // Rutina personalizada - asignada a un alumno
                    Rutina rPersonalizada = new Rutina();
                    rPersonalizada.setNombre("Plan semanal - " + a5.getApellido());
                    rPersonalizada.setDescripcion("Plan de acondicionamiento personalizado con foco en resistencia.");
                    rPersonalizada.setNivel(NivelRutina.AVANZADO);
                    rPersonalizada.setDisciplina(funcional);
                    rPersonalizada.setAlumno(a5);
                    rPersonalizada.setEjercicios(new ArrayList<>(Arrays.asList(
                            crearEjercicio(1, "Remo ergometro", 1, "2000 m", "-", "Ritmo moderado"),
                            crearEjercicio(2, "Sentadilla con barra", 5, "5", "2 min", "80% 1RM"),
                            crearEjercicio(3, "Peso muerto", 5, "5", "2 min", "Tecnica primero"),
                            crearEjercicio(4, "Dominadas", 4, "Al fallo", "90s", "Asistidas si es necesario"),
                            crearEjercicio(5, "Sprint intervalos", 8, "30 seg", "30s", "Maxima intensidad")
                    )));
                    rutinaRepo.save(rPersonalizada);

                    // Rutina generica de BJJ - principiante
                    Rutina rBjj = new Rutina();
                    rBjj.setNombre("BJJ Principiante - Guardia cerrada");
                    rBjj.setDescripcion("Drills tecnicos basicos desde la guardia cerrada.");
                    rBjj.setNivel(NivelRutina.PRINCIPIANTE);
                    rBjj.setDisciplina(bjj);
                    rBjj.setEjercicios(new ArrayList<>(Arrays.asList(
                            crearEjercicio(1, "Movilidad de cadera - shrimp", 3, "10 m", "30s", "Tecnica lenta"),
                            crearEjercicio(2, "Drill - armbar desde guardia", 4, "10", "60s", "Cada lado"),
                            crearEjercicio(3, "Drill - triangulo desde guardia", 4, "10", "60s", "Cerrar el angulo"),
                            crearEjercicio(4, "Rolling tecnico", 3, "5 min", "2 min", "Sin ganar, solo fluidez")
                    )));
                    rutinaRepo.save(rBjj);
                }
            }
        }
    }

    private Ejercicio crearEjercicio(int orden, String nombre, int series, String reps, String descanso, String notas) {
        Ejercicio e = new Ejercicio();
        e.setOrden(orden);
        e.setNombre(nombre);
        e.setSeries(series);
        e.setRepeticiones(reps);
        e.setDescanso(descanso);
        e.setNotas(notas);
        return e;
    }

    private Alumno crearAlumno(String nombre, String apellido, String dni, String email, String tel,
                                LocalDate fechaNac, Set<Disciplina> disciplinas) {
        Alumno a = new Alumno();
        a.setNombre(nombre);
        a.setApellido(apellido);
        a.setDni(dni);
        a.setEmail(email);
        a.setTelefono(tel);
        a.setFechaNacimiento(fechaNac);
        a.setFechaAlta(LocalDate.now().minusMonths(3));
        a.setContactoEmergencia("Familiar - 362400000");
        a.setActivo(true);
        a.setDisciplinas(new HashSet<>(disciplinas));
        return a;
    }
}
