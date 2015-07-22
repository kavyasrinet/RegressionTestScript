import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * This class uses the keyPhrases.tsv file to extract single topic queries
 * and combination-of-two-topic queries.
 */

public class ExtractQueries {
	/**
	 * This method reads the tab separated data from the file : "src/main/resources/keyphrases.tsv"
	 * and constructs single topic queries in : "src/main/resources/broadTopics.txt"
	 * and combination of two broad topics , queries in : "src/main/resources/combinationOfBroadTopics.txt"
	 * and makes sure there are no overlaps between these and the previous queries (in  Queries.txt)
	 *
	 * @param args
	 *
	 * @throws IOException
	 */
	public static void main ( String[] args ) throws IOException {
		HashSet< String > broadTopics = new HashSet<> ( );
		HashSet< String > twoCombination = new HashSet<> ( );
		HashSet< String > queries = new HashSet<> ( );
		// Get the already existing queries
		BufferedReader br = new BufferedReader ( new FileReader ( "src/main/resources/Queries.txt" ) );
		String line;
		while ( ( line = br.readLine ( ) ) != null )
			queries.add ( line.trim ( ).toLowerCase () );
		PrintWriter writer = new PrintWriter ( "src/main/resources/broadTopics.txt" );
		// Get the keyphrases per paper
		HashMap< String, ArrayList< String > > result = getDataFromFile ( "src/main/resources/keyphrases.tsv" );
		for ( String paperId : result.keySet ( ) ) {
			ArrayList< String > keyPhrases = result.get ( paperId );
			Random generator = new Random ( );
			int i = generator.nextInt ( keyPhrases.size ( ) );
			// Pick a random phrase per paper as the single topic query
			broadTopics.add ( keyPhrases.get ( i ) );
			// Get all possible pairs from the keyphrases and pick a random pair
			ArrayList< Pair > pairs = getAllPairs ( keyPhrases );
			twoCombination.add ( pairs.get ( i ).x + " " + pairs.get ( i ).y );
		}
		// Only write queries that are already present in Queries.txt
		for ( String topic : broadTopics ) {
			if ( ! queries.contains ( topic.toLowerCase () ) )
				writer.println ( topic.toLowerCase () );
		}
		writer.close ( );
		writer = new PrintWriter ( "src/main/resources/combinationOfBroadTopics.txt" );
		for ( String combination : twoCombination ) {
			if ( ! queries.contains ( combination.toLowerCase () ) )
				writer.println ( combination.toLowerCase () );
		}
		writer.close ( );

	}

	/**
	 * This function takes in the file containing the papers and keyPhrases
	 * and returns a HashMap that has the paper-id as the key and a list of keyphrases
	 * per paper as the value
	 *
	 * @param fileName
	 *
	 * @return
	 *
	 * @throws IOException
	 */
	public static HashMap< String, ArrayList< String > > getDataFromFile ( String fileName ) throws IOException {
		BufferedReader br = new BufferedReader ( new FileReader ( fileName ) );
		HashMap< String, ArrayList< String > > result = new HashMap<> ( );
		String line = null;
		while ( ( line = br.readLine ( ) ) != null ) {
			String[] vals = line.split ( "\t" );
			int len = vals.length;
			ArrayList< String > list = new ArrayList<> ( );
			for ( int i = 1 ; i < len ; i++ ) {
				list.add ( vals[ i ] );
			}
			result.put ( vals[ 0 ], list );
		}
		return result;
	}

	/**
	 * This function returns all possible pairs of keyPhrases
	 * from a given list of keyPhrases
	 *
	 * @param phrases
	 *
	 * @return
	 */
	public static ArrayList< Pair > getAllPairs ( ArrayList< String > phrases ) {
		ArrayList< Pair > pairs = new ArrayList< Pair > ( );
		int total = phrases.size ( );
		for ( int i = 0 ; i < total ; i++ ) {
			String phrase1 = phrases.get ( i );
			for ( int j = i + 1 ; j < total ; j++ ) {
				String phrase2 = phrases.get ( j );
				pairs.add ( new Pair ( phrase1, phrase2 ) );
			}
		}
		return pairs;
	}
}

/**
 * This class has a pair of keyPhrases
 * as : (phrase1, phrase2)
 */
class Pair {
	public String x, y;

	public Pair ( String x, String y ) {
		this.x = x;
		this.y = y;
	}
}
