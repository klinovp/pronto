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

import uk.ac.manchester.cs.pronto.PSATSolverImpl;
import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;
import uk.ac.manchester.cs.pronto.events.AfterComputeZPartitionEventHandlerImpl;
import uk.ac.manchester.cs.pronto.events.BeforeComputeZPartitionEventHandlerImpl;
import uk.ac.manchester.cs.pronto.events.EVENT_TYPES;
import uk.ac.manchester.cs.pronto.events.ReasoningEventHandler;
import uk.ac.manchester.cs.pronto.io.KBStandaloneLoader;
import uk.ac.manchester.cs.pronto.ProntoMainTestSuite;

/**
 * <p>Title: ZPartitionerTest</p>
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
public class ZPartitionerTest {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "zpartition/";
	private static Map<EVENT_TYPES, List<ReasoningEventHandler>> s_handlersMap = new HashMap<EVENT_TYPES, List<ReasoningEventHandler>>();

	public ZPartitionerTest() {}

	@Test
	public void testZPartition() throws Exception {

		ProbKnowledgeBase kb = new KBStandaloneLoader().load(FILE_PREFIX + "test_zp_1.xml" );
		ZPartitioner zper = new ZPartitionerImpl( new PSATSolverImpl() );
		
		s_handlersMap.put( EVENT_TYPES.BEFORE_ZPARTITION_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new BeforeComputeZPartitionEventHandlerImpl()
				}) );		
		s_handlersMap.put( EVENT_TYPES.AFTER_ZPARTITION_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new AfterComputeZPartitionEventHandlerImpl()
				}) );
		
		zper.setEventHandlers( s_handlersMap );
		
		ZPartition zp = zper.partition( kb.getPTBox() );
		
		assertEquals( 2, zp.numberOfPartitions() );

		Iterator zpIter = zp.partitionIterator();

		assertTrue( zpIter.hasNext() );
		/*
		 * Most specific goes first
		 */
		assertEquals( 1, ((Set) zpIter.next()).size() );
		assertEquals( 2, ((Set) zpIter.next()).size() );
		assertFalse( zpIter.hasNext() );
	}

	@Test	
	public void testZPartitionNull() throws Exception {

		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_g_inconsistent.xml" );
		ZPartitioner zper = new ZPartitionerImpl( new PSATSolverImpl() ); 
		
		s_handlersMap.put( EVENT_TYPES.BEFORE_ZPARTITION_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new BeforeComputeZPartitionEventHandlerImpl()
				}) );		
		s_handlersMap.put( EVENT_TYPES.AFTER_ZPARTITION_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new AfterComputeZPartitionEventHandlerImpl()
				}) );		
		
		zper.setEventHandlers( s_handlersMap );
		
		ZPartition zp = zper.partition( kb.getPTBox() );

		assertNull( zp );
	}

	@Test
	public void testZPartition2() throws Exception {

		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_zp_3.xml" );
		ZPartitioner zper = new ZPartitionerImpl( new PSATSolverImpl() ); 
		
		s_handlersMap.put( EVENT_TYPES.BEFORE_ZPARTITION_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new BeforeComputeZPartitionEventHandlerImpl()
				}) );		
		s_handlersMap.put( EVENT_TYPES.AFTER_ZPARTITION_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new AfterComputeZPartitionEventHandlerImpl()
				}) );		
		
		zper.setEventHandlers( s_handlersMap );
		
		ZPartition zp = zper.partition( kb.getPTBox() );

		assertEquals( 2, zp.numberOfPartitions() );

		Iterator zpIter = zp.partitionIterator();

		assertTrue( zpIter.hasNext() );
		/*
		 * Most specific goes first
		 */
		assertEquals( 2, ((Set) zpIter.next()).size() );
		assertEquals( 2, ((Set) zpIter.next()).size() );
		assertFalse( zpIter.hasNext() );
	}
	
	@Test	
	public void testZPartition3() throws Exception {

		ProbKnowledgeBase kb = new KBStandaloneLoader().load( FILE_PREFIX + "test_zp_4.xml" );
		ZPartitioner zper = new ZPartitionerImpl( new PSATSolverImpl() ); 
		
		s_handlersMap.put( EVENT_TYPES.BEFORE_ZPARTITION_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new BeforeComputeZPartitionEventHandlerImpl()
				}) );		
		s_handlersMap.put( EVENT_TYPES.AFTER_ZPARTITION_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new AfterComputeZPartitionEventHandlerImpl()
				}) );		
		
		zper.setEventHandlers( s_handlersMap );
		
		ZPartition zp = zper.partition( kb.getPTBox() );

		assertEquals( 3, zp.numberOfPartitions() );

		Iterator zpIter = zp.partitionIterator();

		assertTrue( zpIter.hasNext() );
		/*
		 * Most specific goes first
		 */

		assertEquals( 1, ((Set) zpIter.next()).size() );
		assertEquals( 2, ((Set) zpIter.next()).size() );
		assertEquals( 1, ((Set) zpIter.next()).size() );
		assertFalse( zpIter.hasNext() );
	}
	
/*	@Test
	public void testZPartitionTemp() throws Exception {

		ProbKnowledgeBase pkb = new KBStandaloneLoader().load( "file:///C:/kl/manchester/pubs/2010/theses/pavel/KBs/Cadiag2/repaired/repaired_1.owl" );
		ZPartitioner zper = new ZPartitionerImpl( new PSATSolverImpl() ); 
		
		s_handlersMap.put( EVENT_TYPES.AFTER_ZPARTITION_COMPUTED,
				Arrays.asList(new ReasoningEventHandler[]{
						new AfterComputeZPartitionEventHandlerImpl()
				}) );		
		
		zper.setEventHandlers( s_handlersMap );

		ZPartition zp = zper.partition( pkb.getPTBox() );

		assertNotNull( zp );
		
		System.out.println("+++ZPartition+++");
		
		for (Iterator<Set<ConditionalConstraint>> iter = zp.partitionIterator(); iter.hasNext();) {
			
			System.out.println( iter.next().size() );
		}
<<<<<<< .mine
	}	*/
}
