
JGraphT, Social Network Analysis addon
======================================

This library implements several algorithms used in social network analysis (SNA) on top of
the JGraphT library [http://www.jgrapht.org/](http://www.jgrapht.org/). JGraphT is a Java library for Graphs. It supports generics, where nodes
and edges can be of user-defined types. The intention is to support as many SNA-specific algorithms as possible.

Our focus so far has been the following:

1. Centrality measures: Degree centrality, closeness centrality, betweenness centralty, eigenvector centrality, etc.
2. Hierarchy detection algorithms.
3. Improved support for GraphML import/export (weighted and directed).
4. Keyplayers algorithms: KPP-POS, KPP-NEG.
5. Random-error generators and generators for complex network types.
6. Edge prediction algorithms: Common neighbors, Adamic/Adar's measure.

Generally, the algorithms are constructed in such a way that they take the types of graphs for which makes sense
to them as input. For example WeightedDegreeCentrality expects a WeightedGraph as input.

Getting started
---------------

JGraphT-SNA is built with Maven, and you can include it by including its artifact in your Maven project.

    <dependency>
       <groupId>dk.aaue.sna</groupId>
       <artifactId>jgrapht-sna</artifactId>
       <version>1.2-SNAPSHOT</version>
    </dependency>

Artifacts are published to my bitbucket maven repo:

    <repository>
        <id>sorend-maven-repo</id>
        <url>https://bitbucket.org/sorend/maven-repo/raw/tip</url>
    </repository>

Alternatively, you can clone the source-code and build it yourself.

Example code
------------

Suppose you have a graph G and you want to know its entropy centrality, you can get it like this:

    Graph<String, Defaultedge> G = buildGraph();
    CentralityMeasure<String> cm = new dk.aaue.sna.alg.centrality.OrtizArroyoEntropyCentrality(G);
    CentralityResult<String> cr = cm.calculate();

    // assuming your graph has a node "me"
    System.out.println("centrality of 'me' = " + cr.get("me"));
    System.out.println("rank of 'me'       = " + cr.getSortedNodes().indexOf("me"));

About
-----

Currently, all code is written by [me](mailto:soren@tanesha.net). Most of the code was done for
my masters thesis, Aalborg University, Denmark.
Feel free to use and modify the code. If anything useful, send me a pull request. Cadeau to the brilliantly smart
authors of all the algorithms implemented here.

License
-------

License is FreeBSD-style. Note JGraphT is GPL-2.1.
