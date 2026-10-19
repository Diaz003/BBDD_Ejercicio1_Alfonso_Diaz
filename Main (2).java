package com.alfonsodiaz;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/*                     
 *     #########################################################################    
 *                                                                                 
 *                                 EJERCICIOS 1, 2, 3 y 4                          
 *                                                                             
 *     #########################################################################   
 *     #                                                                       #
 *     #    Autor: Alfonso Díaz Morales                Fecha: 14 octu 2026     #                  
 *     #   ------------------------------            ---------------------     #
 *     #                                                                       #
 *     #########################################################################   
 *                                                                              
 *                                   DESCRIPCIÓN:                             
 *                                  --------------                            
 *                                                                             
 *         Listado estricto y recursivo del directorio personal con los datos:
 *          * Nombre (ruta relativa en recursivo)
 *          * Fecha de modificacion
 *          * Propietario
 *          * Tamaño (unidades binarias)
 *          * Indicar solo el nombre si es directorio
 *         Excluyendo ficheros/carpetas ocultos y enlaces simbólicos, 
 *         aplicando límites de resultados y de profundidad máxima.
 *                                                                                                                                                                                                                                                 
 *     #########################################################################    
 */

public class Main {

    private static final String ROJO = "\033[31m";
    private static final String VERDE = "\033[32m";
    private static final String CIAN = "\033[36m";
    private static final String AMARILLO = "\033[33m";
    private static final String RESET = "\033[0m";

    /**
     * MÉTODO PRINCIPAL
     * 
     * Detecta automáticamente la ruta del directorio personal del usuario
     * Inicia un bloque try-catch para manejar errores de lectura de disco
     * Ejecuta el listado estricto pidiendo un límite de resultados
     * Imprime los resultados del listado estricto
     * Ejecuta el listado recursivo pidiendo límite de resultados y profundidad máxima
     * Imprime los resultados del listado recursivo
     */
    public static void main(String[] args) {
        Path directorioPersonal = Path.of(System.getProperty("user.home"));
        Scanner sc = new Scanner(System.in);

        System.out.println(VERDE + "=== INICIO DEL PROGRAMA ===\n" + RESET);
        System.out.println("Directorio personal detectado: " + CIAN + directorioPersonal.toAbsolutePath() + RESET);
        
        try {
            System.out.println(AMARILLO + "\n--- EJERCICIO: Listado Estricto ---" + RESET);
            int maxResultadosEstricto = leerEnteroPositivo(sc, "Introduce el número máximo de resultados a mostrar: ");
            List<String> listadoEstricto = obtenerListadoEstricto(directorioPersonal, maxResultadosEstricto);
            System.out.println("\nResultados del listado estricto:");
            imprimirResultados(listadoEstricto);

            System.out.println(AMARILLO + "\n--- EJERCICIO: Listado Recursivo ---" + RESET);
            int maxResultadosRecursivo = leerEnteroPositivo(sc, "Introduce el número máximo de resultados a mostrar: ");
            int profundidadMaxima = leerEnteroNoNegativo(sc, "Introduce la profundidad máxima de búsqueda: ");
            List<String> listadoRecursivo = obtenerListadoRecursivo(directorioPersonal, profundidadMaxima, maxResultadosRecursivo);
            System.out.println("\nResultados del listado recursivo:");
            imprimirResultados(listadoRecursivo);

        } catch (IOException e) {
            System.err.println(ROJO + "[ERROR] No se pudo acceder al sistema de archivos: " + e.getMessage() + RESET);
        }

        System.out.println(VERDE + "\n=== FIN DEL PROGRAMA ===" + RESET);
        sc.close();
    } 

    /**
     * MÉTODO DE VALIDACIÓN 
     * 
     * Solicita por teclado un número entero estrictamente mayor que cero
     * Utiliza while para atrapar al usuario si introduce letras o números menores o iguales a cero
     * Captura el error y muestra un mensaje en rojo
     * Vuelve a repetir la pregunta hasta obtener un dato válido
     */
    private static int leerEnteroPositivo(Scanner sc, String mensaje) {
        int valor = 0;     
        while (valor <= 0) {
            System.out.print(mensaje);
            try {
                String input = sc.nextLine();
                valor = Integer.parseInt(input);
                if (valor <= 0) {
                    System.out.println(ROJO + "El número debe ser mayor que cero" + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(ROJO + "Esto no es un numero válido" + RESET);
            }
        }
        return valor;
    }

    /**
     * MÉTODO DE VALIDACIÓN
     * 
     * Solicita por teclado un número entero mayor o igual a cero
     * Permite introducir el valor cero para limitar la profundidad al directorio actual
     * Captura el error si se introducen letras o números negativos
     * Muestra un mensaje en rojo y repite la pregunta hasta obtener un dato válido
     */
    private static int leerEnteroNoNegativo(Scanner sc, String mensaje) {
        int valor = -1;     
        while (valor < 0) {
            System.out.print(mensaje);
            try {
                String input = sc.nextLine();
                valor = Integer.parseInt(input);
                if (valor < 0) {
                    System.out.println(ROJO + "La profundidad no puede ser negativa" + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(ROJO + "Esto no es un numero válido" + RESET);
            }
        }
        return valor;
    }

    /**
     * MÉTODO DE LÓGICA 
     * 
     * Abre el directorio y lee sus elementos sin entrar en subcarpetas
     * Descarta directamente los archivos ocultos y los enlaces simbólicos
     * Extrae los datos de los archivos válidos enviándolos a otro método
     * Verifica en cada vuelta si ya se alcanzó el límite máximo de resultados
     */
    public static List<String> obtenerListadoEstricto(Path dir, int maxResults) throws IOException {
        List<String> resultados = new ArrayList<>();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isHidden(entry) || Files.isSymbolicLink(entry)) {
                    continue;
                }
                
                resultados.add(extraerDatosComoTexto(entry, dir));
                
                if (resultados.size() >= maxResults) {
                    break;
                }
            }
        }
        return resultados;
    }

    /**
     * MÉTODO DE LÓGICA 
     * 
     * Navega automáticamente por dentro de las subcarpetas respetando la profundidad máxima
     * Utiliza un SimpleFileVisitor para evaluar cada carpeta y archivo
     * Evita entrar en carpetas ocultas o enlaces devolviendo la instrucción de saltar
     * Detiene la búsqueda por completo cuando alcanza el número máximo de resultados
     */
    public static List<String> obtenerListadoRecursivo(Path root, int maxDepth, int maxResults) throws IOException {
        List<String> resultados = new ArrayList<>();
        
        Files.walkFileTree(root, EnumSet.noneOf(FileVisitOption.class), maxDepth, new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.equals(root)) return FileVisitResult.CONTINUE;
                
                if (Files.isHidden(dir) || Files.isSymbolicLink(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                
                resultados.add(extraerDatosComoTexto(dir, root));
                if (resultados.size() >= maxResults) return FileVisitResult.TERMINATE;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.isHidden(file) || Files.isSymbolicLink(file)) {
                    return FileVisitResult.CONTINUE;
                }
                
                resultados.add(extraerDatosComoTexto(file, root));
                if (resultados.size() >= maxResults) return FileVisitResult.TERMINATE;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
        
        return resultados;
    }

    /**
     * MÉTODO DE LÓGICA
     * 
     * Recibe un archivo y la ruta raíz desde donde empieza la búsqueda
     * Calcula la ruta relativa para mostrar el camino completo de las carpetas
     * Devuelve solo el nombre en color cian si el elemento es un directorio
     * Extrae el dueño limpiando el dominio de Windows si es un archivo
     * Obtiene la fecha de modificación y calcula el tamaño formateado
     * Devuelve una frase juntando todos los datos separados por barras verticales
     */
    private static String extraerDatosComoTexto(Path path, Path root) throws IOException {
        String name = root.relativize(path).toString().replace('\\', '/');
        
        if (Files.isDirectory(path)) {
            return CIAN + name + RESET;
        } else {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            FileOwnerAttributeView ownerView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
            
            String owner = (ownerView != null && ownerView.getOwner() != null) ? ownerView.getOwner().getName() : "desconocido";
            if (owner.contains("\\")) {
                owner = owner.substring(owner.lastIndexOf("\\") + 1);
            }

            LocalDateTime date = LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
            String timeStr = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String sizeStr = formatSize(attrs.size());
            
            return name + " | " + timeStr + " | " + owner + " | " + sizeStr;
        }
    }

    /**
     * MÉTODO DE MATEMÁTICAS
     * 
     * Recibe una cantidad de bytes
     * Utiliza logaritmos para calcular divisiones entre 1024
     * Asigna la letra correspondiente para Kilobytes, Megabytes o Gigabytes
     * Devuelve el texto formateado con dos decimales y estilo español
     */
    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.forLanguageTag("es-ES"), "%.2f %ciB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * MÉTODO DE PRESENTACIÓN
     * 
     * Recibe la lista de textos finales ya montada y preparada
     * Imprime un mensaje de aviso si la lista está vacía
     * Recorre la lista mediante un bucle imprimiendo cada línea por pantalla
     * Se encarga exclusivamente de mostrar datos sin realizar búsquedas
     */
    public static void imprimirResultados(List<String> lista) {
        if (lista.isEmpty()) {
            System.out.println("No se encontraron resultados válidos con los filtros aplicados");
            return;
        }
        
        for (String linea : lista) {
            System.out.println(linea);
        }
    }
}