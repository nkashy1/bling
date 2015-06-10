package systems.adaptix.bling.tags

import org.specs2.mutable.Specification

import org.json4s._
import org.json4s.native.JsonMethods._


/**
 * Created by nkashyap on 6/10/15.
 */

class JsonTagDagSerializerSpecification extends Specification {
  "The JsonTagDagSerializer provides methods to serialize TagDags as JSON strings, and deserialize JSON strings representing TagDags back to TagDag objects." >> {
    implicit val formats = DefaultFormats

    val serializer = new JsonTagDagSerializer {}

    "The serialize method converts a TagDag to a JSON string." >> {
      val tagDag = TagDag("root")
      tagDag.insertTag("child1")
      tagDag.insertTag("child2")
      tagDag.insertTag("grandchild1", Set("child1"))
      tagDag.insertTag("grandchild2", Set("child2"))
      tagDag.insertTag("greatgrandchild", Set("grandchild1", "grandchild2"))

      val serialization = serializer.serialize(tagDag)
      val json = parse(serialization)
      val extractedSerializationInfo = json.extract[TagDagSerializationInfo]

      extractedSerializationInfo.universalTag mustEqual tagDag.universalTag

      val parentsAndChildren = extractedSerializationInfo.parentsAndChildren
      parentsAndChildren("root") mustEqual Set("child1", "child2")
      parentsAndChildren("child1") mustEqual Set("grandchild1")
      parentsAndChildren("child2") mustEqual Set("grandchild2")
      parentsAndChildren("grandchild1") mustEqual Set("greatgrandchild")
      parentsAndChildren("grandchild2") mustEqual Set("greatgrandchild")
      parentsAndChildren("greatgrandchild") mustEqual Set()
      parentsAndChildren.keys must haveSize(6)
    }

    "The deserialize method converts a JSON string representing a TagDag back into a TagDag." >> {
      val serialization =
        """{
          |"universalTag": "root",
          |"parentsAndChildren": {
          | "root": ["child1", "child2"],
          | "child1": ["grandchild1"],
          | "child2": ["grandchild2"],
          | "grandchild1": ["greatgrandchild"],
          | "grandchild2": ["greatgrandchild"],
          | "greatgrandchild": []
          | }
          |}
        """.stripMargin
      val tagDag = serializer.deserialize(serialization)

      tagDag.universalTag mustEqual "root"
      tagDag.tagVertices.keys.toSet mustEqual Set("root", "child1", "child2", "grandchild1", "grandchild2", "greatgrandchild")

      def childrenTags(tag: TagDag#Tag) = tagDag.tagVertices(tag).children.map(_.label).toSet
      childrenTags("root") mustEqual Set("child1", "child2")
      childrenTags("child1") mustEqual Set("grandchild1")
      childrenTags("child2") mustEqual Set("grandchild2")
      childrenTags("grandchild1") mustEqual Set("greatgrandchild")
      childrenTags("grandchild2") mustEqual Set("greatgrandchild")
      childrenTags("greatgrandchild") mustEqual Set[TagDag#Tag]()
    }
  }
}
