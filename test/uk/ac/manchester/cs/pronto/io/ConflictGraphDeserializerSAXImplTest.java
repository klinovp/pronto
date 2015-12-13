/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

import uk.ac.manchester.cs.pronto.constraints.ConflictGraph;
import uk.ac.manchester.cs.pronto.ProntoMainTestSuite;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Aug 30, 2009
 */
public class ConflictGraphDeserializerSAXImplTest {

	private static final String	FILE_PREFIX	= "file:" + ProntoMainTestSuite.BASE + "io/";
	
	/**
	 * Loads conflict graph from disk and verifies it
	 */
	@Test
	public void testDeserialize() throws Exception {
		
		InputStreamReader reader = new InputStreamReader(URI.create(FILE_PREFIX + "test_cg_brca.xml").toURL().openStream());
		ConflictGraph cg = new ConflictGraphDeserializerSAXImpl().deserialize( reader );
		
		assertNotNull(cg);
		assertEquals(44, cg.getConstraintSets().size());
		//System.out.println(cg.getConstraintSets().size());
	}

}
