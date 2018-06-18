/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.*;
import javax.swing.JOptionPane;

/**
 *
 * @author Minato
 */
public class searchPro {

    private static final String REGEX_GET_BLOCK_CONTAINS_PRICE = "h3 class=\"title[\\s|\\S]*?<\\/div>";
    private static final String REGEX_GET_PRICE_IN_BLOCK = "(class=\"price \">\\s*)([0-9.]+)";
    private static final String REGEX_GET_NUMBER_PAGES = "x=\"";
    private static final String REGEX_GET_NUMBER_PAGES_LAST = "\"class";

    // set giá nhỏ nhất để bỏ qua nó (chỉ lấy giá từ 500k trở lên)
    private static final int DEFAULT_PRICE_MIN = 500000;
    static String textArea = "";
    StringBuilder builder = new StringBuilder();

    public StringBuilder searchProduct(String linkPathFile) {
        try {
            FileInputStream fileInPutStream = new FileInputStream(linkPathFile);
            Reader reader = new java.io.InputStreamReader(fileInPutStream, "utf8");
            BufferedReader inputText = new BufferedReader(reader);

            String textFromFileTxt;
            int count = 1;

            while ((textFromFileTxt = inputText.readLine()) != null) {

                if (textFromFileTxt.equals("")) {
                    continue;
                }

                String singleText = textFromFileTxt;

                String symbolSpecial = "!@#$%^&*()_-=+{}[]|\\:;'\",<.>/?`~’";

                for (int i = 0; i < textFromFileTxt.length(); i++) {
                    if (symbolSpecial.contains(singleText.substring(i, i + 1))) {
                        singleText = singleText.replace(singleText.substring(i, i + 1), " ");
                    }
                }

                singleText = singleText.replaceAll("\\s+", " ").toLowerCase();

                if (singleText.contains("–")) {
                    singleText = singleText.replaceAll("–", " ");
                }

                System.out.println(count++ + ": " + textFromFileTxt);

                String textBlockContainsPrice = "";

                int defaultPrice = 100000000, price, sumPages = 0, numberPage = 1;
                do {
                    try {
                        String textLineStream;
                        String textFromStreamURL = "";

                        String encodeSingleText = URLEncoder.encode(singleText.trim(), "UTF-8");

                        encodeSingleText = "https://websosanh.vn/s/" + encodeSingleText;

                        String urlText = encodeSingleText + "?pi=" + (String.valueOf(numberPage)) + ".htm";

                        URL url = new URL(urlText);

                        URLConnection connectURL = url.openConnection();

                        // Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.225.3.1", 3128));
                        // URLConnection connectURL = new URL(urlText).openConnection(proxy);
                        try (BufferedReader inputURL = new BufferedReader(new InputStreamReader(
                                connectURL.getInputStream()))) {
                            while ((textLineStream = inputURL.readLine()) != null) {
                                textFromStreamURL += textLineStream + "\n";

                                if (textFromStreamURL.contains("Xem thêm...")) {
                                    break;
                                }
                            }

                            if (textFromStreamURL.contains("giá từ ")) {
                                textFromStreamURL = textFromStreamURL.replaceAll("giá từ ", "");
                            }

                            Pattern pattern = Pattern.compile(REGEX_GET_BLOCK_CONTAINS_PRICE);
                            Matcher matcher = pattern.matcher(textFromStreamURL.toLowerCase());

                            while (matcher.find()) {
                                // Het Hang
                                if (matcher.group(0).compareTo("out-of-stock") == 0) {
                                    continue;
                                }

                                if (matcher.group(0).contains(textFromFileTxt.toLowerCase().substring(0, textFromFileTxt.indexOf(" ")))) {
                                    textBlockContainsPrice += matcher.group(0);
                                    continue;
                                }

                                String firstText = textFromFileTxt.toLowerCase().substring(0, textFromFileTxt.indexOf(" "));

                                if (textFromFileTxt.toLowerCase().contains(firstText)) {
                                    textBlockContainsPrice += matcher.group(0);
                                }

                            }

                            Pattern pattern1 = Pattern.compile(REGEX_GET_PRICE_IN_BLOCK);
                            Matcher matcher1 = pattern1.matcher(textBlockContainsPrice);

                            while (matcher1.find()) {
                                // Continute if price set = 1
                                if (matcher1.group(2).compareTo("1") == 0) {
                                    continue;
                                }
                                // Get price
                                price = Integer.parseInt(matcher1.group(2).replace(".", ""));

                                if (price <= DEFAULT_PRICE_MIN) {
                                    continue;
                                }

                                if (price < defaultPrice) {
                                    defaultPrice = price;
                                }
                            }

                            // Get sumPages to get all price
                            if (sumPages == 0) {
                                if (textFromStreamURL.lastIndexOf(REGEX_GET_NUMBER_PAGES) != -1) {
                                    sumPages = Integer.parseInt(textFromStreamURL.
                                            substring(textFromStreamURL.lastIndexOf(REGEX_GET_NUMBER_PAGES) + 3, textFromStreamURL.lastIndexOf(REGEX_GET_NUMBER_PAGES_LAST)));
                                }
                            }
                        }

                    } catch (IOException e) {
                        System.out.println(e);
                    }

                    numberPage++;

                } while (numberPage <= sumPages);

                textArea += textFromFileTxt + "\n-----------------------------------------> PriceMin: " + defaultPrice + " đ\n";

                builder.append(textArea);
            }
        } catch (IOException | NumberFormatException e1) {
            System.out.println("Error Input Text.");
        }
        return builder;
    }

    public void output(String linkFIle, String contain) throws IOException {
        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(linkFIle), "UTF-8"))) {
            out.write(contain);
            JOptionPane.showMessageDialog(null, "Done");
        }
    }
}
