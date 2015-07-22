import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileReader;
import java.util.ArrayList;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import java.text.Normalizer;


/**
 * This class fetches title queries from an external file, kept in src/main/resources/ and checks
 * if the system has the first paper's title as the title query
 */
public class TitleScript{

    /**
     * This method takes in two arguments:
     * 1. The name of the file containing queries in src/main/resources/
     * 2. The endpoint name along with the method , for now : "http://beta.s2.allenai.org/api/1/search" or relevance
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println ( "##teamcity[testStarted name='TitleScript']" );
        //        String fileName = "src/main/resources/ExtractedTitle.txt";
        //        String  s2Endpoint = "http://beta.s2.allenai.org/api/1/search";
        //        String fault = "src/main/resources/failedQueries.txt";
        String fileName = args[0];
        String s2Endpoint = args[1];
        String fault = args[2];
        //Define template of query
        String queryTemplate  = "{\n \"resultType\": \"complete\",\n \"queryString\": \"QUERY\",\n \"pageSize\": 10,\n \"page\": 1,\n \"sort\": \"relevance\",\n \"classifications\": [],\n \"authors\": [],\n \"venues\": [],\n \"keyPhrases\": []\n }";
        DefaultHttpClient client;

        //Read the file for queries now
        BufferedReader br = new BufferedReader((new FileReader(fileName)));
        String query  = "";
        int total_correct = 0;
        int total = 0;
        ArrayList<String> noResults = new ArrayList<>();
        ArrayList<String> failedQueries = new ArrayList<>();
        ArrayList<String> failedPapers  =new ArrayList<>();
        client = new DefaultHttpClient();
        while((query = br.readLine())!=null){
            total = total + 1;
            //Scrape the endpoint for this query and fetch the title of first paper
            System.out.println(query);
            query = query.replaceAll("\\P{Print}", "").replaceAll("\\\\","").toLowerCase ( ).trim ( ); //remove special characters and backslash
            String title = scrapeTitleOfFirst(query,client,s2Endpoint,queryTemplate);
            if(title==null){
                noResults.add(query);
                //System.out.println("No results for query: "+query);
                continue;
            }
            //If title matches exactly the query
            if(checkPaper(query,title)){
                total_correct = total_correct+1;
            }
            else{
                //Add to failed lists
                failedQueries.add(query);
                failedPapers.add(title);
            }
        }

        System.out.println("##teamcity[testStdOut name='TitleScript' out='"+(escapeMsg("Number of queries for which the test passed : "+total_correct))+"']");
        double successRate = (total_correct*1.0/total)*100;
        System.out.println("##teamcity[testStdOut name='TitleScript' out='"+(escapeMsg("Success rate : "+ successRate +" % "))+"']");
        if(successRate < 80){
            System.out.println("##teamcity[testFailed name='TitleScript'"+ "message='failure message' details='"+(escapeMsg("The Success rate was : "+ successRate +" % "))+"']");

        }
        PrintWriter writer = new java.io.PrintWriter ( fault );
        if(failedQueries.size()>0) {
            writer.println("The queries that failed :");
            writer.println();
            writer.println();
            for (int i = 0; i < failedQueries.size(); i++) {
                writer.println("Query : " + failedQueries.get(i));
                writer.println("First paper : " + failedPapers.get(i));

            }
        }
        writer.println();
        writer.println();
        writer.println("Number of queries with no results: ");
        writer.println();
        if(noResults.size()>0) {
            for (int i = 0; i < noResults.size(); i++) {
                writer.println("Query : " + noResults.get(i));

            }
        }
        writer.close();
        System.out.println("##teamcity[testFinished name='TitleScript']");
    }


    /**
     * This method first makes a HttpPost request to the endpoint with the query template
     * Then fetches the result as JSON and parses it to fetch the text of the title of the first paper
     * and returns it.
     * @param query
     * @param client
     * @param s2Endpoint
     * @param queryTemplate
     * @return
     * @throws java.io.IOException
     */
    public static String scrapeTitleOfFirst(String query, DefaultHttpClient client, String s2Endpoint, String queryTemplate) throws java.io.IOException {
        Gson g = new Gson();
        HttpPost request = new HttpPost(s2Endpoint);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("charset", "UTF-8");
        request.setEntity(new ByteArrayEntity(queryTemplate.replace("QUERY",query).getBytes()));
        HttpEntity content =  client.execute(request).getEntity();
        String cc = EntityUtils.toString(content);
        JsonObject all = g.fromJson(cc, JsonObject.class);
        JsonArray results = g.fromJson(all.getAsJsonArray("results"),JsonArray.class);
        if(results.size()>0) {
            String title = results.get(0).getAsJsonObject().get("title").getAsJsonObject().get("text").toString();
            title = title.replace("\"", "");
            return title;
        }
        else
            return null;

    }

    /**
     * This method checks if the Query matches the title exactly
     * @param query
     * @param title
     * @return
     */
    public static boolean checkPaper(String query, String title){
        //remove special characters and backslashes
        String t = title.replaceAll("\\P{Print}", "").replaceAll("\\\\","").toLowerCase().trim();
        if(t.length()>0 && t.charAt(t.length ()-1)=='.' && query.charAt ( query.length ()-1 )!='.')
            t = t.substring ( 0,t.length()-1 );
        if(query.equals(t))
            return true;
        else
            return false;
    }


    //This function makes the output of the program parseable by Team City
    public static String escapeMsg ( String msg){
        msg
                .replaceAll("\\'", "|'" )
                .replaceAll("\n", "|n" )
                .replaceAll("\\[", "|[")
        .replaceAll("]", "|]");
        return msg;
    }
}

