
public class PaperMeta {

    static String PaperElasticSearchType = "paper";
    String CitationElasticSearchType = "citation";

    class  Highlights {
        String PreTag = "___PRE_TAG___";
        String PostTag = "___POST_TAG___";

//		    String PreTagRegex = PreTag.r
//		    		String PostTagRegex = PostTag.r
    }

    // TODO: How should we share index structure between s2-offline and online.
    // This is really magic-Stringue consolidation
    static class FieldNames {
        String Survey = "survey";
        String Experimental = "experimental";
        String Theoretical = "theoretical";
        String System = "system";
        String Relevance = "relevance";
        static String Authors = "authors";
        static String BodyText = "bodyText";
        String CompleteRefCount = "completeRefCount";
        static String CitationContexts = "citationContexts";
        static String CitationContextsString = CitationContexts;
        String CitationRank = "citationRank";
        String CitationRankPercentile = "citationRankPercentile";
        String CitationRate = "citationRate";
        String CitedBy = "citedBy";
        String Classification = "classification";
        String FollowOn = "followOn";
        String KeyPhrases = "keyPhrases";
        String MainConcepts = "mainConcepts";
        String NumCitedBy = "numCitedBy";
        String NumFollowOn = "numFollowOn";
        String OpenAirLink = "openAirLink";
        static String PaperAbstract = "paperAbstract";
        String PaperContexts = "paperContexts";
        String References = "references";
        String ShortVenue = "shortVenue";
        static String Title = "title";
        static String Venue = "venue";
        String Year = "year";
        String Context = "context";
        String CitedId = "citedId";
        String PaperId = "paperId";



    }
}
