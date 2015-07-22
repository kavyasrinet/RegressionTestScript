import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.fieldvaluefactor.FieldValueFactorFunctionBuilder;

/**
 * This class builds the query to be sent to the index
 * @author i-kavyas
 *
 */
public class QueryBuilderService{
    /**
     * Thsi function returns a BoolQueryBuilder that takes into consideration
     * various weights for various fields, a new querty to be tested.
     * @param queryString
     * @return
     */
    public static FunctionScoreQueryBuilder queryBuilderAlpha(String queryString){
        BoolQueryBuilder qb = QueryBuilders.boolQuery()
                .must(
                        QueryBuilders.queryStringQuery(QueryParserBase.escape(queryString))
                                //  //  	.field(PaperMeta.FieldNames.Venue, 1.8f)
                                .field(PaperMeta.FieldNames.Authors, 1.8f)
                                .field(PaperMeta.FieldNames.Title, 1.8f)
                                .field(PaperMeta.FieldNames.PaperAbstract, 1f)
                                .field(PaperMeta.FieldNames.CitationContextsString)//, 1.5f)
                                .field(PaperMeta.FieldNames.BodyText,0.5f)
                                .useDisMax(false)
                                .defaultOperator(QueryStringQueryBuilder.Operator.AND)
                )
                .should(
                        QueryBuilders.multiMatchQuery(queryString,
                                PaperMeta.FieldNames.Title+"^1.8", PaperMeta.FieldNames.PaperAbstract+"^1.1", PaperMeta.FieldNames.BodyText+"^0.5")
                                .type(MatchQueryBuilder.Type.PHRASE)
                                .slop(1)
                )
                .should(
                        QueryBuilders.multiMatchQuery(queryString,
                                PaperMeta.FieldNames.Title+"^1.8", PaperMeta.FieldNames.PaperAbstract+"^1.1",PaperMeta.FieldNames.BodyText+"^0.5")//, PaperMeta.FieldNames.Venue)//+"^1.5" )
                                .type(MatchQueryBuilder.Type.PHRASE)
                                .slop(0)
                );

        FieldValueFactorFunctionBuilder scoreFunctionsBuilder = ScoreFunctionBuilders.fieldValueFactorFunction("numCitedBy").modifier(FieldValueFactorFunction.Modifier.LOG2P).factor(1f);
        return QueryBuilders.functionScoreQuery(qb, scoreFunctionsBuilder);
    }
}