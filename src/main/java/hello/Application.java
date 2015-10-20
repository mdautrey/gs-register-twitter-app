package hello;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.web.client.RestTemplate;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application {

    public static void main(String[] args) {
        String appId = promptForInput("Enter your Consumer ID:");
        String appSecret = promptForInput("Enter your Consumer Secret:");
        String message = promptForInput("Enter your status:");
        String appToken = fetchApplicationAccessToken(appId, appSecret);
        List<Tweet> tweets = searchTwitter("#springframework", appToken);
        for (Tweet tweet : tweets) {
            System.out.println(tweet.getText());
        }
        try {
            sendTweet(message);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    private static List<Tweet> searchTwitter(String query, String appToken) {
        // Twitter supports OAuth2 *only* for obtaining an application token, not for user tokens.
        // Using application token for search so that we don't have to go through hassle of getting a user token.
        // This is not (yet) supported by Spring Social, so we must construct the request ourselves.
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + appToken);
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        Map<String, ?> result = rest.exchange("https://api.twitter.com/1.1/search/tweets.json?q={query}", HttpMethod.GET, requestEntity, Map.class, query).getBody();
        List<Map<String, ?>> statuses = (List<Map<String, ?>>) result.get("statuses");
        List<Tweet> tweets = new ArrayList<Tweet>();
        for (Map<String, ?> status : statuses) {
            tweets.add(new Tweet(Long.valueOf(status.get("id").toString()), status.get("text").toString()));
        }
        return tweets;
    }

    private static void sendTweet (String message) throws TwitterException {
        // http://www.codingpedia.org/ama/how-to-post-to-twittter-from-java-with-twitter4j-in-10-minutes/
        Twitter twitter = TwitterFactory.getSingleton();
        Status status = twitter.updateStatus(message);
        System.out.println("Successfully updated status to " + status.getText());


    }

    private static String fetchApplicationAccessToken(String appId, String appSecret) {
        // Twitter supports OAuth2 *only* for obtaining an application token, not for user tokens.
        OAuth2Operations oauth = new OAuth2Template(appId, appSecret, "", "https://api.twitter.com/oauth2/token");
        return oauth.authenticateClient().getAccessToken();
    }
    
    private static String promptForInput(String promptText) {
        return JOptionPane.showInputDialog(promptText + " ");
    }
    
    private static final class Tweet {
        private long id;

        private String text;
        
        public Tweet(long id, String text) {
            this.id = id;
            this.text = text;
        }
        
        public long getId() {
            return id;
        }
        
        public String getText() {
            return text;
        }
    }

}
