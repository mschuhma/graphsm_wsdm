DELETE FROM PropObjCnt
WHERE
prop LIKE 'dbptmpl:%' OR
prop = 'dbpediaowl:wikiPageExternalLink' OR
prop = 'dbpediaowl:wikiPageWikiLink' OR
prop = 'dbpediaowl:wikiPageRedirects' OR
prop = 'dbpediaowl:wikiPageInterLanguageLink' OR
prop = 'dbpediaowl:thumbnail' OR
prop = 'dbpediaowl:wikiPageDisambiguates' OR
prop = 'rdfs:label' OR
prop = 'rdfs:suBClassOf' OR
prop = 'dc11:rights' OR
prop = 'dc11:language' OR
prop = 'dc11:description' OR
prop = 'dbpprop:wikiPageUsesTemplate' OR
prop = 'wdrs:describedby' OR
prop = 'owl:sameAs' OR
prop = 'prov:wasDerivedFrom' OR
prop = 'foaf:primaryTopic' OR
prop = 'wgs84:lat' OR
prop = 'wgs84:long' OR
prop = 'rdfs:seeAlso';

-----------

SELECT prop, count( * ) AS cnt
FROM `PropObjCnt`
WHERE obj NOT LIKE '%http://%'
AND obj NOT LIKE '%:%'
GROUP BY prop
ORDER BY cnt DESC;

------
SELECT sum(cnt)
FROM `PropObjCombIC` 

UPDATE PropObjCombIC
SET IC = log(sum(cnt)/cnt):