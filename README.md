# bling!

Tagged data handler.

---

### The problem

You are receiving a large volume of data. Each data point may be associated with one or (usually) more contexts. These contexts are central to the analysis of the data -- each query involving this data includes some specification of context. You need a program that can handle the data -- both the data store itself, as well as queries to the data store. The problem is that you will not know when you deploy your data handler the full list of contexts under which the data can be considered! In fact, your users want the ability to create contexts at will!

bling! provides an out-of-the-box solution to this problem.

---

