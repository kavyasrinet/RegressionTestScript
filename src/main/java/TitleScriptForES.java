import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;

import java.io.*;
import java.util.ArrayList;

public class TitleScriptForES{
    public static final String ES_IP = "10.12.13.42";
    public static final String ES_CLUSTER_NAME = "s2onlinedemo";
    public static final String ES_INDEX_NAME = "papers_20150302";
    public static final int ES_PORT = 9300;
    public static void main(String[] args) throws Exception {
        if(args.length <1) {
            System.out.println("Not enough arguments");
            System.out.println("Enter the file name of queries");
        }
        //        String fileName = "src/main/resources/TitleQueries.txt";
        //        String  s2Endpoint = "http://beta.s2.allenai.org/api/1/search";
        String fileName = args[0];
        //String s2Endpoint = args[1];
        Client client;

        //Read the file for queries now
        BufferedReader br = new BufferedReader((new FileReader(fileName)));
        String query  = "";
        int total_correct = 0;
        int total = 0;
        ArrayList<String> failedQueries = new ArrayList<>();
        ArrayList<String> failedPapers  =new ArrayList<>();
        PrintWriter ww = new PrintWriter("src/main/resources/failedQueriesLog2p.txt");
        //Initialize client
        client = TitleScriptForES.getESClient();
        while((query = br.readLine())!=null){
            total = total + 1;
            //Scrape the endpoint for this query and fetch the title of first paper
            String title = TitleScriptForES.post(client, QueryBuilderService.queryBuilderAlpha(query));
            if(title==null){
                System.out.println("No results for query: "+query);
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
                System.out.println("No results for query: "+query);
                ww.println("Failed for : "+query);
            }
        }
        ww.close();
        System.out.println("Number of queries for which the test passed : "+total_correct);
        System.out.println("Success rate : "+((total_correct*1.0/total)*100)+" % ");
        if(failedQueries.size()>0) {
            System.out.println("The queries that failed :");
            for (int i = 0; i < failedQueries.size(); i++) {
                System.out.println("Query : " + failedQueries.get(i));
                System.out.println("First paper : " + failedPapers.get(i));

            }
        }
    }
    /**
     * This method checks if the Query matches the title exactly
     * @param query
     * @param title
     * @return
     */
    public static boolean checkPaper(String query, String title){
        String q = QueryParserBase.escape(query.toLowerCase().trim());
        String t = QueryParserBase.escape(title.toLowerCase().trim());
        if(q.equals(t))
            return true;
        else
            return false;
    }

    public static String post(Client client,
                                       QueryBuilder scoredQuery) throws Exception {
        SearchResponse s = client.prepareSearch(ES_INDEX_NAME)
                .setQuery(scoredQuery)
                .setTypes(PaperMeta.PaperElasticSearchType)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFrom(0)
                .setSize(2)
                .setExplain(true)
                .execute()
                .actionGet();
        SearchHit[] searchHits = s.getHits().getHits();
        if(searchHits==null || searchHits.length==0)
            return null;
        String title = searchHits[0].getSource().get("title").toString();
        System.out.println(title);
        return title;
    }

    public static Client getESClient() {
        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", ES_CLUSTER_NAME).build();
        InetSocketTransportAddress address = new InetSocketTransportAddress(ES_IP, ES_PORT);
        return new TransportClient(settings).addTransportAddress(address);
    }
}