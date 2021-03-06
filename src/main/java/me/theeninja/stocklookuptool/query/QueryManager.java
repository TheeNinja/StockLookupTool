package me.theeninja.stocklookuptool.query;

import com.google.gson.Gson;
import me.theeninja.stocklookuptool.response.QueryResponse;
import me.theeninja.stocklookuptool.response.Quote;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class QueryManager {

    // Quandl API Key = sweUCDwhgKDxco9dp_xJ
    // Alpha Vantage API Key =  T2ALTQ2SGQIWBT3E
    // Tradier
    // Maximize B2 B Niches App
    // Access token: FNbyAebHEKHISI2arduyLSXsZV89

    private String symbol;

    private final static Logger logger = Logger.getLogger(QueryManager.class.getSimpleName());
    private final static String BASE_URL = "https://query1.finance.yahoo.com/v7/finance/quote";

    private QueryManager(String symbol) {
        this.symbol = symbol;
    }

    private QueryResponse query() {
        try {
            URIBuilder uriBuilder = new URIBuilder(BASE_URL);
            uriBuilder.addParameter("symbols", symbol);

            URL fullUrl = new URL(uriBuilder.toString());
            URLConnection urlConnection = fullUrl.openConnection();

            logger.log(Level.INFO, "Generted URL for query: {0}", fullUrl.toString());

            InputStream is = urlConnection.getInputStream();

            Gson gson = new Gson();
            String jsonString = IOUtils.toString(is, "UTF-8");
            QueryResponse data = gson.fromJson(jsonString, QueryResponse.class);

            IOUtils.closeQuietly(is);

            return data;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getAllSymbols(String stockExchange) {
        try {
            URL url = new URL(URLEncoder.encode("http://www.nasdaq.com/screening/companies-by-industry.aspx?exchange=" + stockExchange + "&render=download", "UTF-8"));

            try (InputStream is = url.openStream()) {

                String csvString = IOUtils.toString(is, "UTF-8");
                return getSymbolsFromCSV(csvString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException | UnsupportedEncodingException malformedURLException) {
            malformedURLException.printStackTrace();
        }
        return null;
    }

    private static List<String> getSymbolsFromCSV(String csvFileContents) {

        List<String> symbols = new ArrayList<>();

        InputStream is = IOUtils.toInputStream(csvFileContents, Charset.defaultCharset());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        try {
            br.readLine(); // disregards first line

            String line;
            while ((line = br.readLine()) != null) {
                String[] lineParts = line.split(",");
                symbols.add(lineParts[0].replace("\"", ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return symbols;
    }

    public static Quote getQuoteOf(String symbol) {
        return new QueryManager(symbol).query().getQuoteResponse().getSingleQuote();
    }
}