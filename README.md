# Pronto: a probabilistic OWL reasoner
=============

Pronto is a probabilistic reasoner built on top of Pellet [1] that enables
probabilistic knowledge representation and reasoning in OWL ontologies.
Pronto is shipped as a Java library equipped with a command line tool for
demonstrating its capabilities. The main features are:

    * Adding probabilistic statements to an ontology.
    * Inferring new probabilistic statements from a probabilistic 
      ontology
    * Explaining results of probabilistic reasoning
    
You can find more information about Pronto in doc/basic.pdf. There is 
a complete example of Pronto usage with a probabilistic ontology in
examples/brc directory.
    
Installation
------------

Unzip the pronto distribution file into a directory of your choice.

TODO explain how to install GLPK

You can run Pronto command-line program using the script pronto.bat on 
Windows systems or pronto.sh on Unix systems. The command-line program
expects the following arguments:

pronto.bat (or: .sh, if you are on a Mac/Unix) <ontology uri> <mode> <query file uri>

mode can be 's' (probabilistic part in a separate file) or 'e'
(embedded into classical part)

query file format: <query type> <query parameter>*

supported queries:
 - entail <evidence class uri> <conclusion class uri>
 - entail <individual uri> <class uri>
 - psat - decides probabilistic satisfiability
 - consistency - decides probabilistic consistency
 - improbable - determines improbable classes (if any)

Types of queries correspond to the following features:

    * Entailing of a generic (TBox) conditional constraint - 
      expression of the form (C|D)[l,u] where C and D are DL classes
    * Entailing of a concrete (ABox) conditional constraint - 
      expression of the form (C|Thing)[l,u] for some individual (where 
      C is a class)
    * Checking that probabilistic ontology is satisfiable (e.g. has 
      probabilistic model)
    * Checking the probabilistic ontology is consistent (e.g. all 
      conflicts between probabilistic axioms can be resolved during 
      reasoning). See details on "lexicographic entailment" in the 
      original papers by T. Lukasiewicz [2]
    * Retrieving all classes that cannot consistently have probability 
      greater than zero. In other words, any constraint of the form 
      (C|Thing)[x,1] where x > 0 would make the ontology inconsistent

Compiling
---------

If you would like to rebuild Pronto from source files Pronto comes 
with an Ant [3] build file. Packaging can easily be done by running
the command "ant dist" from Pronto's root directory. The resulting jar 
file is named "pronto.jar" and put under dist/lib directory. The main
class in the jar file is uk.ac.manchester.cs.pronto.Pronto.

[1] https://github.com/complexible/pellet
[2] Thomas Lukasiewicz, "Probabilistic Description Logics for the
Semantic Web," available at: 
http://www.kr.tuwien.ac.at/research/reports/rr0605.pdf
[3] http://ant.apache.org/
