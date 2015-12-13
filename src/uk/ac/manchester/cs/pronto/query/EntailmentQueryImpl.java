/**
 * 
 */
package uk.ac.manchester.cs.pronto.query;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.exceptions.QueryProcessingException;
import uk.ac.manchester.cs.pronto.util.CCUtils;

/**
 * <p>Title: EntailmentQueryImpl</p>
 * 
 * <p>Description: 
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */
public class EntailmentQueryImpl implements PQuery {

	private ATermAppl m_evidence = null;
	private ATermAppl m_conclusion = null;
	
	public EntailmentQueryImpl() {}
	
	public int getQueryType() {
		return ENTAILMENT_QUERY;
	}

	public List<?> getQueryParameters() {
		
		return Arrays.asList(new ATermAppl[] {m_evidence, m_conclusion});
	}
	
	public void deserialize(String params, String separator) throws QueryProcessingException {
		
		StringTokenizer st = new StringTokenizer(params, separator);

		try {
			
			m_evidence = CCUtils.uriToATerm(st.nextToken());
			m_conclusion = CCUtils.uriToATerm(st.nextToken());
			
		} catch (Throwable e) {
			
			e.printStackTrace();
			throw new QueryProcessingException(e);
		}
	}
	
	public ATermAppl getEvidence() {
		
		return m_evidence;
	}
	
	public void setEvidence(ATermAppl evidence) {
		
		m_evidence = evidence;
	}

	public ATermAppl getConclusion() {
		
		return m_conclusion;
	}
	
	public void setConclusion(ATermAppl conclusion) {
		
		m_conclusion = conclusion;
	}

	@Override
	public String toString() {

		return "entail";
	}
	
	
}
