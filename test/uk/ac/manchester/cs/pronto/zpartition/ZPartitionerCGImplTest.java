package uk.ac.manchester.cs.pronto.zpartition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import uk.ac.manchester.cs.pronto.ConditionalConstraint;
import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;
import uk.ac.manchester.cs.pronto.events.AfterComputeConflictGraphEventHandlerImpl;
import uk.ac.manchester.cs.pronto.events.BeforeComputeConflictGraphEventHandlerImpl;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.ReasoningEventHandler;
import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;
import uk.ac.manchester.cs.pronto.lp.CCSetAnalyzerImpl2;
import uk.ac.manchester.cs.pronto.ProntoMainTestSuite;

/**
 * @author Pavel Klinov
 */
public class ZPartitionerCGImplTest {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "zpartition/";
	private static Map<EVENT_TYPES, List<ReasoningEventHandler>> s_handlersMap = new HashMap<>();

	@Test
	public void testZPartition1() throws Exception {

		ProbKnowledgeBase kb = new KBStandaloneLoader().load(FILE_PREFIX + "test_zp_1.xml" );
		ZPartitioner zper = new ZPartitionerCGImpl( new CCSetAnalyzerImpl2() );
		
		s_handlersMap.put( EVENT_TYPES.BEFORE_CONFLICT_GRAPH_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new BeforeComputeConflictGraphEventHandlerImpl()
				}) );		
		s_handlersMap.put( EVENT_TYPES.AFTER_CONFLICT_GRAPH_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new AfterComputeConflictGraphEventHandlerImpl()
				}) );
		
		zper.setEventHandlers( s_handlersMap );		
		
		ZPartition zp = zper.partition( kb.getPTBox() );

		assertEquals( 2, zp.numberOfPartitions() );

		Iterator<Set<ConditionalConstraint>> zpIter = zp.partitionIterator();

		assertTrue( zpIter.hasNext() );
		/*
		 * Most specific goes first
		 */
		assertEquals( 1, zpIter.next().size() );
		assertEquals( 2, zpIter.next().size() );
		assertFalse( zpIter.hasNext() );
	}
	
	@Test
	public void testZPartition2() throws Exception {

		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_zp_3.xml" );
		
		ZPartitioner zper = new ZPartitionerCGImpl( new CCSetAnalyzerImpl2() );
		
		s_handlersMap.put( EVENT_TYPES.BEFORE_CONFLICT_GRAPH_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new BeforeComputeConflictGraphEventHandlerImpl()
				}) );		
		s_handlersMap.put( EVENT_TYPES.AFTER_CONFLICT_GRAPH_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new AfterComputeConflictGraphEventHandlerImpl()
				}) );
		
		zper.setEventHandlers( s_handlersMap );		
		
		ZPartition zp = zper.partition( kb.getPTBox() );

		assertEquals( 2, zp.numberOfPartitions() );

		Iterator<Set<ConditionalConstraint>> zpIter = zp.partitionIterator();

		assertTrue( zpIter.hasNext() );
		/*
		 * Most specific goes first
		 */
		assertEquals( 2, zpIter.next().size() );
		assertEquals( 2, zpIter.next().size() );
		assertFalse( zpIter.hasNext() );
	}

	@Test
	public void testZPartition3() throws Exception {

		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_zp_4.xml" );
		
		ZPartitioner zper = new ZPartitionerCGImpl( new CCSetAnalyzerImpl2() );
		
		s_handlersMap.put( EVENT_TYPES.BEFORE_CONFLICT_GRAPH_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new BeforeComputeConflictGraphEventHandlerImpl()
				}) );		
		s_handlersMap.put( EVENT_TYPES.AFTER_CONFLICT_GRAPH_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new AfterComputeConflictGraphEventHandlerImpl()
				}) );
		
		zper.setEventHandlers( s_handlersMap );		
		
		ZPartition zp = zper.partition( kb.getPTBox() );

		assertEquals( 3, zp.numberOfPartitions() );

		Iterator<Set<ConditionalConstraint>> zpIter = zp.partitionIterator();

		assertTrue( zpIter.hasNext() );
		/*
		 * Most specific goes first
		 */

		assertEquals( 1, zpIter.next().size() );
		assertEquals( 2, zpIter.next().size() );
		assertEquals( 1, zpIter.next().size() );
		assertFalse( zpIter.hasNext() );
	}
	
	@Test
	public void testZPartitionNull1() throws Exception {

		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_zp_2.xml" );
		
		ZPartitioner zper = new ZPartitionerCGImpl( new CCSetAnalyzerImpl2() );
		
		s_handlersMap.put( EVENT_TYPES.BEFORE_CONFLICT_GRAPH_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new BeforeComputeConflictGraphEventHandlerImpl()
				}) );		
		s_handlersMap.put( EVENT_TYPES.AFTER_CONFLICT_GRAPH_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new AfterComputeConflictGraphEventHandlerImpl()
				}) );
		
		zper.setEventHandlers( s_handlersMap );		
		
		ZPartition zp = zper.partition( kb.getPTBox() );

		assertNull( zp );
	}
	
	
	/*
	 */
	@Test
	public void testZPartitionNull2() throws Exception {

		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_g_inconsistent.xml" );

		ZPartitioner zper = new ZPartitionerCGImpl( new CCSetAnalyzerImpl2() );
		
		s_handlersMap.put( EVENT_TYPES.BEFORE_CONFLICT_GRAPH_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new BeforeComputeConflictGraphEventHandlerImpl()
				}) );		
		s_handlersMap.put( EVENT_TYPES.AFTER_CONFLICT_GRAPH_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new AfterComputeConflictGraphEventHandlerImpl()
				}) );
		
		zper.setEventHandlers( s_handlersMap );		
		
		ZPartition zp = zper.partition( kb.getPTBox() );

		assertNull( zp );
	}

}
