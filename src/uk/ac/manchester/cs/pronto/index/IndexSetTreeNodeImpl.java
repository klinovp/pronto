/**
 * 
 */
package uk.ac.manchester.cs.pronto.index;

import java.io.IOException;
import java.io.Writer;

import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.utils.ATermUtils;

import uk.ac.manchester.cs.graph.Node;

import aterm.ATermAppl;

import uk.ac.manchester.cs.pronto.cache.IndexCache;
import uk.ac.manchester.cs.pronto.cache.IndexCacheUtils;

/**
 * <p>Title: IndexSetTreeNodeImpl</p>
 * 
 * <p>Description: Index set node always has 3 children: they correspond to ~DC,
 *         DC and ~C branches for the current constraint. Some nodes might be
 *         null which indicates that the branch is pruned off
 * </p>
 * 
 * <p>Copyright: Copyright (c) 2007, 2008</p>
 * 
 * <p>Company: Clark & Parsia, LLC. <http://www.clarkparsia.com></p>
 * 
 * @author pavel
 */ 
public class IndexSetTreeNodeImpl extends Node<IndexTerm> {

	private static int DC_INDEX = 0;	
	private static int NOT_DC_INDEX = 1;	
	private static int NOT_C_INDEX = 2;
	
	private ATermAppl	m_dTerm;
	private ATermAppl	m_cTerm;
	/*
	 * Simple optimization
	 */
	private ATermAppl	m_dcTerm;
	private ATermAppl	m_notdcTerm;
	private ATermAppl	m_notcTerm;
	private ATermAppl	m_notdTerm;
	
	//private boolean m_bRefresh = false;

	public IndexSetTreeNodeImpl(IndexSetTreeNodeImpl parent, IndexTerm term) {

		super(parent);
		
		if (null != term) {
			setObject( term );			
			assertChildren();
		}
	}
	
	protected void refresh() {
		
		if (!isLeaf()) {
		
			assertChildren();
			
			if (ATermUtils.TOP.isEqual( m_cTerm )) {
				
				m_dcTerm = m_dTerm;
				m_notcTerm = ATermUtils.BOTTOM;
				m_notdcTerm = ATermUtils.makeNot( m_dTerm );
 
			} else {
				
				m_dcTerm = ATermUtils.makeAnd( m_dTerm, m_cTerm );
				m_notcTerm = ATermUtils.makeNot( m_cTerm );
				m_notdcTerm = ATermUtils.makeAnd( ATermUtils.makeNot( m_dTerm ), m_cTerm );
			}
			
			m_notdTerm = ATermUtils.makeNot( m_dTerm );			
			
		} else if (null == m_cTerm && m_dTerm == null ){
			
			ensureLeaf();
		}
	}
	
	private boolean updateChild(int index, ATermAppl conjunct, KnowledgeBase kb, IndexCache cache) {
		
		IndexSetTreeNodeImpl child = (IndexSetTreeNodeImpl)getChildAt( index );
		ATermAppl term = getObject().getTerm();
		
		if (!child.isDead()) {
			
			term = ATermUtils.makeAnd( term, conjunct );

			if (!IndexCacheUtils.isSatisfiable(cache, kb, term )) {
				//Prune
				child.setObject( null );
				
				return false;
				
			} else {
				
				child.getObject().setTerm( term );
				child.update( kb, cache );
				
				return true;
			}
			
		} else {
			
			return false;
		}
	}
	/**
	 * Runs down the subtree, updates terms and checks satisfiability
	 */
	protected void update(KnowledgeBase kb, IndexCache cache) {

		getObject().setTerm( ATermUtils.simplify( getObject().getTerm()) );
		
		if( !isLeaf() ) {
			//It is assumed that current node is satisfiable. Check the children
			boolean hasChild = false;
			
			hasChild |= updateChild(0, m_dcTerm, kb, cache);
			hasChild |= updateChild(1, m_notdcTerm, kb, cache);
			hasChild |= updateChild(2, m_notcTerm, kb, cache);
			
			if (!hasChild) {
				ensureLeaf();
			}
		} 
			
	}

	public ATermAppl getCTerm() {
		return m_cTerm;
	}

	public ATermAppl getDTerm() {
		return m_dTerm;
	}

	public void setCTerm(ATermAppl cTerm) {
		
		m_cTerm = cTerm;
		
		if (!isDead()) {
			refresh();
		}
	}

	public void setDTerm(ATermAppl dTerm) {
		m_dTerm = dTerm;
		
		if (!isDead()) {
			refresh();
		}
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
	
	public IndexSetTreeNodeImpl getNotCChild() {
		
		return 0 == getChildren().size() ? null : (IndexSetTreeNodeImpl)super.getChildAt( NOT_C_INDEX );
	}

	public void setNotCChild(IndexSetTreeNodeImpl notcChild) {
		
		super.setChildAt( notcChild, NOT_C_INDEX );
	}
	
	public void importNotCChild(IndexSetTreeNodeImpl notcChild, KnowledgeBase kb, IndexCache cache) {
		
		ATermAppl childTerm = ATermUtils.makeAnd( getObject().getTerm(),  m_notcTerm);
		
		if (IndexCacheUtils.isSatisfiable( cache, kb, childTerm )) {
		
			super.setChildAt( importChild(notcChild, kb, cache, childTerm), NOT_C_INDEX );
		}
	}
	
	public IndexSetTreeNodeImpl getDCChild() {
		
		return 0 == getChildren().size() ? null : (IndexSetTreeNodeImpl)super.getChildAt( DC_INDEX );
	}
	
	public void setDCChild(IndexSetTreeNodeImpl dcChild) {
		
		super.setChildAt( dcChild, DC_INDEX );
	}

	public void importDCChild(IndexSetTreeNodeImpl dcChild, KnowledgeBase kb, IndexCache cache) {
		
		ATermAppl childTerm = ATermUtils.makeAnd( getObject().getTerm(),  m_dcTerm);
		
		if (IndexCacheUtils.isSatisfiable( cache, kb, childTerm )) {
		
			super.setChildAt( importChild(dcChild, kb, cache, childTerm), DC_INDEX );
		}
	}
	
	
	public IndexSetTreeNodeImpl getNotDCChild() {
		
		return 0 == getChildren().size() ? null : (IndexSetTreeNodeImpl)super.getChildAt( NOT_DC_INDEX );
	}
	
	public void setNotDCChild(IndexSetTreeNodeImpl notdcChild) {
		
		super.setChildAt( notdcChild, NOT_DC_INDEX );
	}

	public void importNotDCChild(IndexSetTreeNodeImpl notdcChild, KnowledgeBase kb, IndexCache cache) {
		
		ATermAppl childTerm = ATermUtils.makeAnd( getObject().getTerm(),  m_notdcTerm);
		
		if (IndexCacheUtils.isSatisfiable( cache, kb, childTerm )) {
		
			super.setChildAt( importChild(notdcChild, kb, cache, childTerm), NOT_DC_INDEX );
		}
	}
	
	private void assertChildren() {
		
		if (0 == getChildren().size()) {
		
			addChild( null );
			addChild( null );
			addChild( null );
		}
	}

	protected void ensureLeaf() {
		
		setChildren( null );
		m_cTerm = null;
		m_dTerm = null;
		m_dcTerm = null;
		m_notcTerm = null;
		m_notdTerm = null;		
		m_notdcTerm = null;
	}
	
	@Override
	public boolean isLeaf() {

		return (null == m_cTerm || m_dTerm == null);
	}

	@Override
	protected Node<IndexTerm> newNode(Node<IndexTerm> parent) {

		return new IndexSetTreeNodeImpl( (IndexSetTreeNodeImpl) parent, null );
	}

	public boolean isIndexTerm() {
		
		return isLeaf() && !isDead();
	}
	
	
	public void dump(Writer writer, int level) throws IOException {
		
		for (int i = 0; i <= level; i++) {
			writer.write( "  " );
		}
		
		if (isDead()) {
			writer.write( "DEAD! " );
		}
		
		if (isLeaf()) {
			
			writer.write( "Leaf: " );
			writer.write( getObject() + System.getProperty( "line.separator" ));
			
		} else {
			
			writer.write( getObject().toString() + System.getProperty( "line.separator" ));
			
			getDCChild().dump( writer, level + 1 );
			getNotDCChild().dump( writer, level + 1 );
			getNotCChild().dump( writer, level + 1 );
		}
	}

	
	public int renumberChildren(int base) {
		
		if (isIndexTerm()) {
			
			getObject().setIndex( base );
			//System.out.println(getObject());
			return base + 1;
			
		} else if (!isDead()) {

			getObject().setIndex( -1 );			
			
			base = getDCChild().renumberChildren( base );
			base = getNotDCChild().renumberChildren( base );
			base = getNotCChild().renumberChildren( base );
		}
			
		return base;
	}

	
	public IndexSetTreeNodeImpl importChild(IndexSetTreeNodeImpl child, KnowledgeBase kb, IndexCache cache, ATermAppl term) {
		
		IndexSetTreeNodeImpl clone = (IndexSetTreeNodeImpl) child.cloneNode( this );
		
		assertChildren();
		clone.getObject().setTerm( term );
		clone.update( kb, cache );
		
		return clone;
	}

	@Override
	/**
	 * We need to make deep copies of index terms
	 */
	protected void copyObject(Node<IndexTerm> node) {

		IndexSetTreeNodeImpl source = (IndexSetTreeNodeImpl) node;
		IndexTerm term = source.getObject();
		
		if (null != term) {
			
			setObject(node.getObject().clone());
			
		} else {
			
			setObject(null);
		}
		
		setCTerm(source.getCTerm());
		setDTerm(source.getDTerm());
	}
	
	/**
	 * Checks if current node has only a single non-dead (satisfiable) child
	 * If yes - returns this child. If not - returns null
	 * 
	 * @return
	 */
	public IndexSetTreeNodeImpl hasSingleChild() {

		Node<IndexTerm> single = null;
		int dead = 0;
		
		if( !isLeaf() && !isDead() ) {

			for( Node<IndexTerm> child : getChildren() ) {
				
				if( ((IndexSetTreeNodeImpl) child).isDead() ) {
					
					dead++;
					
				} else {
					
					single = child;
				}
			}
		}
		
		return 2 == dead ? (IndexSetTreeNodeImpl)single : null;
	}
}
