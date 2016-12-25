#symptom-checker

##Overview

This is a web crawler and lucene indexer to collect symptoms from NHS web pages. It creates a list of common symptoms from a large list on the web. It then visits nhs symptom web pages for various ailments and crawls them in a concurrent manner. The contents of each nhs web page is saved to a lucene index. The contents are also checked against the common symptom list and any matches are saved to a H2 database.

##To run the program

The site indexer must be run in order for the documents to be indexed to lucene and for the symtoms to be saved to H2. Once the indexer is complete, the values from the database and the index can be viewed. The NHSSymptomIndexer#printSymptomsFromDatabase prints all the matches that were found against the common symtpoms list and saved to the H2 database. The percentage accuracy of the indexed symptoms that were saved to the H2 database can be retrieved with the AccuracyCalculator#getAccuracy method. This method relies on a human-created list of symptoms for each page to check the H2 values against. 
Some examples of index searches are given in the NHSSymptomIndexer#printDocumentsFromIndex method. You can supply your own Query based search or String based search by adding them to this method. You must use Lucene queries and Strings that can be interpreted by a ComplexPhraseQueryParser (constructed with StandardAnalyzer).

