package uk.ac.manchester.cs.pronto;

import org.mindswap.pellet.utils.ATermUtils;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.util.CCUtils;

/**
 * <p>Title: ConditionalConstraint</p>
 * 
 * <p>Description: 
 *  Represents a conditional constraint of the form (D|C)[l,u] where C is a
 *  concept-evidence, D - concept-conclusion and l,u are the lower and upper
 *  bounds respectively
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel 
 */
public class ConditionalConstraint {

	//Both evidence and conclusion can be arbitrary concept expressions
	private ATermAppl	m_evidence;
	private ATermAppl	m_conclusion;
	private Interval	m_interval;
	//Little optimization
	private ATermAppl	m_dcTerm;
	private ATermAppl	m_notdcTerm;
	private ATermAppl	m_notcTerm;
	private ATermAppl	m_notdTerm;
	// Used for a proper ordering in containers
	private final int m_orderId = globalId();
	
	private static int s_global_id = 0;
	
	private static synchronized int globalId() {
		
		return s_global_id++;
	}

	public ConditionalConstraint(ATermAppl evidence, ATermAppl conclusion, double lower, double upper) {
		
		m_evidence = ATermUtils.normalize( evidence );
		m_conclusion = ATermUtils.normalize( conclusion );
		m_interval = new Interval( lower, upper );
		
		refresh();
	}

	public ConditionalConstraint(String evURI, String cnURI, double lower, double upper) {
		
		this( CCUtils.uriToATerm(evURI), CCUtils.uriToATerm(cnURI), lower, upper );
	}

	private void refresh() {

		if( null != m_evidence && null != m_conclusion ) {

			if( ATermUtils.TOP.isEqual( m_evidence ) ) {
				
				m_dcTerm = m_conclusion;
				m_notdcTerm = ATermUtils.makeNot( m_conclusion );
				m_notcTerm = ATermUtils.BOTTOM;
				m_notdTerm = ATermUtils.negate( m_conclusion );
			} else {
				
				m_dcTerm = ATermUtils.normalize( ATermUtils.makeAnd( m_conclusion, m_evidence ));
				m_notdcTerm = ATermUtils.normalize(ATermUtils.makeAnd( ATermUtils.makeNot( m_conclusion ), m_evidence ));
				m_notcTerm = ATermUtils.negate( m_evidence );
				m_notdTerm = ATermUtils.negate( m_conclusion );
			}
		}
	}
	
	public ATermAppl getEvidence() {
		
		return m_evidence;
	}

	public ATermAppl getConclusion() {
		
		return m_conclusion;
	}

	public double getLowerBound() {
		
		return m_interval.getLowerBound();
	}

	public double getUpperBound() {
		
		return m_interval.getUpperBound();
	}

	public void setUncertaintyInterval(double lower, double upper) {
		
		m_interval = new Interval( lower, upper );
	}

	public Interval getUncertaintyInterval() {
		
		return m_interval;
	}


	public ATermAppl getDCTerm() {
		
		return m_dcTerm;
	}

	public ATermAppl getNotDCTerm() {
		
		return m_notdcTerm;
	}
	
	public ATermAppl getNotCTerm() {
		
		return m_notcTerm;
	}	
	
	public ATermAppl getNotDTerm() {
		
		return m_notdTerm;
	}	
	
	@Override
	public boolean equals(Object obj) {

		if( !(obj instanceof ConditionalConstraint) || null == obj ) {
			
			return false;
			
		} else if( this == obj ) {

			return true;
			
		} else {

			ConditionalConstraint cc = (ConditionalConstraint) obj;

			if( m_evidence.isEqual( cc.getEvidence() ) && m_conclusion.isEqual( cc.getConclusion() )
					&& m_interval.isEqual( cc.getUncertaintyInterval() ) ) {
				
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {

		return ("(" + CCUtils.getLocalName( m_conclusion.toString() ) + "|"
				+ CCUtils.getLocalName( m_evidence.toString() ) + ")" + m_interval.toString()).hashCode();
	}

	
	@Override
	public String toString() {

		return m_orderId + ": (" + CCUtils.getLocalName( m_conclusion.toString() ) + "|"
				+ CCUtils.getLocalName( m_evidence.toString() ) + ")" + m_interval.toString();
	}	

	public int getOrder() {
		
		return m_orderId;
	}
	
	/**
	 * Simple representation of a closed interval
	 * 
	 * @author pavel
	 */
	public class Interval {

		double	m_lower;
		double	m_upper;

		Interval(double l, double u) {
			m_lower = l;
			m_upper = u;
		}

		public double getLowerBound() {
			return m_lower;
		}

		public double getUpperBound() {
			return m_upper;
		}

		public boolean isEqual(Interval interval) {

			return (Math.abs(m_lower - interval.getLowerBound()) < Constants.PROBABILITY_LOW_THRESHOLD)
			&& (Math.abs(m_upper - interval.getUpperBound()) < Constants.PROBABILITY_LOW_THRESHOLD);
		}
		
		public String toString() {
			
			return "[" + m_lower + ";" + m_upper + "]";
		}

		public int hashCode() {
			
			return toString().hashCode();
		}

		/*
		 * We do not implement Comparable because not all intervals are comparable
		 */
		public int compareTo(Interval arg0) {
			
			if ((m_lower + Constants.PROBABILITY_LOW_THRESHOLD) >= arg0.getLowerBound()
					&& (m_upper + Constants.PROBABILITY_LOW_THRESHOLD >= arg0.getUpperBound())) {
				
				return 1;
				
			} else if (m_lower <= (arg0.getLowerBound() + Constants.PROBABILITY_LOW_THRESHOLD)
					&& m_upper <= (arg0.getUpperBound() + Constants.PROBABILITY_LOW_THRESHOLD)) {
				
				return -1;
			}
			
			return 0;
		}
	}

	public boolean isConditional() {
		
		return !m_evidence.equals( ATermUtils.TOP );
	}
}
