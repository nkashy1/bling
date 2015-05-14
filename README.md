# bling

Tagged data handler.

---

### Status

Everything you read here is intended as a promise, not as a representation of the current state of the project.

---

### The problem

You are receiving a large volume of data. Each data point may be associated with one or (usually) more contexts. These contexts are central to the analysis of the data -- each query involving this data includes some specification of context. You need a program that can handle the data -- both the data store itself, as well as queries to the data store. The problem is that you will not know when you deploy your data handler the full list of contexts under which the data can be considered! In fact, your users demand the ability to create contexts at will!

*bling* provides an off-the-shelf solution to this problem. It allows you to store your data in a relational database and provides quick responses when you make contextual queries to this database.

---

### Tags

Tags are strings. Each data point is passed to bling together with a collection of tags which signify the contexts to which the point belongs. To a large extent, these tags serve the same function as Twitter hashtags.

*bling* goes one better, though, in allowing for the specification of relationships *between* tags. Each *bling* instance assumes that the tags it knows of form a rooted, directed, acyclic graph (with the root signifying universal context). In addition to data management, *bling* provides a convenient interface for the management of tag relationships.