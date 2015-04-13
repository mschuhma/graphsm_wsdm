# graphsm_wsdm
Graph Space Model (WSDM'14)

## Intro
This is the code for the graph document model as described in:

 Michael Schuhmacher and Simone Paolo Ponzetto: Knowledge-based Graph Document Modeling. In Proceedings of WSDM'14, pp.   543-552, ACM, 2014 ([paper](http://dx.doi.org/10.1145/2556195.2556250),            [presentation](http://dws.informatik.uni-mannheim.de/fileadmin/lehrstuehle/ki/pub/michael/WSDM14_Schuhmacher_Knowledge-based_Graph_Document_Modeling.pdf))


## This code
Note that this code is not really running out of the box. It's more a resource for you to copy&paste some code. Most relevant w.r.t. to the paper are:

* The main experiments method running the [LP50 experiments](src/main/java/de/uma/dws/graphsm/experiments/Exp130810PairwiseGraphsDijkstrHungJGTTagMe.java) with the Lee Pincombe 50 Documents data (as repored in the paper)
* The Triple Weighting approaches [CombIC](src/main/java/de/uma/dws/graphsm/tripleweighter/TripleWeighterAddedIC.java), [jointIC](src/main/java/de/uma/dws/graphsm/tripleweighter/TripleWeighterJointIC.java), [PMI](src/main/java/de/uma/dws/graphsm/tripleweighter/TripleWeighterPMIPlusIC.java)
* The JGraphT-based, parallel [Dijkstral](src/main/java/de/uma/dws/graphsm/jgrapht/DijkstraParallel.java) for computing the cheapest Paths
* The [Hungarian Method](src/main/java/de/unima/alcomox/algorithms/HungarianMethod.java) for the approx. Graph Matching

Also third party datasets are not provided, as I cannot redistributed those without permission. Folders where third-party data are need are empty in this repo, but contain a readme.txt.

## DBpedia Graph Weights

The different Triple Weighter Classes build upon a full triple counts statistic for DBpedia.
These data are already pre-computed and availble from here

* [Gibhub:dbpediaweights](https://github.com/mschuhma/dbpediaweights) (for DBpedia 3.9 and DBpedia2014)
