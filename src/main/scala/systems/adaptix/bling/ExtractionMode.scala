package systems.adaptix.bling

/**
 * Created by nkashyap on 6/10/15.
 */
sealed trait ExtractionMode

object DisjunctiveMode extends ExtractionMode
object ConjunctiveMode extends ExtractionMode
