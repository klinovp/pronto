/**
 * 
 */
package uk.ac.manchester.cs.pronto.io;

import java.io.File;
import java.io.IOException;

import uk.ac.manchester.cs.pronto.ProbKnowledgeBase;

/**
 * @author Pavel Klinov
 *
 * pklinov@cs.man.ac.uk, pklinov@clarkparsia.com
 * 
 * Serializes probabilistic knowledge base to human or machine readable formats
 * 
 */
public interface PKBSerializer {

	public String serialize(ProbKnowledgeBase pkb) throws IOException;
	public void serializeToFile(ProbKnowledgeBase pkb, File file) throws IOException;
}
