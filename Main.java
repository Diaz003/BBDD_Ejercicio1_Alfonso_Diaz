package com.alfonsodiaz;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/*                     
 *     #########################################################################    
 *                                                                                 
 *                                     EJERCICIOS 1, 2 y 3                           
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
 *         Listado no recursivo del directorio personal con los datos:
 *          * Nombre
 *          * Fecha de modificacion
 *          * Propietario
 *          * Tamaño 
 *          * Indicar solo el nombre si es directorio
 *         Excluyendo ficheros ocultos y enlaces simbólicos, y aplicando
 *         un límite máximo de resultados
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
     * Detecta automáticamente la ruta del directorio personal del usuario.
     * Inicia un bloque try-catch para manejar posibles errores al leer el disco duro.
     * Pide al usuario el número máximo de resultados usando un método seguro que evita cuelgues
     * Llama a la lógica del programa para recolectar y formatear los datos de los archivos
     * Llama a la presentación para imprimir esa lista de datos por pantalla
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

        } catch (IOException e) {
            System.err.println(ROJO + "[ERROR] No se pudo acceder al sistema de archivos: " + e.getMessage() + RESET);
        }

        System.out.println(VERDE + "\n=== FIN DEL PROGRAMA ===" + RESET);
        sc.close();
    } 

    /**
     * MÉTODO DE VALIDACIÓN 
     * 
     * Solicita por teclado un número entero mayor que cero.
     * Utiliza while para atrapar al usuario, pero si introduce letras, decimales,
     * ceros o números negativos, el programa captura el error o la condición ,
     * mostrara un mensaje en rojo y vuelve a repetir la pregunta hasta obtener un dato válido
     */
    private static int leerEnteroPositivo(Scanner sc, String mensaje) {
        int valor = 0;     
        while (valor <= 0) {
            System.out.print(mensaje);
            try {
                String input = sc.nextLine();
                valor = Integer.parseInt(input);
                if (valor <= 0) {
                    System.out.println(ROJO + "El número es negativo o cero" + RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(ROJO + "Esto no es un numero" + RESET);
            }
        }
        return valor;
    }

    /**
     * MÉTODO DE LÓGICA 
     * 
     * Abre el directorio indicado y lee sus elementos de forma plana
     * Descarta directamente los archivos que están ocultos o que son enlaces simbólicos
     * Envía cada archivo válido a otro método para convertir sus datos en un texto, y lo guarda en una lista
     * Verifica en cada vuelta si ya ha alcanzado el límite máximo de resultados para detener la búsqueda
     */
    public static List<String> obtenerListadoEstricto(Path dir, int maxResults) throws IOException {
        List<String> resultados = new ArrayList<>();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (Files.isHidden(entry) || Files.isSymbolicLink(entry)) {
                    continue;
                }
                
                resultados.add(extraerDatosComoTexto(entry));
                
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
     * Analiza un archivo o carpeta individual
     * Si es una carpeta, simplemente devuelve su nombre coloreado en cian.
     * Si es un archivo, usa las herramientas de Java para extraer su propietario junto con
     * su fecha de modificación formateada y su tamaño formateado
     * Por ultimo devuelve una única frasejuntando todos estos datos separados por barras verticales
     */
    private static String extraerDatosComoTexto(Path path) throws IOException {
        String name = path.getFileName().toString();
        
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
     * Recibe una cantidad de bytes y utiliza logaritmos para saber si debe dividir
     * entre 1024 para mostrar Kilobytes, Megabytes o Gigabytes
     * Devuelve un texto con la cantidad calculada, dos decimales, formato español 
     * y la letra correspondiente
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
     * Recibe la lista de textos finales ya montada y preparada.
     * Si la lista está vacía, imprime un mensaje de aviso
     * Si tiene datos, la recorre mediante un bucle 'for' imprimiendo cada línea por pantalla.
     * No realiza ninguna lógica de búsqueda, solo imprime
     */
    public static void imprimirResultados(List<String> lista) {
        if (lista.isEmpty()) {
            System.out.println("No se encontraron resultados válidos con los filtros aplicados.");
            return;
        }
        
        for (String linea : lista) {
            System.out.println(linea);
        }
    }
}