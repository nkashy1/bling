# bling

Tagged data handler.

---



## The purpose of bling: an example application

Suppose that you are building a data base of tweets. Each tweet may contain several hashtags. You can consider the hashtags as definers of context. Suppose a tweet from Alice has the following content:

```THIS SUCKS #A #B```

Then you can assume that Alice is unhappy and that she wants to broadcast her unhappiness to every Twitter user interested in at least one of the hashtags #A and #B. Alice is therefore broadcasting her dissatisfaction in the context represented by `#A` as well as the context represented by `#B`.

Suppose that you are building an application which generates statistics about hashtag usage within the tweets in your data base. If you just stored your tweets raw, as strings, it would be expensive to parse through each tweet to determine whether it contained a given hash tag when you wanted to generate your statistics. This kind of storage scheme would incur unnecessary costs if you wanted to generate statistics for multiple hashtags at the same time. It would be much more reasonable for you to extract the hashtags in a tweet* *at the time it was inserted into the data base and designate somehow that that tweet was associated with those hashtags. Then, when you wanted to generate statistics about a given hashtag or set of hashtags, all you would have to do is look at your hashtag-tweet association and retrieve from that the tweets associated with your hashtag or set of hashtags. In this scheme, each tweet only has to be parsed once for hashtag association rather than having to be parsed over and over again.

Suppose further that you are creating a taxonomy of hashtags. Under this taxonomy, each hashtag can have children and if `#A` has `#B` as a child that means that the context represented by `#B` is a further restriction upon the context specified by `#A`. For example, `#ASeriouslyBlows` and `#ATotallyRocks` represent contexts which restrict the context represented by `#A` by casting it in a negative and positive light respectively.

Suppose that you want your app to generate statistics about represented contexts rather than simply statistics about hashtags. There is an additional difficulty to this. To see this problem, consider the following tweet:

```#ATotallyRocks, lol```

This tweet *should* be considered when we are generating statistics about the context represented by `#A` because the context represented by `#ATotallyRocks` is coherent with the context represented by `#A`. However, the hashtag `#A` does not appear anywhere in the tweet. Accounting for such taxonomical relationships between hashtags would require your data store to be aware of the taxonomy in the first place. This is especially true in this case, where each hashtag is fulfilling a dual role as a token that can appear in a treat as well as a signifier of a concept.

*bling* is a means of making taxonomy-aware queries to a data store of this type. It handles takes care of the logistics of relating the taxonomy to the data, of maintaining consistency between the two, and of ensuring that work is not unnecessarily repeated when performing queries to the data base.



## What is a taxonomy really?

Conventionally, people tend to think of taxonomies as trees of concepts. This is a bit restrictive, however, as a given object in a taxonomy can be derived from multiple others. For example, in the hashtag taxonomy of the previous section, the hashtag the context represented by `#AlovesB` could be viewed as a restriction of the context represented by `#A` as well as the context represented by `#B` *as well as* the contexts represented by `#love` and `#like` and `#cooties` and so on. Although there are many possible schemes by which one may impose a tree structure on hashtags to generate a taxonomy of represented contexts, there does not seem to be an easy way to do so while simultaneously allowing for such a liberal notion of derivation.

The reason that a tree structure is normally assumed in the first place is to rule out the existence of circularities in the taxonomy, so that a possibility of classification is not paradoxically contingent upon the possibility of classification. However, there *is* a mathematical structure which rules out such paradoxes while at the same time allowing the representation of rich derivational relationships —  an acyclic directed graph (DAG).

*bling* represents taxonomies as rooted DAGs, i.e. DAGs having a root vertex which is an ancestor to every other vertex. In terms of contexts, this represents something like a null context from which every other context is derived.



## Taxonomy-conscious queries

*bling* does maintain information in the data store about which data are tagged with which contexts, but it also allows a user to define contexts outside the data store as aggregates of existing contexts. These externally defined contexts need not ever directly correspond to a tag used on any of the data. When a query is made to the data store, *bling* looks at all the contexts involved in the query, expands each of them to construct a compound SQL query involving the children which are present as tags in the data store, and then executes this query on the data base.



## The guts

Users interface with *bling* through a *BlingConsole* object. The *BlingConsole* makes use of a *tags.TagDag* object to manage the taxonomy of contexts and it makes use of a *data.DataHandler* object to manage the data store.

Insertion of data into the data store requires tag information to be extracted from each entry and explicitly passed to *bling* along with the entry itself. For example, in the Twitter example from above, *bling* would require the user to extract the hashtags present in each tweet and pass them to *bling* along with the tweets themselves. This can be automated, of course, on top of the basic *bling* layer.

Queries are constructed using *SelectionCriterion* objects. *SelectionCriterion* is an algebraic data type which allows users to construct SQL queries using scala data types.

For more information:

1. You can generate API documentation for the project by cloning the repository and running `$ sbt doc` from the top-level *bling* directory. The documentation will be generated in `<path-to-bling>/target/scala-2.11/api/`
2. The *specs2* specifications serve as examples of how* *to use the different components of *bling*.
3. The repository contains a *SampleApp* which you can run to see *bling* in action at a very primitive level and which you can browse the code of to see how to initialize a *BlingConsole*.



