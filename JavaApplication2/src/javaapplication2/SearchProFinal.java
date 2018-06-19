/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;

/**
 *
 * @author Minato
 */
public class SearchProFinal {

    private static final String REGEX_GET_BLOCK_CONTAINS_PRICE = "h3 class=\"title[\\s|\\S]*?<\\/div>";
    private static final String REGEX_GET_PRICE_IN_BLOCK = "(class=\"price \">\\s*)([0-9.]+)";
    private static final String REGEX_GET_NUMBER_PAGES = "x=\"";
    private static final String REGEX_GET_NUMBER_PAGES_LAST = "\"class";
    private static final String REGEX_GET_TITLE_PRODUCT = "title=\".+?>\\S+";

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

                if (textFromFileTxt.length() == 0) {
                    continue;
                }

                String firstText = textFromFileTxt.toLowerCase().substring(0, textFromFileTxt.indexOf(" ")).trim();

                boolean checkFirstText = false;

                if (firstText.equals("máy") || firstText.equals("cây") || firstText.equals("hút") || firstText.equals("vòi") || firstText.equals("tủ")) {
                    checkFirstText = true;
                }

                String singleText = textFromFileTxt;

                String symbolSpecial = "!@#$%^&*()_-=+{}[]|\\:;'\",<.>/?`~’";

                int length = textFromFileTxt.length();

                for (int i = 0; i < length; i++) {
                    if (symbolSpecial.contains(singleText.substring(i, i + 1))) {
                        singleText = singleText.replace(singleText.substring(i, i + 1), " ");
                    }
                }

                if (singleText.contains("–")) {
                    singleText = singleText.replaceAll("–", " ");
                }

                singleText = singleText.replaceAll("\\s+", " ").toLowerCase();

                System.out.println(count++ + ": " + textFromFileTxt);

                int defaultPrice = 100000000, price, sumPages = 0, numberPage = 1;
                do {

                    String textLineStream;

                    StringBuilder builderTextFromStreamURL = new StringBuilder();

                    String encodeSingleText = URLEncoder.encode(singleText.trim(), "UTF-8");

                    encodeSingleText = "https://websosanh.vn/s/" + encodeSingleText;

                    String urlText = encodeSingleText + "?pi=" + (String.valueOf(numberPage)) + ".htm";

                       URL url = new URL(urlText);
                       URLConnection connectURL = url.openConnection();
//                     Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("", 1234));
//                     URLConnection connectURL = new URL(urlText).openConnection(proxy);

                    try (BufferedReader inputURL = new BufferedReader(new InputStreamReader(
                            connectURL.getInputStream(), StandardCharsets.UTF_8))) {

                        while ((textLineStream = inputURL.readLine()) != null) {

                            builderTextFromStreamURL.append(textLineStream).append("\n");

                            if (textLineStream.contains("Xem thêm...")) {
                                break;
                            }
                        }

                        String textFromStreamURL = builderTextFromStreamURL.toString();

                        if (textFromStreamURL.contains("giá từ ")) {
                            textFromStreamURL = textFromStreamURL.replaceAll("giá từ ", "");
                        }

                        Pattern pattern = Pattern.compile(REGEX_GET_BLOCK_CONTAINS_PRICE);
                        Matcher matcher = pattern.matcher(textFromStreamURL.toLowerCase());

                        StringBuilder textBlockContainsPrice = new StringBuilder();

                        while (matcher.find()) {

                            // Het Hang
                            if (matcher.group(0).compareTo("out-of-stock") == 0) {
                                continue;
                            }

                            if (checkFirstText) {
                                textBlockContainsPrice.append(matcher.group(0));
                                continue;
                            }

                            Pattern pattern1 = Pattern.compile(REGEX_GET_TITLE_PRODUCT);
                            Matcher matcher1 = pattern1.matcher(matcher.group(0).toLowerCase());

                            while (matcher1.find()) {
                                String lastText = matcher1.group().substring(matcher1.group().lastIndexOf(">") + 1, matcher1.group().length()).trim();

                                if (lastText.equals(firstText)) {
                                    textBlockContainsPrice.append(matcher.group(0));
                                }
                            }

                        }

                        Pattern pattern2 = Pattern.compile(REGEX_GET_PRICE_IN_BLOCK);
                        Matcher matcher2 = pattern2.matcher(textBlockContainsPrice);

                        while (matcher2.find()) {
                            // Continute if price set = 1
                            if (matcher2.group(2).compareTo("1") == 0) {
                                continue;
                            }
                            // Get price
                            price = Integer.parseInt(matcher2.group(2).replace(".", ""));

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

                    numberPage++;

                } while (numberPage <= sumPages);

//                textArea += textFromFileTxt + "\n-----------------------------------------> PriceMin: " + defaultPrice + " đ\n";
                System.out.println("-----------------------------------------> PriceMin: " + defaultPrice + " đ");

            }
        } catch (IOException | NumberFormatException e1) {
            System.out.println(e1);
        }
        return builder;
    }

}
