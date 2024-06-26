package Backend;

import Frontend.MainFrame;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GenerateCitation {
    
    private MainFrame mainFrame;

    public GenerateCitation(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
    
    private static String getmlaMonthName(int month) {
        String[] monthNames = {"Jan.", "Feb.", "Mar.", "Apr.", "May", "June", "July", "Aug.", "Sept.", "Oct.", "Nov.", "Dec."};
        return monthNames[month - 1];
    }
    
    public void generateArticleCitation(String query) {
        String encodedquery = null;
        try {
            encodedquery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String citationURL = "https://www.mybib.com/api/autocite/search?q="+encodedquery+"&sourceId=article_journal";
        try {
            URL url = new URL(citationURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder responseBuilder = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                responseBuilder.append(output);
            }
            JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
            JSONArray resultsArray = jsonResponse.getJSONArray("results");
            JSONObject result = resultsArray.getJSONObject(0);
            JSONObject metadata = result.getJSONObject("metadata");
            JSONArray authors = metadata.optJSONArray("author");
            StringBuilder authorNames = new StringBuilder();
            if (authors != null) {
                for (int i = 0; i < authors.length(); i++) {
                    JSONObject author = authors.getJSONObject(i);
                    String givenName = author.optString("given", "");
                    String familyName = author.optString("family", "");
                    if (!givenName.isEmpty() || !familyName.isEmpty()) {
                        if (i > 0) {
                            authorNames.append(", ");
                        }
                        authorNames.append(familyName).append(", ").append(givenName);
                    }
                }
            }
            String title = metadata.optString("title", "");
            String containerTitle = metadata.optString("containerTitle", "");
            String volume = metadata.optString("volume", "");
            String issue = metadata.optString("issue", "");
            String page = metadata.optString("page", "");
            String doiValue = metadata.optString("doi", "");
            JSONObject issuedDate = metadata.optJSONObject("issued");
            int year = issuedDate != null ? issuedDate.optInt("year", 0) : 0;
            String aapacitation = authorNames.toString() + " (" + year + "). " + title + ". " + containerTitle + ", " + volume + "(" + issue + "), " + page + ". " + "https://doi.org/" + doiValue;
            String amlacitation = authorNames.toString() + " \"" + title + ".\" " + containerTitle + ", vol. " + volume + ", no. " + issue + ", " + getmlaMonthName(issuedDate.getInt("month")) + " " + year + ", pp. " + page + ", " + "https://doi.org/" + doiValue + ".";
            String aharwardcitation = authorNames.toString() + " (" + year + "). " + title + ". " + containerTitle + ", " + volume + "(" + issue + "), pp." + page + ". doi:" + "https://doi.org/" + doiValue + ".";
            mainFrame.jTextArea3.setText(aapacitation);
            mainFrame.jTextArea4.setText(amlacitation);
            mainFrame.jTextArea5.setText(aharwardcitation);
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}