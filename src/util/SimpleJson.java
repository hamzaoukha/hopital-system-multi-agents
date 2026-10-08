package util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleJson {

    public static List<Map<String, String>> lireTableau(String chemin) throws IOException {
        String s = new String(Files.readAllBytes(Paths.get(chemin)), StandardCharsets.UTF_8);
        List<Map<String, String>> lignes = new ArrayList<Map<String, String>>();

        Map<String, String> courant = null;
        StringBuilder buffer = new StringBuilder();
        String cle = null;
        boolean dansChaine = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (dansChaine) {
                if (c == '"') {
                    dansChaine = false;
                } else {
                    buffer.append(c);
                }
                continue;
            }

            switch (c) {
                case '"':
                    dansChaine = true;
                    break;
                case '{':
                    courant = new LinkedHashMap<String, String>();
                    buffer.setLength(0);
                    cle = null;
                    break;
                case ':':
                    cle = buffer.toString().trim();
                    buffer.setLength(0);
                    break;
                case ',':
                    if (courant != null && cle != null) {
                        courant.put(cle, buffer.toString().trim());
                    }
                    buffer.setLength(0);
                    cle = null;
                    break;
                case '}':
                    if (courant != null && cle != null) {
                        courant.put(cle, buffer.toString().trim());
                    }
                    if (courant != null) {
                        lignes.add(courant);
                    }
                    courant = null;
                    buffer.setLength(0);
                    cle = null;
                    break;
                case '[':
                case ']':
                    break;
                default:
                    if (!Character.isWhitespace(c)) {
                        buffer.append(c);
                    }
            }
        }
        return lignes;
    }

    public static void ecrireTableau(String chemin, List<Map<String, String>> lignes) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < lignes.size(); i++) {
            Map<String, String> m = lignes.get(i);
            sb.append("  {");
            int j = 0;
            for (Map.Entry<String, String> e : m.entrySet()) {
                if (j > 0) {
                    sb.append(", ");
                }
                sb.append('"').append(echapper(e.getKey())).append("\": ");
                sb.append('"').append(echapper(e.getValue())).append('"');
                j++;
            }
            sb.append("}");
            if (i < lignes.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]\n");

        PrintWriter pw = new PrintWriter(chemin, "UTF-8");
        try {
            pw.print(sb.toString());
        } finally {
            pw.close();
        }
    }

    private static String echapper(String v) {
        if (v == null) {
            return "";
        }
        return v.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static int entier(Map<String, String> m, String cle, int defaut) {
        try {
            return Integer.parseInt(m.get(cle).trim());
        } catch (Exception e) {
            return defaut;
        }
    }
}
