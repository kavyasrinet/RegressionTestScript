import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.*;

/**
 * This class generates 1000 title queries for an index
 * given the metadata file and the citation edges file
 */
public class GenerateTitleQueries{

    /**
     * This method takes in the relative path of metadata file,
     * the citation edges file and the output file to write the results to.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        if(args.length<3){
            System.out.println("Not enough arguments");
            System.out.println("Enter arguments in the sequence: metadata_file citationEdges_file output_file");
        }
        //Take in inputs. For now the relative path is as commented
        String metadataFile = args[0];//"src/main/resources/metadata.json";
        String citationEdgesFile = args[1];//"src/main/resources/citationEdges.txt";
        String TitleFile = args[2];
        //Populate the maps containing id as the key and value as:
        //Title, year and citations respectively
        HashMap<String , PaperAttributes> idMap = getAttributes(metadataFile);
        LinkedHashMap<String, String> titleMap = getMap(idMap,0);
        LinkedHashMap<String, Integer> yearMap = getMap(idMap,1);
        yearMap = getSorted(yearMap);
        LinkedHashMap<String, Integer> citationMap = getCitations(citationEdgesFile);
        citationMap = getSorted(citationMap);

        //Now fetch the papers
        HashSet<String> all = new HashSet<>();
        all.addAll(fetchPapers(citationMap, 700));
        all.addAll(fetchPapers(yearMap,500));
        HashSet<String> titles = new HashSet<>();
        for(String s: all){
            titles.add(titleMap.get(s));
            if(titles.size()==1000)
                break;
        }
        //Write to output file
        PrintWriter writer = new PrintWriter(TitleFile);
        for(String title: titles)
            writer.println(title);
        writer.close();
    }

    /**
     * This method takes in the citation edges file and
     * returns back a hashmap containing id as the key and number of citations as the value
     * @param fileName
     * @return
     * @throws IOException
     */
    public static LinkedHashMap<String, Integer> getCitations(String fileName) throws IOException {
        LinkedHashMap<String , Integer> citationMap = new LinkedHashMap<>();
        BufferedReader br = new BufferedReader((new FileReader(fileName)));
        String line;
        while((line=  br.readLine())!=null){
            String[] ids = line.split("\t");
            String paper1 = ids[1];
            if(!citationMap.containsKey(paper1)){
                citationMap.put(paper1,1);
            }
            else
                citationMap.put(paper1,citationMap.get(paper1)+1);
        }

        return citationMap;
    }

    /**
     * This method takes in the metadata file and returns back
     * a hashmap that has the id as the key and an object containing title
     * and year of the paper
     * @param fileName
     * @return
     * @throws IOException
     */
    public static HashMap<String , PaperAttributes> getAttributes(String fileName) throws IOException {
        HashMap<String , PaperAttributes> idMap = new HashMap<>();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileName), "UTF8"));
        String line  = "";
        Gson g;
        PaperAttributes pa;
        while((line = br.readLine())!=null){
            g = new Gson();
            JsonObject all = g.fromJson(line, JsonObject.class);
            String title = all.get("title").toString().replace("\"", "");
            String id = all.get("id").toString().replace("\"","");
            int year = Integer.parseInt(all.get("year").toString());
            pa = new PaperAttributes();
            pa.setTitle(title);
            pa.setYear(year);
            idMap.put(id,pa);
        }
        return idMap;
    }

    /**
     * This method extracts either just the title or just the year
     * and returns the hashmap
     * @param idMap
     * @param i
     * @param <T>
     * @return
     */
    public static <T>  LinkedHashMap<String, T> getMap(HashMap<String , PaperAttributes> idMap, int i){
        if(i==0){
            LinkedHashMap<String, String> titleMap = new LinkedHashMap<>();
            for(String id: idMap.keySet()){
                String title = idMap.get(id).getTitle();
                titleMap.put(id,title);
            }
            return (LinkedHashMap<String, T>) titleMap;
        }
        else{
            LinkedHashMap<String, Integer> yearMap = new LinkedHashMap<>();
            for(String id: idMap.keySet()){
                int year = idMap.get(id).getYear();
                yearMap.put(id,year);
            }
            return (LinkedHashMap<String, T>) yearMap;
        }
    }

    /**
     * This method takes in a HashMap that has an Integer value and returns
     * back a HashMap sorted by value
     * @param map
     * @return
     */
    public static LinkedHashMap<String, Integer> getSorted(HashMap<String, Integer> map){
        ArrayList<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return b.getValue().compareTo(a.getValue());
            }
        });
        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    /**
     * This method gets the top number papers from
     * a sorted hashmap and returns the same
     * @param map
     * @param number
     * @param <T>
     * @return
     */
    public static <T> HashSet<String> fetchPapers(LinkedHashMap<String, T> map, int number){
        HashSet<String> set = new HashSet<>();
        int i  =0;
        for(String id: map.keySet()){
            i= i+1;
            set.add(id);
            if(i>=number)
                return set;

        }
        return set;
    }
}


/**
 * This class has paper attributes like
 * title and year.
 */
class PaperAttributes{
    private String title;
    private int year;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }



}