package ioc.decoupled;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class DaumExchangeRateProvider implements ExchangeRateProvider {

    private static final String DAUM_EXCHANGES_API_URL = "https://finance.daum.net/api/exchanges/FRX.KRWUSD";
    private static final String DAUM_EXCHANGES_URL = "https://finance.daum.net/exchanges/FRX.KRWUSD";

    @Override
    public double getExchangeRate() {
        final var httpURLConnection = connect(DAUM_EXCHANGES_API_URL);
        final var responseBody = readResponseBody(httpURLConnection);
        final var pattern = Pattern.compile("\"basePrice\":(\\d+\\.?\\d*)");
        final var matcher = pattern.matcher(responseBody);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return 0;
    }

    private static String readResponseBody(final HttpURLConnection httpURLConnection) {
        try (final var inputStreamReader = new InputStreamReader(httpURLConnection.getInputStream());
             final var bufferedReader = new BufferedReader(inputStreamReader)) {
            final var builder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static HttpURLConnection connect(final String url) {
        try {
            final var httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("referer", DAUM_EXCHANGES_URL);
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("HTTP 응답 코드: " + responseCode);
            }
            return httpURLConnection;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
