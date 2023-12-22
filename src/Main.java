import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {

        // Conectarse al sitio web
        URL url = new URL("https://cienciasdelsur.com/");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // Obtener el contenido HTML de la página
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        Document doc = Jsoup.parse(content.toString());
        Elements element = doc.select(".td-image-wrap"); //selecciono la clase a inspeccionar para scrapear
        Elements links = element.select("a"); //busco todos los tags <a> que contienen enlaces para poder scrapear luego
        ArrayList<String> enlaces = new ArrayList<>(); //Creo una lista para guardar los enlaces
        for (Element link : links) {
            enlaces.add(link.attr("href"));//guardo los enlaces en una lista
        }
        int i = 4; //inicializo la variable en 4 para iterar la lista
        String texto = ""; //inicializo esta variable para concatenar los articulos
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("EXTRACCION_TEXTOS.txt"), "utf-8"))) { //se crea el archivo para guardar los articulos
            writer.write(texto);
        } catch (IOException ex) {
            System.out.println("error");
        }

        while (i < enlaces.size()) { //ciclo para iterar todos los enlaces y scrapearlos
            URL url2 = new URL(enlaces.get(i)); //se obtienen los url de la lista de enlaces
            HttpURLConnection con2 = (HttpURLConnection) url2.openConnection(); //se realiza la conexion al enlace
            //se lee el contenido del html
            BufferedReader in2 = new BufferedReader(new InputStreamReader(con2.getInputStream()));
            String inputLine2;
            StringBuilder content2 = new StringBuilder();
            while ((inputLine2 = in2.readLine()) != null) {
                content2.append(inputLine2);
            }
            in2.close();
            Document doc2 = Jsoup.parse(content2.toString());
            Element tituloElemento = doc2.selectFirst("h1.entry-title"); //se extrae el titulo del articulo
            String titulo = tituloElemento.text();//se guarda el titulo del articulo
            texto += titulo + "\n"; //se guarda el titulo en esta variable texto para luego concatenar con el articulo
            Element Articulo = doc2.selectFirst("div.td-post-content.tagdiv-type"); //se extrae el articulo de dicha clase
            String articulo = Articulo.text(); //extraigo el articulo del enlace
            texto += articulo + "\n"; //concateno el articulo con su titulo
            //escribo el texto en el archivo
            try {
                FileWriter writer = new FileWriter("EXTRACCION_TEXTOS.txt", StandardCharsets.UTF_8, true);
                writer.write(texto);
                writer.close();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            texto = ""; //vuelvo a reiniciar la variable para el siguente enlace
            i++; //paso al siguente enlace
        }

        //leo el archivo donde estan guardados todos los articulos
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("EXTRACCION_TEXTOS.txt"), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
            line = reader.readLine();
        }
        String fileContent = sb.toString();
        reader.close();

        /* Creo una lista de palabras donde cargo las palabras del archivo, las convierto a minuscula
        y las separo de acuerdo a la siguente expresion regular*/
        String[] words = fileContent.toLowerCase().split("[^\\p{L}\\p{N}]+");

        // Creo un map para contar las repeticiones de cada palabra
        Map<String, Integer> wordCounts = new HashMap<>();
        /* En este ciclo se cuenta el número de veces que aparece cada palabra
        y se agrega en el map tanto la palabra y su numero de apariciones*/
        for (String word : words) {
            Integer count = wordCounts.get(word);
            if (count == null) {
                count = 0;
            }
            wordCounts.put(word, count + 1);
        }

        // Creo un conjunto de palabras a excluir
        Set<String> exclusiones = new HashSet<>(Arrays.asList("de", "la", "y", "en", "el", "que", "del", "a", "se", "un", "es", "no",
                "al", "como", "author", "su", "com", "https", "cienciasdelsur", "o", "e", "lo", "son", "sus", "puede", "desde", "foto", "ser",
                "alejandra", "los", "debe", "sin", "parte", "qué", "pero", "falta", "las", "gran", "te", "por", "sosa", "ha", "donde", "través",
                "si", "galeano", "josé", "ya", "todavia", "vez", "además", "benítez", "about", "min", "related", "posts", "pareció", "compartir",
                "esto", "ni", "así", "ortiz", "cómo", "tener", "todavía", "embargo", "este", "sino", "cual", "le", "sea", "otras", "están", "cantidad",
                "cuando", "diferentes", "pueden", "hacer", "hace", "porque", "cada", "menos", "primer", "una", "para", "con", "más", "también", "sobre",
                "esta", "entre", "fue", "uno", "está", "otros", "5", "forma", "tiene", "hay", "nos", "ejemplo", "the", "estos", "otra", "tienen", "estas", "muy",
                "hasta", "todo", "según", "actualmente", "4", "dos", "acceso", "personas"));

        // Eliminar caracteres especiales, convertir a minúsculas y excluir palabras de la lista de exclusión
        Map<String, Integer> frecuencias = new HashMap<>();
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            String palabra = entry.getKey().replaceAll("[^\\p{L}\\p{N}]+", "").toLowerCase().trim();
            if (!exclusiones.contains(palabra)) {
                frecuencias.put(palabra, entry.getValue());
            }
        }

        // Ordenar por frecuencia las palabras
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(frecuencias.entrySet());
        Collections.sort(sortedEntries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                return e2.getValue().compareTo(e1.getValue());
            }
        });
        //muestra las 50 palabras con sus frecuencias
        for (i = 0; i < 50 && i < sortedEntries.size(); i++) {
            System.out.println(sortedEntries.get(i).getKey() + " : " + sortedEntries.get(i).getValue());
        }
        System.out.println("\n");
        // Guardar los datos ordenados en un archivo nuevo
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("FRECUENCIA_PALABRAS.txt"), StandardCharsets.UTF_8);
        for (i = 0; i < 50 && i < sortedEntries.size(); i++) {
            String l = sortedEntries.get(i).getKey() + " : " + sortedEntries.get(i).getValue();
            writer.write(l);
            writer.write(System.lineSeparator()); // nueva línea
        }
        writer.close();

        //Item 2
        // Conectarse al sitio web
        url = new URL("https://cienciasdelsur.com/");
        con = (HttpURLConnection) url.openConnection();
        // Obtener el contenido HTML de la página
        in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        // Analizar el contenido HTML utilizando JSoup
        doc = Jsoup.parse(content.toString());
        element = doc.select(".td-image-wrap"); //selecciono la clase a inspeccionar para scrapear
        links = element.select("a"); //busco todos los tags <a> que contienen enlaces para poder scrapear luego
        enlaces = new ArrayList<>(); //Creo una lista para guardar los enlaces
        for (Element link : links) {
            enlaces.add(link.attr("href"));//guardo los enlaces en una lista
        }
        i = 4; //inicializo la variable en 4 para iterar la lista
        // Obtener la palabra compuesta ingresada por el usuario
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese su palabra compuesta: ");
        String palabraCompuesta = scanner.nextLine();

        int apariciones = 0; //guardo todas las apariciones de la palabra compuesta

        while (i < enlaces.size()) {
            URL url2 = new URL(enlaces.get(i));
            HttpURLConnection con2 = (HttpURLConnection) url2.openConnection();
            BufferedReader in2 = new BufferedReader(
                    new InputStreamReader(con2.getInputStream()));
            String inputLine2;
            StringBuilder content2 = new StringBuilder();
            while ((inputLine2 = in2.readLine()) != null) {
                content2.append(inputLine2);
            }
            in2.close();
            Document doc2 = Jsoup.parse(content2.toString());
            texto = doc2.text();
            if (texto.contains(palabraCompuesta)) { //verifico si se encuentra la palabra compuesta
                apariciones++;
            }
            i++; //paso al siguente enlace
        }
        //muestro las apariciones si se encontraron
        if (apariciones > 0) {
            System.out.println("Se encontraron " + apariciones + " apariciones de la palabra compuesta: " + palabraCompuesta);
        } else {
            System.out.println("No se encontraron apariciones de la palabra compuesta.");
        }

    }

}
