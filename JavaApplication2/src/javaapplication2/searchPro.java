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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.*;
import javax.swing.JOptionPane;

/**
 *
 * @author minat
 */
public class searchPro {

    private static final String REGEX_GET_BLOCK_CONTAINS_PRICE = "h3 class=\"title[\\s|\\S]*?<\\/div>";
    private static final String REGEX_GET_PRICE_IN_BLOCK = "(class=\"price \">\\s*)([0-9.]+)";
    private static final String REGEX_GET_NUMBER_PAGES = "x=\"";
    private static final String REGEX_GET_NUMBER_PAGES_LAST = "\"class";
    static String textArea = "";

    public String searchProduct(String linkPathFile) {
        try {
            FileInputStream fileInPutStream = new FileInputStream(linkPathFile);
            Reader reader = new java.io.InputStreamReader(fileInPutStream, "utf8");
            BufferedReader inputText = new BufferedReader(reader);

            String textFromFileTxt;

            while ((textFromFileTxt = inputText.readLine()) != null) {

                String singleText = textFromFileTxt;

                String symbolSpecial = "!@#$%^&*()_-=+{}[]|\\:;'\",<.>/?`~’–";

                String tempSingleText = singleText;

                for (int i = 0; i < tempSingleText.length(); i++) {
                    if (symbolSpecial.contains(singleText.substring(i, i + 1))) {
                        singleText = singleText.replace(singleText.substring(i, i + 1), " ");
                    }
                }
                singleText = singleText.replaceAll("\\s+", " ").toLowerCase();

                String textBlockContainsPrice = "";

                int defaultPrice = 100000000, price, sumPages = 0, numberPage = 1;
                do {
                    try {
                        String textLineStream;
                        String textFromStreamURL = "";

                        String encodeSingleText = URLEncoder.encode(singleText, "UTF-8");

                        encodeSingleText = "https://websosanh.vn/s/" + encodeSingleText;

                        String urlText = encodeSingleText + "?pi=" + (String.valueOf(numberPage)) + ".htm";

                        URL url = new URL(urlText);

                        URLConnection connectURL = url.openConnection();

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
                            Matcher matcher = pattern.matcher(textFromStreamURL);
                            
                            while (matcher.find()) {
                                // Het Hang
                                if (matcher.group(0).compareTo("out-of-stock") == 0) {
                                    continue;
                                }
                                textBlockContainsPrice += matcher.group(0);
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

            }
        } catch (Exception e1) {
            System.out.println("Error Input Text.");
        }
        return textArea;
    }

    public void output(String linkFIle, String contain) throws IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(linkFIle), "UTF-8"));

        out.write(contain);
        JOptionPane.showMessageDialog(null, "Done");
        out.close();
    }
}
